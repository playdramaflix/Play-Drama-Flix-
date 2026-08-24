package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.*
import com.example.ui.*
import com.example.ui.player.ShortsReelsPlayer
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBackgroundDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TealAccent
import com.example.ui.viewmodel.BottomNavTab
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.PlayerDisplayMode
import com.example.ui.viewmodel.UiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PlayDramaFlixApp()
            }
        }
    }
}

@Composable
fun PlayDramaFlixApp(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val contentsState by viewModel.contentsState.collectAsStateWithLifecycle()
    val watchDetailState by viewModel.watchDetailState.collectAsStateWithLifecycle()
    val activeContent by viewModel.activeContent.collectAsStateWithLifecycle()
    val selectedEpisode by viewModel.selectedEpisode.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val playerDisplayMode by viewModel.playerDisplayMode.collectAsStateWithLifecycle()
    val isInWatchlist by viewModel.isCurrentItemInWatchlist.collectAsStateWithLifecycle()
    val continueWatchingList by viewModel.continueWatchingList.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var selectedTopCategory by remember { mutableStateOf("Home") }
    var showNotificationsScreen by remember { mutableStateOf(false) }

    val allContents = when (val state = contentsState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }

    val spotlightDrama = allContents.firstOrNull { it.isFeatured } ?: allContents.firstOrNull()
    val recentlyAdded = allContents.filter { it.isRecent }
    val popularSeries = allContents.sortedByDescending { it.numericViews }
    val shortsDramas = allContents.filter { it.type == "shorts" }
    val dramaSeries = allContents.filter { it.type == "series" }
    val movies = allContents.filter { it.type == "movie" }
    val animeSeries = allContents.filter { it.type == "anime" || it.categories.any { c -> c.contains("Anime", ignoreCase = true) } }

    // System Back Press Handler
    if (showNotificationsScreen) {
        BackHandler { showNotificationsScreen = false }
    } else if (activeContent != null) {
        BackHandler { viewModel.closePlayerOrDetail() }
    } else if (currentTab == BottomNavTab.HOME && selectedTopCategory != "Home" && selectedTopCategory != "All") {
        BackHandler { selectedTopCategory = "Home" }
    } else if (currentTab != BottomNavTab.HOME) {
        BackHandler { viewModel.selectTab(BottomNavTab.HOME) }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (activeContent == null && !showNotificationsScreen) {
                PlayDramaFlixBottomNav(
                    selectedTab = currentTab,
                    onTabSelected = { tab ->
                        viewModel.selectTab(tab)
                    }
                )
            }
        },
        containerColor = BackgroundDark,
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold")
    ) { innerPadding ->
        when {
            showNotificationsScreen -> {
                NotificationsScreen(
                    notifications = notifications,
                    onBack = { showNotificationsScreen = false }
                )
            }

            activeContent != null -> {
                val content = activeContent!!
                val details = (watchDetailState as? UiState.Success)?.data

                if (playerDisplayMode == PlayerDisplayMode.SHORTS_VERTICAL_REEL) {
                    ShortsReelsPlayer(
                        content = content,
                        episodes = details?.episodes ?: emptyList(),
                        onClose = { viewModel.closePlayerOrDetail() },
                        onToggleWatchlist = { viewModel.toggleWatchlistCurrentItem() },
                        isInWatchlist = isInWatchlist,
                        onProgressUpdate = { pos, dur -> viewModel.saveProgress(pos, dur) },
                        onEpisodeLocked = { viewModel.selectTab(BottomNavTab.VIP) }
                    )
                } else {
                    DramaDetailPlayerScreen(
                        content = content,
                        watchDetails = details,
                        selectedEpisode = selectedEpisode,
                        selectedServer = selectedServer,
                        isInWatchlist = isInWatchlist,
                        recommendedContents = allContents.filter { it.slug != content.slug },
                        onBackClick = { viewModel.closePlayerOrDetail() },
                        onSelectEpisode = { ep -> viewModel.selectEpisode(ep) },
                        onSelectServer = { srv -> viewModel.selectServer(srv) },
                        onToggleWatchlist = { viewModel.toggleWatchlistCurrentItem() },
                        onSaveProgress = { pos, dur -> viewModel.saveProgress(pos, dur) },
                        onContentClick = { nextContent -> viewModel.openContentDetail(nextContent) }
                    )
                }
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .background(BackgroundDark)
                ) {
                    when (currentTab) {
                        BottomNavTab.HOME -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                            ) {
                                // 1. YOUKU-Style Top Header & Navigation Bar (Pinned at top)
                                TopNavigationBar(
                                    categories = listOf("Home", "Popular", "Shorts Drama", "Drama Series", "Movies", "Anime Series", "Bangla Dub", "Hindi Dub"),
                                    selectedCategory = selectedTopCategory,
                                    notificationCount = notifications.size,
                                    onCategorySelected = { cat ->
                                        selectedTopCategory = cat
                                    },
                                    onSearchClick = {
                                        viewModel.selectTab(BottomNavTab.SEARCH)
                                    },
                                    onVipClick = {
                                        viewModel.selectTab(BottomNavTab.VIP)
                                    },
                                    onNotificationClick = {
                                        showNotificationsScreen = true
                                    }
                                )

                                // 2. Smooth Animated Category Content Area
                                AnimatedContent(
                                    targetState = selectedTopCategory,
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(200)) + slideInHorizontally(animationSpec = tween(200), initialOffsetX = { 30 }))
                                            .togetherWith(fadeOut(animationSpec = tween(150)))
                                    },
                                    label = "category_screen_transition",
                                    modifier = Modifier.fillMaxSize()
                                ) { targetCategory ->
                                    if (targetCategory.equals("Home", ignoreCase = true) || targetCategory.equals("All", ignoreCase = true)) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            contentPadding = PaddingValues(bottom = 24.dp)
                                        ) {
                                            // Hot Spotlight Hero Banner Card
                                            if (spotlightDrama != null) {
                                                item {
                                                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
                                                        HotSpotlightHeroCard(
                                                            drama = spotlightDrama,
                                                            onWatchClick = {
                                                                viewModel.openContentDetail(spotlightDrama, playImmediately = true)
                                                            },
                                                            onDetailsClick = {
                                                                viewModel.openContentDetail(spotlightDrama, playImmediately = false)
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            // Continue Watching Section (from local Room database!)
                                            if (continueWatchingList.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    SectionHeader(
                                                        title = "Continue Watching",
                                                        onSeeAllClick = { viewModel.selectTab(BottomNavTab.WATCHLIST) }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    androidx.compose.foundation.lazy.LazyRow(
                                                        contentPadding = PaddingValues(horizontal = 14.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        items(continueWatchingList) { item ->
                                                            val contentItem = allContents.find { it.slug == item.contentSlug }
                                                            Column(
                                                                modifier = Modifier
                                                                    .width(140.dp)
                                                                    .clickable {
                                                                        contentItem?.let { viewModel.openContentDetail(it, playImmediately = true) }
                                                                    }
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(84.dp)
                                                                        .clip(RoundedCornerShape(12.dp))
                                                                        .background(CardBackgroundDark)
                                                                ) {
                                                                    AsyncImage(
                                                                        model = ImageRequest.Builder(context)
                                                                            .data(item.posterUrl)
                                                                            .crossfade(true)
                                                                            .error(R.drawable.img_derailment)
                                                                            .build(),
                                                                        contentDescription = item.contentTitle,
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentScale = ContentScale.Crop
                                                                    )

                                                                    Icon(
                                                                        imageVector = Icons.Default.PlayCircle,
                                                                        contentDescription = "Resume",
                                                                        tint = Color.White,
                                                                        modifier = Modifier
                                                                            .size(28.dp)
                                                                            .align(Alignment.Center)
                                                                    )

                                                                    LinearProgressIndicator(
                                                                        progress = { item.progressPercentage.coerceIn(0f, 1f) },
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .height(3.dp)
                                                                            .align(Alignment.BottomCenter),
                                                                        color = TealAccent,
                                                                        trackColor = Color.Black.copy(alpha = 0.5f)
                                                                    )
                                                                }

                                                                Spacer(modifier = Modifier.height(4.dp))

                                                                Text(
                                                                    text = item.contentTitle,
                                                                    color = Color.White,
                                                                    fontSize = 11.sp,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                                Text(
                                                                    text = "EP ${item.episodeNumber}",
                                                                    color = TealAccent,
                                                                    fontSize = 10.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // 1. Recently Added
                                            if (recentlyAdded.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    SectionHeader(
                                                        title = "Recently Added",
                                                        onSeeAllClick = { selectedTopCategory = "Popular" }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDramaRow(
                                                        dramas = recentlyAdded,
                                                        onDramaClick = { drama -> viewModel.openContentDetail(drama) }
                                                    )
                                                }
                                            }

                                            // 2. Popular Series
                                            if (popularSeries.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    SectionHeader(
                                                        title = "Popular Series",
                                                        onSeeAllClick = { selectedTopCategory = "Popular" }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDramaRow(
                                                        dramas = popularSeries,
                                                        onDramaClick = { drama -> viewModel.openContentDetail(drama) }
                                                    )
                                                }
                                            }

                                            // 3. Shorts Drama
                                            if (shortsDramas.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    SectionHeader(
                                                        title = "Shorts Drama",
                                                        onSeeAllClick = { selectedTopCategory = "Shorts Drama" }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDramaRow(
                                                        dramas = shortsDramas,
                                                        onDramaClick = { drama -> viewModel.openContentDetail(drama, playImmediately = true) }
                                                    )
                                                }
                                            }

                                            // 4. Drama Series
                                            if (dramaSeries.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    SectionHeader(
                                                        title = "Drama Series",
                                                        onSeeAllClick = { selectedTopCategory = "Drama Series" }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDramaRow(
                                                        dramas = dramaSeries,
                                                        onDramaClick = { drama -> viewModel.openContentDetail(drama) }
                                                    )
                                                }
                                            }

                                            // 5. Movies
                                            if (movies.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    SectionHeader(
                                                        title = "Movies",
                                                        onSeeAllClick = { selectedTopCategory = "Movies" }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDramaRow(
                                                        dramas = movies,
                                                        onDramaClick = { drama -> viewModel.openContentDetail(drama) }
                                                    )
                                                }
                                            }

                                            // 6. Anime Series
                                            if (animeSeries.isNotEmpty()) {
                                                item {
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    SectionHeader(
                                                        title = "Anime Series",
                                                        onSeeAllClick = { selectedTopCategory = "Anime Series" }
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    HorizontalDramaRow(
                                                        dramas = animeSeries,
                                                        onDramaClick = { drama -> viewModel.openContentDetail(drama) }
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // Dedicated Category Page Screen!
                                        CategoryPageScreen(
                                            categoryName = targetCategory,
                                            allContents = allContents,
                                            onContentClick = { drama -> viewModel.openContentDetail(drama) },
                                            onPlayClick = { drama -> viewModel.openContentDetail(drama, playImmediately = true) }
                                        )
                                    }
                                }
                            }
                        }

                        BottomNavTab.SHORTS -> {
                            val shortsList = shortsDramas.ifEmpty { allContents }
                            val firstShorts = shortsList.firstOrNull()
                            if (firstShorts != null) {
                                val dummyEpisodes = remember(firstShorts.id) {
                                    listOf(
                                        EpisodeDto("ep_1", 1, "Episode 1", videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
                                        EpisodeDto("ep_2", 2, "Episode 2", videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
                                        EpisodeDto("ep_3", 3, "Episode 3", videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4")
                                    )
                                }
                                ShortsReelsPlayer(
                                    content = firstShorts,
                                    episodes = dummyEpisodes,
                                    onClose = { viewModel.selectTab(BottomNavTab.HOME) },
                                    onToggleWatchlist = { viewModel.toggleWatchlistCurrentItem() },
                                    isInWatchlist = isInWatchlist,
                                    onProgressUpdate = { pos, dur -> viewModel.saveProgress(pos, dur) },
                                    onEpisodeLocked = { viewModel.selectTab(BottomNavTab.VIP) }
                                )
                            }
                        }

                        BottomNavTab.SEARCH -> {
                            SearchScreen(
                                allContents = allContents,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onContentClick = { drama -> viewModel.openContentDetail(drama) }
                            )
                        }

                        BottomNavTab.WATCHLIST -> {
                            WatchlistHistoryScreen(
                                continueWatchingList = continueWatchingList,
                                watchlist = watchlist,
                                allContents = allContents,
                                onContentClick = { drama -> viewModel.openContentDetail(drama) },
                                onClearHistory = { viewModel.clearWatchHistory() }
                            )
                        }

                        BottomNavTab.VIP -> {
                            SubscribeVipScreen(
                                onSubscribeSuccess = {
                                    viewModel.selectTab(BottomNavTab.HOME)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
