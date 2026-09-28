package com.example.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RakshaVaaniApi {

    @GET("health")
    suspend fun checkHealth(): Response<HealthResponse>

    @GET("status")
    suspend fun checkStatus(): Response<HealthResponse>

    @Multipart
    @POST("enhance")
    suspend fun enhanceAudio(
        @Part audio: MultipartBody.Part,
        @Part("noise_category") noiseCategory: RequestBody,
        @Part("noise_intensity") noiseIntensity: RequestBody,
        @Part("sample_rate") sampleRate: RequestBody
    ): Response<ResponseBody>
}
