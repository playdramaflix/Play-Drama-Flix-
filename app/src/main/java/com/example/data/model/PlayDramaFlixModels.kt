package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContentResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "status") val status: Any? = null,
    @Json(name = "total") val total: Int? = 0,
    @Json(name = "data") val data: List<ContentItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ContentItemDto(
    @Json(name = "id") val rawId: Any? = null,
    @Json(name = "type") val type: String = "series", // "movie" | "series" | "shorts" | "anime"
    @Json(name = "title") val title: String = "",
    @Json(name = "slug") val slug: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "meta_description") val metaDescription: String? = null,
    @Json(name = "language") val language: String = "Bangla Dubbed",
    @Json(name = "dub_badge") val customDubBadge: String? = null,
    @Json(name = "release_year") val releaseYear: String = "2026",
    @Json(name = "rating") val rawRating: Any? = "8.5",
    @Json(name = "views") val rawViews: Any? = 0,
    @Json(name = "categories") val rawCategories: Any? = null,
    @Json(name = "total_episodes") val rawTotalEpisodes: Any? = null,
    @Json(name = "poster_url") val posterUrl: String? = null,
    @Json(name = "banner_url") val bannerUrl: String? = null,
    @Json(name = "share_url") val shareUrl: String? = null,
    @Json(name = "synopsis") val customSynopsis: String? = null,
    @Json(name = "is_featured") val isFeatured: Boolean = false,
    @Json(name = "is_recent") val isRecent: Boolean = false,
    @Json(name = "is_hot") val isHot: Boolean = false
) {
    val id: String
        get() = rawId?.toString() ?: slug

    val rating: Double
        get() = when (rawRating) {
            is Number -> rawRating.toDouble()
            is String -> rawRating.toDoubleOrNull() ?: 8.5
            else -> 8.5
        }

    val views: String
        get() = when (rawViews) {
            is Number -> "${rawViews} views"
            is String -> rawViews
            else -> "0 views"
        }

    val categories: List<String>
        get() = when (rawCategories) {
            is List<*> -> rawCategories.filterIsInstance<String>()
            is String -> if (rawCategories.isNotBlank()) listOf(rawCategories) else listOf("Drama Series")
            else -> listOf("Drama Series")
        }

    val totalEpisodes: Int
        get() {
            val num = when (rawTotalEpisodes) {
                is Number -> rawTotalEpisodes.toInt()
                is String -> rawTotalEpisodes.toIntOrNull() ?: 0
                else -> 0
            }
            return if (num > 0) num else 1
        }

    val dubBadge: String
        get() {
            if (!customDubBadge.isNullOrBlank()) return customDubBadge
            val lower = language.lowercase()
            return when {
                lower.contains("bangla") -> "Bangla Dub"
                lower.contains("hindi") -> "Hindi Dub"
                lower.contains("dual") -> "Dual Audio"
                else -> "Bangla Dub"
            }
        }

    val synopsis: String
        get() = description?.takeIf { it.isNotBlank() } ?: customSynopsis ?: metaDescription ?: "Watch full episodes in HD on PlayDramaFlix."
}

@JsonClass(generateAdapter = true)
data class WatchDetailResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "status") val status: Any? = null,
    @Json(name = "content") val content: ContentItemDto? = null,
    @Json(name = "servers") val servers: List<ServerDto> = emptyList(),
    @Json(name = "episodes") val episodes: List<EpisodeDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ServerDto(
    @Json(name = "id") val rawId: Any? = null,
    @Json(name = "content_id") val rawContentId: Any? = null,
    @Json(name = "episode_id") val rawEpisodeId: Any? = null,
    @Json(name = "server_name") val serverName: String? = "Server 1 (Byse.sx)",
    @Json(name = "raw_url") val rawUrl: String? = null,
    @Json(name = "embed_url") val embedUrl: String? = null,
    @Json(name = "server_type") val serverType: String? = "stream",
    @Json(name = "quality") val quality: String? = "Streaming"
) {
    val id: String get() = rawId?.toString() ?: ""
    val name: String get() = serverName ?: "Server 1 (Byse.sx)"
    val episodeId: String get() = rawEpisodeId?.toString() ?: ""
    val url: String get() = embedUrl?.takeIf { it.isNotBlank() } ?: rawUrl ?: ""
    val type: String get() = if (serverType == "hls" || url.endsWith(".m3u8")) "hls" else if (url.contains("/e/") || url.contains("embed") || url.contains("byse")) "embed" else "embed"
}

@JsonClass(generateAdapter = true)
data class EpisodeDto(
    @Json(name = "episode_id") val rawEpisodeId: Any? = null,
    @Json(name = "episode_number") val episodeNumber: Int = 1,
    @Json(name = "ep_title") val epTitle: String = "Episode 1",
    @Json(name = "season_number") val seasonNumber: Int = 1,
    @Json(name = "duration") val duration: String = "24m",
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "embed_url") val embedUrl: String? = null,
    @Json(name = "is_locked") val isLocked: Boolean = false,
    @Json(name = "ads_count") val adsCount: Int = 0
) {
    val episodeId: String get() = rawEpisodeId?.toString() ?: episodeNumber.toString()
}

@JsonClass(generateAdapter = true)
data class NotificationResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "status") val status: Any? = null,
    @Json(name = "total") val total: Int? = 0,
    @Json(name = "data") val data: List<NotificationItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NotificationItemDto(
    @Json(name = "id") val rawId: Any? = null,
    @Json(name = "title") val title: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "url") val url: String? = null,
    @Json(name = "icon_type") val iconType: String = "series",
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "time_ago") val customTimeAgo: String? = null,
    @Json(name = "is_read") val isRead: Boolean = false
) {
    val id: String get() = rawId?.toString() ?: ""
    val timeAgo: String get() = customTimeAgo ?: createdAt ?: "Recent"
}

@JsonClass(generateAdapter = true)
data class DeviceRegisterRequest(
    @Json(name = "device_token") val deviceToken: String,
    @Json(name = "onesignal_player_id") val onesignalPlayerId: String? = null,
    @Json(name = "platform") val platform: String = "android",
    @Json(name = "app_version") val appVersion: String = "1.0.0",
    @Json(name = "device_model") val deviceModel: String = "Android Device",
    @Json(name = "os_version") val osVersion: String = "14"
)

