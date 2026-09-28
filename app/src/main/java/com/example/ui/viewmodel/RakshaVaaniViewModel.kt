package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlayer
import com.example.audio.AudioRecorder
import com.example.audio.WavHelper
import com.example.data.AppDatabase
import com.example.data.AudioEnhancementRecord
import com.example.data.AudioRecordRepository
import com.example.network.EnhancementMetadata
import com.example.network.HealthResponse
import com.example.network.NetworkResult
import com.example.network.RakshaVaaniNetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class PipelineStage {
    CAPTURE,
    NOISE_ANALYSIS,
    SPEECH_ENHANCEMENT,
    OUTPUT
}

enum class PipelineStatus {
    READY,
    ANALYZING,
    PROCESSING,
    COMPLETE,
    ERROR,
    BACKEND_OFFLINE
}

data class ActiveAudioInfo(
    val file: File,
    val fileName: String,
    val durationMs: Long,
    val durationFormatted: String,
    val fileSizeFormatted: String,
    val sampleRate: Int = 16000,
    val amplitudes: List<Float> = emptyList(),
    val isRecording: Boolean = false
)

data class BackendSettings(
    val serverIp: String = "192.168.1.10",
    val serverPort: Int = 8000,
    val processingMode: String = "Remote Prototype", // "Remote Prototype", "On-Device (Planned)"
    val sampleRate: String = "16 kHz",
    val audioQuality: String = "High",
    val autoPlay: Boolean = true,
    val isOnline: Boolean = false,
    val isCheckingConnection: Boolean = false,
    val lastPingMessage: String? = null
)

class RakshaVaaniViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val database = AppDatabase.getInstance(context)
    private val repository = AudioRecordRepository(database.audioRecordDao())
    private val networkClient = RakshaVaaniNetworkClient(context)

    val recorder = AudioRecorder(context)
    val player = AudioPlayer(context)

    // Active Audio State
    private val _activeAudio = MutableStateFlow<ActiveAudioInfo?>(null)
    val activeAudio: StateFlow<ActiveAudioInfo?> = _activeAudio.asStateFlow()

    // Enhanced Audio State
    private val _enhancedAudio = MutableStateFlow<ActiveAudioInfo?>(null)
    val enhancedAudio: StateFlow<ActiveAudioInfo?> = _enhancedAudio.asStateFlow()

    // A/B Comparison Switch: true = Enhanced, false = Original Raw
    private val _isPlayingEnhanced = MutableStateFlow(true)
    val isPlayingEnhanced: StateFlow<Boolean> = _isPlayingEnhanced.asStateFlow()

    // Noise Settings
    private val _selectedNoiseCategory = MutableStateFlow("UAV / Drone")
    val selectedNoiseCategory: StateFlow<String> = _selectedNoiseCategory.asStateFlow()

    private val _noiseIntensity = MutableStateFlow(60) // 0..100
    val noiseIntensity: StateFlow<Int> = _noiseIntensity.asStateFlow()

    // Pipeline Telemetry State
    private val _pipelineStage = MutableStateFlow(PipelineStage.CAPTURE)
    val pipelineStage: StateFlow<PipelineStage> = _pipelineStage.asStateFlow()

    private val _pipelineStatus = MutableStateFlow(PipelineStatus.READY)
    val pipelineStatus: StateFlow<PipelineStatus> = _pipelineStatus.asStateFlow()

    private val _transmissionProgress = MutableStateFlow(0f)
    val transmissionProgress: StateFlow<Float> = _transmissionProgress.asStateFlow()

    private val _transmissionStatusText = MutableStateFlow("Ready for transmission")
    val transmissionStatusText: StateFlow<String> = _transmissionStatusText.asStateFlow()

    private val _lastMetrics = MutableStateFlow<EnhancementMetadata?>(null)
    val lastMetrics: StateFlow<EnhancementMetadata?> = _lastMetrics.asStateFlow()

    private val _backendOfflineMessage = MutableStateFlow<String?>(null)
    val backendOfflineMessage: StateFlow<String?> = _backendOfflineMessage.asStateFlow()

    // Settings
    private val _settings = MutableStateFlow(BackendSettings())
    val settings: StateFlow<BackendSettings> = _settings.asStateFlow()

    // History
    val historyRecords = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _historyFilter = MutableStateFlow("All") // "All", "Recorded", "Processed"
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    private var transmissionJob: Job? = null

    init {
        // Prepare initial demo sample so user can instantly preview and test
        loadDemoSample("UAV / Drone")
        // Check initial backend health in background
        checkBackendHealth()
    }

    fun setNoiseCategory(category: String) {
        _selectedNoiseCategory.value = category
    }

    fun setNoiseIntensity(intensity: Int) {
        _noiseIntensity.value = intensity.coerceIn(0, 100)
    }

    fun getSnrDescription(intensity: Int): String {
        return when {
            intensity <= 20 -> "+10 dB (Low Contamination)"
            intensity <= 40 -> "+5 dB (Moderate Noise)"
            intensity <= 60 -> "0 dB (Equal Noise/Signal)"
            intensity <= 75 -> "-5 dB (High Degradation)"
            intensity <= 90 -> "-10 dB to -15 dB (Severe Tactical)"
            else -> "-20 dB (Critical Low-SNR)"
        }
    }

    fun getSnrShort(intensity: Int): String {
        return when {
            intensity <= 20 -> "+10 dB"
            intensity <= 40 -> "+5 dB"
            intensity <= 60 -> "0 dB"
            intensity <= 75 -> "-5 dB"
            intensity <= 90 -> "-10 dB"
            else -> "-20 dB"
        }
    }

    fun loadDemoSample(noiseCategory: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = WavHelper.generateTacticalDemoSample(
                context = context,
                name = noiseCategory.replace("/", "").replace(" ", "_").lowercase(),
                noiseType = noiseCategory,
                durationSeconds = 5,
                isEnhanced = false
            )
            setActiveAudioFromFile(file, "tactical_${noiseCategory.take(3).lowercase()}_001.wav")
        }
    }

    fun setActiveAudioFromFile(file: File, displayFileName: String = file.name) {
        val durationMs = (file.length() / (16000 * 2) * 1000).coerceAtLeast(1000)
        val sec = durationMs / 1000
        val durationFormatted = String.format("%02d:%02d", sec / 60, sec % 60)
        val sizeMb = file.length() / (1024f * 1024f)
        val sizeFormatted = String.format("%.1f MB", sizeMb.coerceAtLeast(0.1f))
        val amplitudes = WavHelper.extractAmplitudes(file, 48)

        _activeAudio.value = ActiveAudioInfo(
            file = file,
            fileName = displayFileName,
            durationMs = durationMs,
            durationFormatted = durationFormatted,
            fileSizeFormatted = sizeFormatted,
            sampleRate = 16000,
            amplitudes = amplitudes
        )
    }

    fun startRecording() {
        player.stop()
        val file = recorder.startRecording(viewModelScope)
        if (file != null) {
            _activeAudio.value = ActiveAudioInfo(
                file = file,
                fileName = file.name,
                durationMs = 0,
                durationFormatted = "00:00",
                fileSizeFormatted = "0.0 MB",
                isRecording = true
            )
        }
    }

    fun stopRecording() {
        val file = recorder.stopRecording()
        if (file != null && file.exists()) {
            setActiveAudioFromFile(file, "field_rec_${System.currentTimeMillis() % 10000}.wav")

            // Insert recorded audio into Room history
            viewModelScope.launch(Dispatchers.IO) {
                val record = AudioEnhancementRecord(
                    fileName = _activeAudio.value?.fileName ?: "recording.wav",
                    originalFilePath = file.absolutePath,
                    durationMs = _activeAudio.value?.durationMs ?: 3000,
                    noiseCategory = _selectedNoiseCategory.value,
                    noiseIntensity = _noiseIntensity.value,
                    snrEstimate = getSnrShort(_noiseIntensity.value),
                    status = "RECORDED",
                    fileSizeFormatted = _activeAudio.value?.fileSizeFormatted ?: "0.5 MB"
                )
                repository.insert(record)
            }
        }
    }

    fun cancelRecording() {
        recorder.cancelRecording()
    }

    fun toggleAudioPlayback() {
        val currentFile = if (_isPlayingEnhanced.value && _enhancedAudio.value != null) {
            _enhancedAudio.value?.file
        } else {
            _activeAudio.value?.file
        }

        if (currentFile != null) {
            if (player.isPlaying.value) {
                player.pause()
            } else {
                player.loadAudio(currentFile, autoPlay = true, scope = viewModelScope)
            }
        }
    }

    fun switchABComparison(playEnhanced: Boolean) {
        _isPlayingEnhanced.value = playEnhanced
        val targetFile = if (playEnhanced && _enhancedAudio.value != null) {
            _enhancedAudio.value?.file
        } else {
            _activeAudio.value?.file
        }

        if (targetFile != null) {
            val wasPlaying = player.isPlaying.value
            val currentProgress = player.playbackProgress.value
            player.loadAudio(targetFile, autoPlay = wasPlaying, scope = viewModelScope)
            if (wasPlaying) {
                player.seekTo(currentProgress)
            }
        }
    }

    fun sendToEngine(onComplete: () -> Unit) {
        val input = _activeAudio.value?.file ?: return

        transmissionJob?.cancel()
        _pipelineStatus.value = PipelineStatus.ANALYZING
        _pipelineStage.value = PipelineStage.NOISE_ANALYSIS
        _transmissionProgress.value = 0.1f
        _transmissionStatusText.value = "Analyzing acoustic noise profile..."
        _backendOfflineMessage.value = null

        transmissionJob = viewModelScope.launch(Dispatchers.IO) {
            // Stage 1: Noise Analysis
            delay(600)
            _transmissionProgress.value = 0.35f
            _pipelineStage.value = PipelineStage.SPEECH_ENHANCEMENT
            _pipelineStatus.value = PipelineStatus.PROCESSING
            _transmissionStatusText.value = "Connecting to Python RakshaVaani backend..."

            // Try real network backend
            val result = networkClient.sendAudioForEnhancement(
                inputFile = input,
                noiseCategory = _selectedNoiseCategory.value,
                noiseIntensity = _noiseIntensity.value,
                serverIp = _settings.value.serverIp,
                port = _settings.value.serverPort
            )

            when (result) {
                is NetworkResult.Success -> {
                    val (outFile, metadata) = result.data
                    _transmissionProgress.value = 0.85f
                    _pipelineStage.value = PipelineStage.OUTPUT
                    _transmissionStatusText.value = "Receiving enhanced speech stream..."
                    delay(400)

                    _transmissionProgress.value = 1.0f
                    _pipelineStatus.value = PipelineStatus.COMPLETE
                    _lastMetrics.value = metadata
                    setupEnhancedAudio(outFile, metadata)
                    saveResultToHistory(outFile, metadata)
                    _settings.value = _settings.value.copy(isOnline = true)
                    onComplete()
                }
                is NetworkResult.BackendOffline -> {
                    _pipelineStatus.value = PipelineStatus.BACKEND_OFFLINE
                    _backendOfflineMessage.value = "BACKEND OFFLINE: Python RakshaVaani Engine unreachable at ${result.endpoint} (${result.message})"
                    _settings.value = _settings.value.copy(isOnline = false)
                }
                is NetworkResult.Error -> {
                    _pipelineStatus.value = PipelineStatus.ERROR
                    _backendOfflineMessage.value = result.message
                }
            }
        }
    }

    /**
     * Operator option to run the Diagnostic Test Bench when the laptop GPU isn't active on Wi-Fi.
     * Accurately labeled in the UI as "OFFLINE SIMULATION / TEST BENCH".
     */
    fun runOfflineDiagnosticEnhancement(onComplete: () -> Unit) {
        val input = _activeAudio.value?.file ?: return

        transmissionJob?.cancel()
        _pipelineStatus.value = PipelineStatus.PROCESSING
        _pipelineStage.value = PipelineStage.SPEECH_ENHANCEMENT
        _transmissionProgress.value = 0.3f
        _transmissionStatusText.value = "Running On-Device Acoustic Test Bench (Diagnostic Mode)..."

        transmissionJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            _transmissionProgress.value = 0.65f
            val (outFile, metadata) = networkClient.runDiagnosticOfflineEnhancement(
                inputFile = input,
                noiseCategory = _selectedNoiseCategory.value,
                noiseIntensity = _noiseIntensity.value
            )
            delay(400)
            _transmissionProgress.value = 1.0f
            _pipelineStage.value = PipelineStage.OUTPUT
            _pipelineStatus.value = PipelineStatus.COMPLETE
            _lastMetrics.value = metadata
            setupEnhancedAudio(outFile, metadata)
            saveResultToHistory(outFile, metadata)
            onComplete()
        }
    }

    private fun setupEnhancedAudio(file: File, metadata: EnhancementMetadata) {
        val durationMs = _activeAudio.value?.durationMs ?: 5000
        val sec = durationMs / 1000
        val durationFormatted = String.format("%02d:%02d", sec / 60, sec % 60)
        val sizeFormatted = _activeAudio.value?.fileSizeFormatted ?: "2.4 MB"
        val amplitudes = WavHelper.extractAmplitudes(file, 48)

        _enhancedAudio.value = ActiveAudioInfo(
            file = file,
            fileName = "enhanced_${_activeAudio.value?.fileName ?: "audio.wav"}",
            durationMs = durationMs,
            durationFormatted = durationFormatted,
            fileSizeFormatted = sizeFormatted,
            sampleRate = 16000,
            amplitudes = amplitudes
        )
        _isPlayingEnhanced.value = true

        if (_settings.value.autoPlay) {
            player.loadAudio(file, autoPlay = true, scope = viewModelScope)
        }
    }

    private fun saveResultToHistory(outFile: File, metadata: EnhancementMetadata) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = AudioEnhancementRecord(
                fileName = "enhanced_${_activeAudio.value?.fileName ?: "audio.wav"}",
                originalFilePath = _activeAudio.value?.file?.absolutePath ?: "",
                enhancedFilePath = outFile.absolutePath,
                durationMs = _activeAudio.value?.durationMs ?: 4000,
                noiseCategory = _selectedNoiseCategory.value,
                noiseIntensity = _noiseIntensity.value,
                snrEstimate = getSnrShort(_noiseIntensity.value),
                status = "PROCESSED",
                processingTimeMs = metadata.processingTimeMs,
                snrImprovement = metadata.snrImprovementDb,
                siSnr = metadata.siSnrDb,
                pesqEst = metadata.pesqEst,
                fileSizeFormatted = _activeAudio.value?.fileSizeFormatted ?: "2.4 MB"
            )
            repository.insert(record)
        }
    }

    fun checkBackendHealth() {
        viewModelScope.launch(Dispatchers.IO) {
            _settings.value = _settings.value.copy(isCheckingConnection = true)
            val res = networkClient.checkHealth(_settings.value.serverIp, _settings.value.serverPort)
            when (res) {
                is NetworkResult.Success -> {
                    _settings.value = _settings.value.copy(
                        isOnline = true,
                        isCheckingConnection = false,
                        lastPingMessage = "Online: ${res.data.engine} (${res.data.device})"
                    )
                }
                is NetworkResult.BackendOffline -> {
                    _settings.value = _settings.value.copy(
                        isOnline = false,
                        isCheckingConnection = false,
                        lastPingMessage = "Offline: ${res.message}"
                    )
                }
                is NetworkResult.Error -> {
                    _settings.value = _settings.value.copy(
                        isOnline = false,
                        isCheckingConnection = false,
                        lastPingMessage = "Error: ${res.message}"
                    )
                }
            }
        }
    }

    fun updateServerIp(ip: String) {
        _settings.value = _settings.value.copy(serverIp = ip.trim())
        checkBackendHealth()
    }

    fun updateServerPort(port: Int) {
        _settings.value = _settings.value.copy(serverPort = port)
        checkBackendHealth()
    }

    fun updateProcessingMode(mode: String) {
        _settings.value = _settings.value.copy(processingMode = mode)
    }

    fun toggleAutoPlay(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoPlay = enabled)
    }

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun cancelTransmission() {
        transmissionJob?.cancel()
        _pipelineStatus.value = PipelineStatus.READY
        _transmissionProgress.value = 0f
    }

    fun clearActiveAudio() {
        player.stop()
        _activeAudio.value = null
        _enhancedAudio.value = null
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        recorder.cancelRecording()
    }
}
