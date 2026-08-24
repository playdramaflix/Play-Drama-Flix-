package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.*
import com.example.ui.player.VideoPlayerContainer
import com.example.ui.theme.*

@Composable
fun DramaDetailPlayerScreen(
    content: ContentItemDto,
    watchDetails: WatchDetailResponse?,
    selectedEpisode: EpisodeDto?,
    selectedServer: ServerDto?,
    isInWatchlist: Boolean,
    recommendedContents: List<ContentItemDto>,
    onBackClick: () -> Unit,
    onSelectEpisode: (EpisodeDto) -> Unit,
    onSelectServer: (ServerDto) -> Unit,
    onToggleWatchlist: () -> Unit,
    onSaveProgress: (progressMs: Long, totalDurationMs: Long) -> Unit,
    onContentClick: (ContentItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(1420) }
    var isSynopsisExpanded by remember { mutableStateOf(false) }
    var showLockedAdDialog by remember { mutableStateOf<EpisodeDto?>(null) }

    val episodes = watchDetails?.episodes ?: emptyList()
    val servers = watchDetails?.servers ?: emptyList()
    val currentEpisode = selectedEpisode ?: episodes.firstOrNull() ?: EpisodeDto("ep_1", 1, "Episode 1")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Text(
                text = "playdramaflix.com",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = TextPrimary
                )
            }
        }

        // Main Vertical Content Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. High Performance Media3 / WebEmbed Video Player
            item(span = { GridItemSpan(3) }) {
                VideoPlayerContainer(
                    content = content,
                    episode = currentEpisode,
                    servers = servers,
                    selectedServer = selectedServer,
                    onSelectServer = onSelectServer,
                    onProgressUpdate = onSaveProgress,
                    onNextEpisode = {
                        val nextEpNum = currentEpisode.episodeNumber + 1
                        val nextEp = episodes.find { it.episodeNumber == nextEpNum }
                        if (nextEp != null && !nextEp.isLocked) {
                            onSelectEpisode(nextEp)
                        }
                    }
                )
            }

            // 2. Title & Navigation Meta (Prev / Next Buttons & Social Bar)
            item(span = { GridItemSpan(3) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = content.title,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Prev / Next Navigation
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackgroundDark)
                                    .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val prevEp = episodes.find { it.episodeNumber == currentEpisode.episodeNumber - 1 }
                                        if (prevEp != null) onSelectEpisode(prevEp)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = "Previous",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Pre",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackgroundDark)
                                    .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
                                    .clickable {
                                        val nextEp = episodes.find { it.episodeNumber == currentEpisode.episodeNumber + 1 }
                                        if (nextEp != null) {
                                            if (nextEp.isLocked) showLockedAdDialog = nextEp
                                            else onSelectEpisode(nextEp)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Next",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = "Next",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Meta Chips + Social Reaction Counter Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Year pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceVariantDark)
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = GoldVip,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = content.releaseYear,
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Rating pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF282415))
                                    .border(0.8.dp, GoldVip.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldVip,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = content.rating.toString(),
                                        color = GoldVip,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // ...more synopsis toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2A2210))
                                    .clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isSynopsisExpanded) "less" else "...more",
                                    color = GoldVip,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Right Reaction Buttons: [👍] [🔗] [💬] [📑 Watchlist]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Like
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CardBackgroundDark)
                                    .clickable {
                                        isLiked = !isLiked
                                        likesCount += if (isLiked) 1 else -1
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isLiked) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = "Like",
                                        tint = if (isLiked) TealAccent else TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "$likesCount",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Share
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CardBackgroundDark)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = "Share",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "3.2K",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Watchlist Bookmark Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isInWatchlist) GoldVip.copy(alpha = 0.2f) else Color(0xFF282415))
                                    .border(1.dp, if (isInWatchlist) GoldVip else BorderDark, RoundedCornerShape(6.dp))
                                    .clickable { onToggleWatchlist() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Bookmark List",
                                    tint = GoldVip,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Expandable Synopsis
                    AnimatedVisibility(visible = isSynopsisExpanded) {
                        Text(
                            text = content.synopsis ?: "Watch all episodes of ${content.title} on PlayDramaFlix.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // 3. Episode Horizontal Scroll Pills (Full Span)
            item(span = { GridItemSpan(3) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    episodes.forEach { episode ->
                        val isSelected = episode.episodeNumber == currentEpisode.episodeNumber

                        if (isSelected) {
                            // Active Teal Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TealAccent)
                                    .clickable { onSelectEpisode(episode) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("active_episode_pill_${episode.episodeNumber}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "EP ${episode.episodeNumber}",
                                        color = BackgroundDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    EqualizerWaveform()
                                }
                            }
                        } else if (episode.isLocked) {
                            // Locked Episode Pill with Ads Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackgroundDark)
                                    .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { showLockedAdDialog = episode }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("locked_episode_pill_${episode.episodeNumber}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "EP ${episode.episodeNumber}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GoldVip)
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = GoldButtonText,
                                                modifier = Modifier.size(9.dp)
                                            )
                                            Text(
                                                text = "${episode.adsCount} Ads",
                                                color = GoldButtonText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Unlocked Episode Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardBackgroundDark)
                                    .border(0.8.dp, BorderDark, RoundedCornerShape(8.dp))
                                    .clickable { onSelectEpisode(episode) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("episode_pill_${episode.episodeNumber}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "EP ${episode.episodeNumber}",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = TextMuted,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Divider Line
            item(span = { GridItemSpan(3) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderDark)
                        .padding(horizontal = 14.dp)
                )
            }

            // 4. Recommended Header
            item(span = { GridItemSpan(3) }) {
                Text(
                    text = "Recommended for You",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // 5. Recommended Dramas Grid (3 Columns)
            items(recommendedContents) { recItem ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clickable { onContentClick(recItem) }
                        .testTag("recommended_item_${recItem.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.68f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackgroundDark)
                            .border(0.8.dp, BorderDark, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(recItem.posterUrl)
                                .crossfade(true)
                                .error(R.drawable.img_guess_who_i_am)
                                .build(),
                            contentDescription = recItem.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Bottom gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                        startY = 100f
                                    )
                                )
                        )

                        // Dub Badge Top-Right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 6.dp, topEnd = 12.dp))
                                .background(if (recItem.dubBadge?.contains("Bangla", ignoreCase = true) == true) BadgeBangla else BadgeHindi)
                                .padding(horizontal = 5.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = recItem.dubBadge ?: recItem.language,
                                color = Color.White,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Bottom Episodes Count
                        Text(
                            text = "${recItem.totalEpisodes} Episodes",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = recItem.title,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        lineHeight = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Ad Unlock Dialog for Locked Episodes
    showLockedAdDialog?.let { ep ->
        AlertDialog(
            onDismissRequest = { showLockedAdDialog = null },
            containerColor = SurfaceDark,
            title = {
                Text(text = "Unlock Episode ${ep.episodeNumber}", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Watch ${ep.adsCount} short sponsored ads or upgrade to VIP to unlock this episode immediately.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectEpisode(ep)
                        showLockedAdDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldVip, contentColor = GoldButtonText)
                ) {
                    Text(text = "Watch Ads & Play", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockedAdDialog = null }) {
                    Text(text = "Cancel", color = TextMuted)
                }
            }
        )
    }
}
