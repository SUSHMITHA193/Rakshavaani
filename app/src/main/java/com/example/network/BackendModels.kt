package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthResponse(
    @Json(name = "status") val status: String = "online",
    @Json(name = "engine") val engine: String = "RakshaVaani GTCRN Service",
    @Json(name = "device") val device: String = "NVIDIA RTX 5050",
    @Json(name = "sample_rate") val sampleRate: Int = 16000,
    @Json(name = "version") val version: String = "1.0-tactical"
)

@JsonClass(generateAdapter = true)
data class EnhancementMetadata(
    @Json(name = "processing_time_ms") val processingTimeMs: Long = 0,
    @Json(name = "snr_improvement_db") val snrImprovementDb: Float = 0f,
    @Json(name = "si_snr_db") val siSnrDb: Float = 0f,
    @Json(name = "pesq_est") val pesqEst: Float = 0f,
    @Json(name = "noise_profile") val noiseProfile: String = "",
    @Json(name = "noise_intensity") val noiseIntensity: Int = 0,
    @Json(name = "model") val model: String = "GTCRN (16kHz)",
    @Json(name = "is_simulated") val isSimulated: Boolean = false
)

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class BackendOffline(val message: String, val endpoint: String) : NetworkResult<Nothing>()
    data class Error(val message: String, val throwable: Throwable? = null) : NetworkResult<Nothing>()
}
