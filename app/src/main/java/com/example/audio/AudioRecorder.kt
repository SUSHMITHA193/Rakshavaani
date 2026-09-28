package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class AudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var recordingFile: File? = null

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _recordedDurationSeconds = MutableStateFlow(0)
    val recordedDurationSeconds: StateFlow<Int> = _recordedDurationSeconds.asStateFlow()

    private val sampleRate = WavHelper.SAMPLE_RATE_16K
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    @SuppressLint("MissingPermission")
    fun startRecording(coroutineScope: CoroutineScope): File? {
        if (_isRecording.value) return recordingFile

        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                audioRecord?.release()
                audioRecord = null
                return null
            }

            val timestamp = System.currentTimeMillis()
            val outputFile = File(context.cacheDir, "raksha_rec_$timestamp.wav")
            recordingFile = outputFile

            audioRecord?.startRecording()
            _isRecording.value = true
            _recordedDurationSeconds.value = 0

            recordingJob = coroutineScope.launch(Dispatchers.IO) {
                var totalBytesWritten = 0L
                val startTime = System.currentTimeMillis()

                FileOutputStream(outputFile).use { fos ->
                    // Write placeholder WAV header
                    WavHelper.writeWavHeader(fos, 0, 36)

                    val audioBuffer = ByteArray(bufferSize)
                    while (isActive && _isRecording.value) {
                        val readBytes = audioRecord?.read(audioBuffer, 0, bufferSize) ?: -1
                        if (readBytes > 0) {
                            fos.write(audioBuffer, 0, readBytes)
                            totalBytesWritten += readBytes

                            // Calculate peak amplitude for waveform feedback
                            var maxAmp = 0
                            for (i in 0 until readBytes step 2) {
                                if (i + 1 < readBytes) {
                                    val sample = (audioBuffer[i].toInt() and 0xFF) or (audioBuffer[i + 1].toInt() shl 8)
                                    val shortVal = sample.toShort()
                                    val absVal = abs(shortVal.toInt())
                                    if (absVal > maxAmp) maxAmp = absVal
                                }
                            }
                            _currentAmplitude.value = (maxAmp / 32768f).coerceIn(0f, 1f)
                        }

                        val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                        _recordedDurationSeconds.value = elapsedSec
                    }
                }

                // Finalize WAV header sizes with actual written PCM length
                WavHelper.updateWavHeaderSizes(outputFile, totalBytesWritten)
            }

            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
            return null
        }
    }

    fun stopRecording(): File? {
        if (!_isRecording.value) return recordingFile

        _isRecording.value = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            audioRecord = null
        }

        recordingJob?.cancel()
        recordingJob = null
        _currentAmplitude.value = 0f

        return recordingFile
    }

    fun cancelRecording() {
        stopRecording()
        recordingFile?.delete()
        recordingFile = null
        _recordedDurationSeconds.value = 0
    }
}
