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
    suspend fun getWatchDetails(slug: String, fallbackContent: ContentItemDto? = null): Result<WatchDetailResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getWatchDetails(slug)
            if (response.isSuccessful && response.body()?.content != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackWatchDetails(slug, fallbackContent))
            }
        } catch (e: Exception) {
            Log.w("PlayDramaFlixRepo", "Watch details API failed, serving fallback details for slug=$slug: ${e.message}")
            Result.success(getFallbackWatchDetails(slug, fallbackContent))
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
            // --- SHORTS DRAMA (Mini / Vertical Reels) ---
            ContentItemDto(
                rawId = "s1",
                title = "The Proud Dragon God Bangla Dubbed",
                slug = "the-proud-dragon-god-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.4",
                rawViews = "415K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 7,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/the-proud-dragon-god-bangla-dubbed",
                description = "A world-defying warrior descends from sacred peaks to reclaim his family glory in fast-paced vertical mini episodes.",
                isFeatured = true,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "s2",
                title = "Lost In Love Bangla Dubbed",
                slug = "lost-in-love-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.1",
                rawViews = "280K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 9,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787446896_6a8a4670591ea.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787446896_6a8a4670593c3.jpg",
                shareUrl = "https://playdramaflix.com/lost-in-love-bangla-dubbed",
                description = "Rediscovering feelings across time, heartbreak, and sweet unexpected twists in this romantic micro drama.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "s3",
                title = "Doctor Boyfriend Bangla Dubbed",
                slug = "doctor-boyfriend-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.0",
                rawViews = "320K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 12,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/doctor-boyfriend-bangla-dubbed",
                description = "An unexpected hospital encounter leads to a high-voltage romance between a talented surgeon and an ambitious executive.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "s4",
                title = "Waking Up As The Richest Bangla Dubbed",
                slug = "waking-up-as-the-richest-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.2",
                rawViews = "389K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 7,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                shareUrl = "https://playdramaflix.com/waking-up-as-the-richest-bangla-dubbed",
                description = "From broke underling to billionaire heir overnight! Hilarious and thrilling revenge drama reels.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "s5",
                title = "Little Poor Thing Rises by Bearing Children Bangla Dubbed",
                slug = "little-poor-thing-rises-by-bearing-children-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "8.8",
                rawViews = "240K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 8,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787413105_6a89c271df941.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787413105_6a89c271dfab5.webp",
                shareUrl = "https://playdramaflix.com/little-poor-thing-rises-by-bearing-children-bangla-dubbed",
                description = "A strong-willed mother takes back control of an electronics empire and shows the world her real strength.",
                isFeatured = false,
                isRecent = true,
                isHot = false
            ),
            ContentItemDto(
                rawId = "s6",
                title = "Choddobeshi Bhalobasa Bengali Dubbed",
                slug = "choddobeshi-bhalobasa-bengali-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.1",
                rawViews = "310K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 9,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_mafia_mohobbat}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_mafia_mohobbat}",
                shareUrl = "https://playdramaflix.com/choddobeshi-bhalobasa-bengali-dubbed",
                description = "Deepto Play micro drama of unspoken affection, disguise, and redemption.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "s7",
                title = "The Shadow's Counter Attack Bangla Dubbed",
                slug = "the-shadows-counter-attack-bangla-dubbed",
                type = "shorts",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.0",
                rawViews = "275K",
                rawCategories = "Shorts Drama, Bangla Dub",
                rawTotalEpisodes = 7,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                shareUrl = "https://playdramaflix.com/the-shadows-counter-attack-bangla-dubbed",
                description = "In the shadows of the cyber city, an elite agent strikes back against syndicate masters.",
                isFeatured = false,
                isRecent = false,
                isHot = false
            ),

            // --- DRAMA SERIES (Long-form TV/Web Series) ---
            ContentItemDto(
                rawId = "d1",
                title = "Like A Dragon Season 1 Bangla Dubbed",
                slug = "like-a-dragon-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.2",
                rawViews = "560K",
                rawCategories = "Drama Series, Bangla Dub",
                rawTotalEpisodes = 10,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787413105_6a89c271df941.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787413105_6a89c271dfab5.webp",
                shareUrl = "https://playdramaflix.com/like-a-dragon-season-1-bangla-dubbed",
                description = "An explosive story of loyalty, honor, and destiny unfolding in modern Tokyo.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d2",
                title = "Love Is Panacea Hindi Dubbed",
                slug = "love-is-panacea-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2025",
                rawRating = "9.1",
                rawViews = "490K",
                rawCategories = "Drama Series, Hindi Dub",
                rawTotalEpisodes = 11,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787446896_6a8a4670591ea.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787446896_6a8a4670593c3.jpg",
                shareUrl = "https://playdramaflix.com/love-is-panacea-hindi-dubbed",
                description = "A compassionate neurosurgeon and a brilliant medical researcher find solace and romance while fighting rare diseases.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d3",
                title = "Guess Who I Am Hindi Dubbed",
                slug = "guess-who-i-am-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2024",
                rawRating = "9.4",
                rawViews = "890K",
                rawCategories = "Drama Series, Hindi Dub",
                rawTotalEpisodes = 24,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                shareUrl = "https://playdramaflix.com/guess-who-i-am-hindi-dubbed",
                description = "A legendary vigilante woman dedicated to punishing scumbags meets a mysterious corporate heir in an intense cat-and-mouse romance.",
                isFeatured = true,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d4",
                title = "Squid Game Season 2 Bangla Dubbed",
                slug = "squid-game-season-2-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2025",
                rawRating = "9.8",
                rawViews = "1.5M",
                rawCategories = "Drama Series, Bangla Dub, Popular Series",
                rawTotalEpisodes = 9,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/squid-game-season-2-bangla-dubbed",
                description = "Player 456 returns with a fiery resolve as lethal new survival games test morality and trust.",
                isFeatured = true,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d5",
                title = "All of Us Are Dead Season 2 Hindi Dubbed",
                slug = "all-of-us-are-dead-season-2-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2025",
                rawRating = "9.6",
                rawViews = "1.2M",
                rawCategories = "Drama Series, Hindi Dub, Popular Series",
                rawTotalEpisodes = 12,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                shareUrl = "https://playdramaflix.com/all-of-us-are-dead-season-2-hindi-dubbed",
                description = "The battle for survival expands into the quarantined city amidst evolved infected threats.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d6",
                title = "My Demon Season 1 Bangla Dubbed",
                slug = "my-demon-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2025",
                rawRating = "9.5",
                rawViews = "780K",
                rawCategories = "Drama Series, Bangla Dub, Popular Series",
                rawTotalEpisodes = 16,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/my-demon-season-1-bangla-dubbed",
                description = "A 200-year-old demon loses his powers upon meeting an icy chaebol heiress, sparking a contract marriage full of secrets.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d7",
                title = "Hidden Love Season 1 Bangla Dubbed",
                slug = "hidden-love-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2025",
                rawRating = "9.3",
                rawViews = "690K",
                rawCategories = "Drama Series, Bangla Dub, Popular Series",
                rawTotalEpisodes = 25,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787446896_6a8a4670591ea.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787446896_6a8a4670593c3.jpg",
                shareUrl = "https://playdramaflix.com/hidden-love-season-1-bangla-dubbed",
                description = "A sweet, heartwarming tale of a long-held youthful crush turning into a deep and mature love story.",
                isFeatured = false,
                isRecent = false,
                isHot = true
            ),
            ContentItemDto(
                rawId = "d8",
                title = "Derailment Season 1 Hindi Dubbed",
                slug = "derailment-season-1-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2026",
                rawRating = "8.9",
                rawViews = "650K",
                rawCategories = "Drama Series, Hindi Dub",
                rawTotalEpisodes = 30,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_derailment}",
                shareUrl = "https://playdramaflix.com/derailment-season-1-hindi-dubbed",
                description = "A wealthy heiress travels across parallel timelines and meets a childhood confidant who holds the key to her identity.",
                isFeatured = false,
                isRecent = true,
                isHot = false
            ),
            ContentItemDto(
                rawId = "d9",
                title = "Wish Woosh Season 1 Bangla Dubbed",
                slug = "wish-woosh-season-1-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2025",
                rawRating = "8.7",
                rawViews = "190K",
                rawCategories = "Drama Series, Bangla Dub",
                rawTotalEpisodes = 13,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787413105_6a89c271df941.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787413105_6a89c271dfab5.webp",
                shareUrl = "https://playdramaflix.com/wish-woosh-season-1-bangla-dubbed",
                description = "Office romance and everyday workplace magic in a heartwarming romance drama.",
                isFeatured = false,
                isRecent = true,
                isHot = false
            ),
            ContentItemDto(
                rawId = "d10",
                title = "Always Home Hindi Dubbed",
                slug = "always-home-hindi-dubbed",
                type = "series",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2025",
                rawRating = "8.6",
                rawViews = "150K",
                rawCategories = "Drama Series, Hindi Dub",
                rawTotalEpisodes = 3,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/always-home-hindi-dubbed",
                description = "A poignant miniseries exploring friendship, childhood memories, and returning to roots.",
                isFeatured = false,
                isRecent = true,
                isHot = false
            ),
            ContentItemDto(
                rawId = "d11",
                title = "Rohoshyamoyi Bangla Dubbed",
                slug = "rohoshyamoyi-bangla-dubbed",
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2025",
                rawRating = "9.1",
                rawViews = "430K",
                rawCategories = "Drama Series, Bangla Dub",
                rawTotalEpisodes = 36,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_guess_who_i_am}",
                shareUrl = "https://playdramaflix.com/rohoshyamoyi-bangla-dubbed",
                description = "An intricate ancient dynasty intrigue drama filled with deception, secret alliances, and grand battles.",
                isFeatured = false,
                isRecent = false,
                isHot = true
            ),

            // --- ANIME SERIES ---
            ContentItemDto(
                rawId = "a1",
                title = "Overflow Hindi Dubbed Available",
                slug = "overflow-hindi-dubbed-available",
                type = "anime",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2020",
                rawRating = "8.9",
                rawViews = "142.5K",
                rawCategories = "Anime Series, Hindi Dub",
                rawTotalEpisodes = 8,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/overflow-hindi-dubbed-available",
                description = "Watch Overflow Hindi Dubbed online in HD with all episodes available in crystal clear audio.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "a2",
                title = "Solo Leveling Season 1 Hindi Dubbed",
                slug = "solo-leveling-season-1-hindi-dubbed",
                type = "anime",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2024",
                rawRating = "9.7",
                rawViews = "920K",
                rawCategories = "Anime Series, Hindi Dub, Popular Series",
                rawTotalEpisodes = 12,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/solo-leveling-season-1-hindi-dubbed",
                description = "In a world where hunters face monsters, the weakest hunter receives a secret quest system to level up without limits.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "a3",
                title = "Jujutsu Kaisen Season 2 Bangla Dubbed",
                slug = "jujutsu-kaisen-season-2-bangla-dubbed",
                type = "anime",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2024",
                rawRating = "9.6",
                rawViews = "810K",
                rawCategories = "Anime Series, Bangla Dub, Popular Series",
                rawTotalEpisodes = 23,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_mafia_mohobbat}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_mafia_mohobbat}",
                shareUrl = "https://playdramaflix.com/jujutsu-kaisen-season-2-bangla-dubbed",
                description = "The Shibuya incident shatters the jujutsu world in an unforgettable clash of curses and sorcerers.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),

            // --- MOVIES ---
            ContentItemDto(
                rawId = "m1",
                title = "The Wandering Earth II Hindi Dubbed",
                slug = "the-wandering-earth-2-hindi-dubbed",
                type = "movie",
                language = "Hindi Dubbed",
                customDubBadge = "Hindi",
                releaseYear = "2024",
                rawRating = "9.2",
                rawViews = "310K",
                rawCategories = "Movies, Hindi Dub",
                rawTotalEpisodes = 1,
                posterUrl = "https://playdramaflix.com/public/uploads/posters/1787544040_6a8bc1e8f068e.webp",
                bannerUrl = "https://playdramaflix.com/public/uploads/banner/1787544040_6a8bc1e8f1024.webp",
                shareUrl = "https://playdramaflix.com/the-wandering-earth-2-hindi-dubbed",
                description = "Humanity builds enormous planetary engines on the surface of the earth in this epic sci-fi blockbuster.",
                isFeatured = false,
                isRecent = true,
                isHot = true
            ),
            ContentItemDto(
                rawId = "m2",
                title = "Demon Slayer: Mugen Train Bangla Dubbed",
                slug = "demon-slayer-mugen-train-bangla-dubbed",
                type = "movie",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2025",
                rawRating = "9.5",
                rawViews = "450K",
                rawCategories = "Movies, Anime Series, Bangla Dub",
                rawTotalEpisodes = 1,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/demon-slayer-mugen-train-bangla-dubbed",
                description = "Tanjiro and the Demon Slayer Corps board the Infinity Train to face deadly demons in an unforgettable battle.",
                isFeatured = false,
                isRecent = false,
                isHot = true
            )
        )
    }

    fun getFallbackWatchDetails(slug: String, fallbackContent: ContentItemDto? = null): WatchDetailResponse {
        val content = fallbackContent
            ?: getFallbackContents().find { it.slug == slug || it.rawId == slug }
            ?: ContentItemDto(
                rawId = slug,
                title = slug.replace("-", " ").replaceFirstChar { it.uppercase() },
                slug = slug,
                type = "series",
                language = "Bangla Dubbed",
                customDubBadge = "Bangla",
                releaseYear = "2026",
                rawRating = "9.0",
                rawViews = "100K",
                rawCategories = "Drama Series",
                rawTotalEpisodes = 10,
                posterUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                bannerUrl = "android.resource://${context.packageName}/${R.drawable.img_hero_squid_game}",
                shareUrl = "https://playdramaflix.com/$slug",
                description = "Enjoy high quality streaming with full episodes.",
                isFeatured = false,
                isRecent = false,
                isHot = false
            )

        val sampleVideos = listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
        )

        val isMovie = content.type == "movie"
        val totalEp = if (isMovie) 1 else if (content.totalEpisodes > 0) content.totalEpisodes else 8
        
        val episodes = (1..totalEp).map { num ->
            val videoUrl = sampleVideos[(num - 1) % sampleVideos.size]
            val epTitle = if (isMovie) "Full Movie HD" else "${content.title} - Episode $num"
            EpisodeDto(
                rawEpisodeId = "ep_${content.slug}_$num",
                episodeNumber = num,
                epTitle = epTitle,
                seasonNumber = 1,
                duration = if (isMovie) "1h 54m" else "${20 + (num % 5)}m",
                videoUrl = videoUrl,
                embedUrl = "https://byse.sx/e/${content.slug}_ep_$num",
                isLocked = num > 2 && !isMovie,
                adsCount = if (num > 2) 2 else 0
            )
        }

        val servers = listOf(
            ServerDto(
                rawId = "srv_1_${content.slug}",
                serverName = "Fast Stream HD (Byse)",
                rawUrl = episodes.firstOrNull()?.videoUrl ?: sampleVideos.first(),
                serverType = "mp4",
                rawEpisodeId = episodes.firstOrNull()?.episodeId
            ),
            ServerDto(
                rawId = "srv_2_${content.slug}",
                serverName = "VIP Ultra Server (Direct HLS)",
                rawUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8",
                serverType = "hls",
                rawEpisodeId = episodes.firstOrNull()?.episodeId
            ),
            ServerDto(
                rawId = "srv_3_${content.slug}",
                serverName = "Web Embed Player (IFrame)",
                rawUrl = "https://byse.sx/e/${content.slug}_embed",
                serverType = "embed",
                rawEpisodeId = episodes.firstOrNull()?.episodeId
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
