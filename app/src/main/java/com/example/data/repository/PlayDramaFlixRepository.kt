package com.example.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.R
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.ApiClient
import com.example.data.remote.PlayDramaFlixApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlayDramaFlixRepository(
    private val context: Context,
    private val apiService: PlayDramaFlixApiService = ApiClient.apiService,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val watchHistoryDao = database.watchHistoryDao()
    private val watchlistDao = database.watchlistDao()

    // Room DB Observables
    val continueWatchingFlow: Flow<List<WatchHistoryEntity>> = watchHistoryDao.getContinueWatching()
    val watchlistFlow: Flow<List<WatchlistEntity>> = watchlistDao.getAllWatchlist()

    fun isItemInWatchlist(slug: String): Flow<Boolean> = watchlistDao.isInWatchlistFlow(slug)

    suspend fun toggleWatchlist(item: ContentItemDto, isInList: Boolean) = withContext(Dispatchers.IO) {
        if (isInList) {
            watchlistDao.removeFromWatchlist(item.slug)
        } else {
            watchlistDao.addToWatchlist(
                WatchlistEntity(
                    id = item.slug,
                    title = item.title,
                    posterUrl = item.posterUrl,
                    dubBadge = item.dubBadge ?: item.language,
                    rating = item.rating,
                    category = item.categories.firstOrNull() ?: item.type,
                    totalEpisodes = item.totalEpisodes
                )
            )
        }
    }

    suspend fun saveWatchProgress(
        content: ContentItemDto,
        episode: EpisodeDto,
        progressMs: Long,
        totalDurationMs: Long
    ) = withContext(Dispatchers.IO) {
        val pct = if (totalDurationMs > 0) (progressMs.toFloat() / totalDurationMs.toFloat()) else 0f
        watchHistoryDao.saveWatchProgress(
            WatchHistoryEntity(
                id = "${content.slug}_ep_${episode.episodeNumber}",
                contentSlug = content.slug,
                contentTitle = content.title,
                posterUrl = content.posterUrl,
                episodeNumber = episode.episodeNumber,
                episodeTitle = episode.epTitle,
                seasonNumber = episode.seasonNumber,
                progressMs = progressMs,
                totalDurationMs = totalDurationMs,
                progressPercentage = pct,
                dubBadge = content.dubBadge ?: content.language,
                lastWatchedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        watchHistoryDao.clearAllHistory()
    }

    // Fetch Contents from API or Fallback
    suspend fun getContents(): Result<List<ContentItemDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getContents()
            if (response.isSuccessful && response.body()?.data?.isNotEmpty() == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.success(getFallbackContents())
            }
        } catch (e: Exception) {
            Log.w("PlayDramaFlixRepo", "API call failed, serving rich curated fallback dataset: ${e.message}")
            Result.success(getFallbackContents())
        }
    }

    // Fetch Watch Details for a specific drama
    suspend fun getWatchDetails(slug: String): Result<WatchDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWatchDetails(slug)
            if (response.isSuccessful && response.body()?.content != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackWatchDetails(slug))
            }
        } catch (e: Exception) {
            Log.w("PlayDramaFlixRepo", "Watch details API failed, serving fallback details for slug=$slug: ${e.message}")
            Result.success(getFallbackWatchDetails(slug))
        }
    }

    // Fetch Notifications
    suspend fun getNotifications(): Result<List<NotificationItemDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body()?.data?.isNotEmpty() == true) {
                Result.success(response.body()!!.data)
            } else {
                Result.success(getFallbackNotifications())
            }
        } catch (e: Exception) {
            Result.success(getFallbackNotifications())
        }
    }

    // Register Device for FCM
    suspend fun registerDevice(token: String, oneSignalId: String? = null) = withContext(Dispatchers.IO) {
        try {
            val req = DeviceRegisterRequest(
                deviceToken = token,
                onesignalPlayerId = oneSignalId,
                platform = "android",
                appVersion = "1.0.0",
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                osVersion = Build.VERSION.RELEASE
            )
            apiService.registerDevice(req)
        } catch (e: Exception) {
            Log.e("PlayDramaFlixRepo", "Device registration error: ${e.message}")
        }
    }

    // Fallback Mock Data matching PlayDramaFlix website
    fun getFallbackContents(): List<ContentItemDto> {
        return listOf(
            ContentItemDto(
                rawId = "1",
                title = "Overflow Hindi Dubbed Available | Season 1 All Episodes",
                slug = "overflow-hindi-dubbed-available",
                type = "anime",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi Dub",
                releaseYear = "2020",
                rawRating = "8.9",
                rawViews = "142.5K",
                rawCategories = "Anime Series",
                rawTotalEpisodes = 8,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/overflow-hindi-dubbed-available",
                description = "Watch Overflow Hindi Dubbed online in HD with all episodes available. Complete Hindi dubbed episodes with crystal clear audio and fast servers.",
                isFeatured = true,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "2",
                title = "Guess Who I Am Hindi Dubbed | Full Episodes",
                slug = "guess-who-i-am-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi Dub",
                releaseYear = "2024",
                rawRating = "9.4",
                rawViews = "890K",
                rawCategories = "Drama Series",
                rawTotalEpisodes = 24,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                shareUrl = "https://playdramaflix.com/watch/guess-who-i-am-hindi-dubbed",
                description = "A legendary vigilante woman dedicated to punishing scumbags meets a mysterious corporate heir in an intense cat-and-mouse romance with thrilling twists.",
                isFeatured = true,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "10",
                title = "Squid Game Season 2 Bangla Dubbed",
                slug = "squid-game-season-2-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla Dub",
                releaseYear = "2025",
                rawRating = "9.8",
                rawViews = "1.5M",
                rawCategories = "Popular Series",
                rawTotalEpisodes = 9,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/squid-game-season-2-bangla-dubbed",
                description = "Player 456 returns with a fiery resolve as lethal new survival games test morality and trust in this global phenomenon.",
                isFeatured = true,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "11",
                title = "All of Us Are Dead Season 2 Hindi Dubbed",
                slug = "all-of-us-are-dead-season-2-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi Dub",
                releaseYear = "2025",
                rawRating = "9.6",
                rawViews = "1.2M",
                rawCategories = "Popular Series",
                rawTotalEpisodes = 12,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                shareUrl = "https://playdramaflix.com/all-of-us-are-dead-season-2-hindi-dubbed",
                description = "The battle for survival expands into the quarantined city as high school students fight for their future amidst evolved infected threats.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "12",
                title = "My Demon Season 1 Bangla Dubbed",
                slug = "my-demon-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla Dub",
                releaseYear = "2025",
                rawRating = "9.5",
                rawViews = "780K",
                rawCategories = "Popular Series",
                rawTotalEpisodes = 16,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/my-demon-season-1-bangla-dubbed",
                description = "A pitiless 200-year-old demon loses his powers upon meeting an icy chaebol heiress, sparking a thrilling contract marriage full of secrets.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "13",
                title = "Hidden Love Season 1 Bangla Dubbed",
                slug = "hidden-love-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla Dub",
                releaseYear = "2025",
                rawRating = "9.3",
                rawViews = "690K",
                rawCategories = "Popular Series",
                rawTotalEpisodes = 25,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787446896_6a8a4670591ea.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787446896_6a8a4670593c3.jpg",
                shareUrl = "https://playdramaflix.com/hidden-love-season-1-bangla-dubbed",
                description = "A sweet, heartwarming tale of a long-held youthful crush turning into a deep and mature love story through years of dedication.",
                isFeatured = false,
                isRecent = false,
                isHot = true
            ),
            ContentItemDto(
                rawId = "3",
                title = "Derailment Season 1 Hindi Dubbed",
                slug = "derailment-season-1-hindi-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla Dub",
                releaseYear = "2026",
                rawRating = "8.9",
                rawViews = "650K",
                rawCategories = "Drama Series",
                rawTotalEpisodes = 30,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                shareUrl = "https://playdramaflix.com/derailment-season-1-hindi-dubbed",
                description = "A wealthy heiress accidentally travels across parallel timelines and meets a mysterious childhood confidant who holds the key to her true identity.",
                isFeatured = false,
                isRecent = true,
                isHot = false
            ),
            ContentItemDto(
                rawId = "4",
                title = "The Proud Dragon God Bangla Dubbed Series",
                slug = "you-are-my-ultimate-taste-in-the-world-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "BANGLA DUB",
                releaseYear = "2026",
                rawRating = "9.3",
                rawViews = "215K",
                rawCategories = "Shorts Drama",
                rawTotalEpisodes = 7,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/you-are-my-ultimate-taste-in-the-world-bangla-dubbed",
                description = "A world-defying warrior descends from the sacred peaks to reclaim his empire and avenge his clan in explosive short reel episodes.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "5",
                title = "Mafia Se Mohobbat Bangla Dubbed",
                slug = "new-saga-season-1-hindi-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "BANGLA DUB",
                releaseYear = "2026",
                rawRating = "8.7",
                rawViews = "180K",
                rawCategories = "Anime Series",
                rawTotalEpisodes = 12,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_mafia_mohobbat}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_mafia_mohobbat}",
                shareUrl = "https://playdramaflix.com/new-saga-season-1-hindi-dubbed",
                description = "A perilous underground romance between a fierce protector and a high-society doctor tangled in underworld warfare and hidden feelings.",
                isFeatured = false,
                isRecent = false,
                isHot = true
            ),
            ContentItemDto(
                rawId = "6",
                title = "Like A Dragon Season 1 Bangla Dubbed",
                slug = "like-a-dragon-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla Dub",
                releaseYear = "2026",
                rawRating = "8.8",
                rawViews = "64.1K",
                rawCategories = "Shorts Drama",
                rawTotalEpisodes = 10,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787413105_6a89c271df941.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787413105_6a89c271dfab5.webp",
                shareUrl = "https://playdramaflix.com/like-a-dragon-season-1-bangla-dubbed",
                description = "An explosive story of loyalty, honor, and destiny unfold in modern Tokyo.",
                isFeatured = false,
                isRecent = false,
                isHot = false
            ),
            ContentItemDto(
                rawId = "7",
                title = "Lost In Love Bangla Dubbed",
                slug = "lost-in-love-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "BANGLA DUB",
                releaseYear = "2026",
                rawRating = "8.5",
                rawViews = "52.3K",
                rawCategories = "Shorts Drama",
                rawTotalEpisodes = 9,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787446896_6a8a4670591ea.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787446896_6a8a4670593c3.jpg",
                shareUrl = "https://playdramaflix.com/lost-in-love-bangla-dubbed",
                description = "Memories rediscovered across heartbreak, unexpected second chances, and modern romance.",
                isFeatured = false,
                isRecent = false,
                isHot = false
            ),
            ContentItemDto(
                rawId = "8",
                title = "The Wandering Earth II Hindi Dubbed",
                slug = "the-wandering-earth-2-hindi-dubbed",
                type = "movie",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi Dub",
                releaseYear = "2024",
                rawRating = "9.2",
                rawViews = "310K",
                rawCategories = "Movies",
                rawTotalEpisodes = 1,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/the-wandering-earth-2-hindi-dubbed",
                description = "Humanity builds enormous engines on the surface of the earth to find a new home in this epic blockbuster.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "9",
                title = "Demon Slayer: Mugen Train Bangla Dubbed",
                slug = "demon-slayer-mugen-train-bangla-dubbed",
                type = "movie",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla Dub",
                releaseYear = "2025",
                rawRating = "9.5",
                rawViews = "450K",
                rawCategories = "Movies",
                rawTotalEpisodes = 1,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/demon-slayer-mugen-train-bangla-dubbed",
                description = "Tanjiro and the Demon Slayer Corps board the Infinity Train to face deadly demons in an unforgettable cinematic battle.",
                isFeatured = false,
                isRecent = false,
                isHot = true
            )
        )
    }

    fun getFallbackWatchDetails(slug: String): WatchDetailResponse {
        val content = getFallbackContents().find { it.slug == slug } ?: getFallbackContents().first()

        val sampleVideos = listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        )

        val totalEp = if (content.totalEpisodes > 0) content.totalEpisodes else 8
        val episodes = (1..totalEp).map { num ->
            val videoUrl = sampleVideos[(num - 1) % sampleVideos.size]
            EpisodeDto(
                rawEpisodeId = "ep_${slug}_$num",
                episodeNumber = num,
                epTitle = "Episode $num",
                seasonNumber = 1,
                duration = "${20 + (num % 5)}m",
                videoUrl = videoUrl,
                embedUrl = "https://byse.sx/e/pdflix_$num",
                isLocked = num > 2,
                adsCount = if (num > 2) 2 else 0
            )
        }

        val servers = listOf(
            ServerDto(
                rawId = "srv_1",
                serverName = "Fast Stream HD (Byse)",
                rawUrl = episodes.first().videoUrl ?: sampleVideos.first(),
                serverType = "mp4"
            ),
            ServerDto(
                rawId = "srv_2",
                serverName = "VIP Ultra Server (Direct HLS)",
                rawUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                serverType = "hls"
            ),
            ServerDto(
                rawId = "srv_3",
                serverName = "Web Embed Player (IFrame)",
                rawUrl = "https://byse.sx/e/drama_stream_demo",
                serverType = "embed"
            )
        )

        return WatchDetailResponse(
            success = true,
            status = 200,
            content = content,
            servers = servers,
            episodes = episodes
        )
    }

    private fun getFallbackNotifications(): List<NotificationItemDto> {
        return listOf(
            NotificationItemDto(
                rawId = "notif_1",
                title = "New Episode Released! 🎉",
                message = "'Overflow Hindi Dubbed' Episode 8 Season Finale is now available to stream in 1080p HD!",
                url = "/overflow-hindi-dubbed-available",
                customTimeAgo = "10m ago"
            ),
            NotificationItemDto(
                rawId = "notif_2",
                title = "Bangla Dubbed Drama Alert 🔥",
                message = "'The Proud Dragon God' Bangla Dubbed all new episodes added. Watch free now!",
                url = "/you-are-my-ultimate-taste-in-the-world-bangla-dubbed",
                customTimeAgo = "1h ago"
            ),
            NotificationItemDto(
                rawId = "notif_3",
                title = "VIP Subscription Offer 👑",
                message = "Get 50% discount on Annual VIP Pass. Enjoy ad-free 4K Ultra streaming with offline downloads.",
                customTimeAgo = "1d ago"
            )
        )
    }
}
