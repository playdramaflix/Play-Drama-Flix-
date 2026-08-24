package com.example.data.remote

import com.example.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface PlayDramaFlixApiService {

    @GET("contents")
    suspend fun getContents(
        @Query("category") category: String? = null,
        @Query("language") language: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int? = 1
    ): Response<ContentResponse>

    @GET("watch/{slug}")
    suspend fun getWatchDetails(
        @Path("slug") slug: String
    ): Response<WatchDetailResponse>

    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationResponse>

    @POST("devices/register")
    suspend fun registerDevice(
        @Body request: DeviceRegisterRequest
    ): Response<Map<String, Any>>
}
