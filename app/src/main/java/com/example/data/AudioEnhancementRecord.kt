package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_records")
data class AudioEnhancementRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val originalFilePath: String,
    val enhancedFilePath: String? = null,
    val durationMs: Long = 0,
    val sampleRate: Int = 16000,
    val noiseCategory: String = "UAV / Drone",
    val noiseIntensity: Int = 60,
    val snrEstimate: String = "-10 dB",
    val status: String = "RECORDED", // "RECORDED", "PROCESSED", "FAILED"
    val processingTimeMs: Long = 0,
    val snrImprovement: Float? = null,
    val siSnr: Float? = null,
    val pesqEst: Float? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val fileSizeFormatted: String = "2.4 MB",
    val isDemoSample: Boolean = false
)
