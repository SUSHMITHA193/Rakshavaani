package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null
    private var currentFilePath: String? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0)
    val totalDurationMs: StateFlow<Int> = _totalDurationMs.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress.asStateFlow()

    fun loadAudio(file: File, autoPlay: Boolean = false, scope: CoroutineScope) {
        stop()
        currentFilePath = file.absolutePath

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.fromFile(file))
                prepare()
                _totalDurationMs.value = duration.coerceAtLeast(1)
                _currentPositionMs.value = 0
                _playbackProgress.value = 0f

                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPositionMs.value = duration
                    _playbackProgress.value = 1f
                    stopProgressTracking()
                }

                if (autoPlay) {
                    start()
                    _isPlaying.value = true
                    startProgressTracking(scope)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun play(scope: CoroutineScope) {
        mediaPlayer?.let { player ->
            try {
                if (!player.isPlaying) {
                    if (_playbackProgress.value >= 0.99f) {
                        player.seekTo(0)
                        _currentPositionMs.value = 0
                        _playbackProgress.value = 0f
                    }
                    player.start()
                    _isPlaying.value = true
                    startProgressTracking(scope)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                    stopProgressTracking()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun togglePlayPause(scope: CoroutineScope) {
        if (_isPlaying.value) {
            pause()
        } else {
            play(scope)
        }
    }

    fun seekTo(progress: Float) {
        mediaPlayer?.let { player ->
            try {
                val targetMs = (progress.coerceIn(0f, 1f) * player.duration).toInt()
                player.seekTo(targetMs)
                _currentPositionMs.value = targetMs
                _playbackProgress.value = progress
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        stopProgressTracking()
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _isPlaying.value = false
            _currentPositionMs.value = 0
            _playbackProgress.value = 0f
        }
    }

    private fun startProgressTracking(scope: CoroutineScope) {
        stopProgressTracking()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            val pos = player.currentPosition
                            val dur = player.duration.coerceAtLeast(1)
                            _currentPositionMs.value = pos
                            _playbackProgress.value = pos.toFloat() / dur.toFloat()
                        }
                    } catch (e: Exception) {
                        // Player might be in transition
                    }
                }
                delay(50)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
    }
}
