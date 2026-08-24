package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ContentItemDto
import com.example.ui.theme.*
import com.example.ui.viewmodel.BottomNavTab

@Composable
fun TopNavigationBar(
    categories: List<String>,
    selectedCategory: String,
    notificationCount: Int = 3,
    onCategorySelected: (String) -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 10.dp, start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontal Scrollable Category Pills
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory

                when (category) {
                    "VIP" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF1E1A14))
                                .border(1.2.dp, GoldVip, RoundedCornerShape(50))
                                .clickable { onCategorySelected("VIP") }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("top_vip_pill"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "👑", fontSize = 12.sp)
                                Text(
                                    text = "VIP",
                                    color = GoldVip,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    "All" -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) TealAccent else SurfaceVariantDark.copy(alpha = 0.7f))
                                .border(1.dp, if (isSelected) TealAccent else BorderDark, RoundedCornerShape(50))
                                .clickable { onCategorySelected("All") }
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                                .testTag("top_all_pill"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "All",
                                color = if (isSelected) BackgroundDark else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) TealAccent else SurfaceVariantDark.copy(alpha = 0.7f))
                                .border(1.dp, if (isSelected) TealAccent else BorderDark, RoundedCornerShape(50))
                                .clickable { onCategorySelected(category) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("top_category_pill_$category"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) BackgroundDark else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Notification Bell Icon with Badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(SurfaceVariantDark.copy(alpha = 0.7f))
                .border(1.dp, BorderDark, CircleShape)
                .clickable { onNotificationClick() }
                .testTag("notification_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notifications",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )

            if (notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(NotificationBadgeRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = notificationCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HotSpotlightHeroCard(
    drama: ContentItemDto,
    onWatchClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1B2338),
                        Color(0xFF121522),
                        Color(0xFF0D0F17)
                    )
                )
            )
            .border(1.dp, BorderDark, RoundedCornerShape(24.dp))
            .padding(14.dp)
            .testTag("hot_spotlight_hero")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    // Badge Chips (HOT SPOTLIGHT, Release Year, Rating)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldVip)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "HOT SPOTLIGHT",
                                color = GoldButtonText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceVariantDark)
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = drama.releaseYear,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

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
                                    text = drama.rating.toString(),
                                    color = GoldVip,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = drama.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Tags
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (listOf(drama.dubBadge ?: drama.language) + drama.categories.take(2)).forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceVariantDark.copy(alpha = 0.8f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tag,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (Watch Now & Details)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onWatchClick,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldVip,
                                contentColor = GoldButtonText
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("spotlight_watch_now")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = GoldButtonText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Watch Now",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Button(
                            onClick = onDetailsClick,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceVariantDark,
                                contentColor = TextPrimary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.linearGradient(listOf(BorderDark, BorderDark))
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("spotlight_details")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Details",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Right Side Poster
                Box(
                    modifier = Modifier
                        .width(108.dp)
                        .height(152.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.2.dp, GoldVip.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable { onWatchClick() }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(drama.posterUrl)
                            .crossfade(true)
                            .error(R.drawable.img_anime_overflow)
                            .build(),
                        contentDescription = drama.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Carousel Dots Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF388BFF))
                )
                Spacer(modifier = Modifier.width(4.dp))
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(SurfaceVariantDark)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SectionRed)
            )
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.clickable { onSeeAllClick() }
        ) {
            Text(
                text = "See All",
                color = SectionRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SectionRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun HorizontalDramaRow(
    dramas: List<ContentItemDto>,
    onDramaClick: (ContentItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(dramas) { drama ->
            DramaPosterCardHorizontal(
                drama = drama,
                onClick = { onDramaClick(drama) }
            )
        }
    }
}

@Composable
fun DramaPosterCardHorizontal(
    drama: ContentItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .width(128.dp)
            .clickable { onClick() }
            .testTag("drama_item_${drama.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackgroundDark)
                .border(0.8.dp, BorderDark, RoundedCornerShape(16.dp))
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(drama.posterUrl)
                    .crossfade(true)
                    .error(R.drawable.img_guess_who_i_am)
                    .build(),
                contentDescription = drama.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Dark gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 180f
                        )
                    )
            )

            // Top Right Corner Tag: [Hindi] or [Bangla]
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 8.dp, topEnd = 16.dp))
                    .background(if (drama.dubBadge?.contains("Bangla", ignoreCase = true) == true) BadgeBangla else BadgeHindi)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = drama.dubBadge ?: drama.language,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Cyan RECENTLY ADDED Pill (if applicable)
            if (drama.isRecent) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF00A2FF))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RECENTLY ADDED",
                        color = Color.White,
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Bottom Left Episodes Count
            Text(
                text = "${drama.totalEpisodes} Episodes",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = drama.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Composable
fun PlayDramaFlixBottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0E1017),
        modifier = modifier
            .fillMaxWidth()
            .border(width = 0.8.dp, color = BorderDark)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.values().forEach { tab ->
                val isSelected = tab == selectedTab

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .testTag("bottom_nav_${tab.name.lowercase()}")
                ) {
                    when (tab) {
                        BottomNavTab.HOME -> {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = tab.label,
                                tint = if (isSelected) TealAccent else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavTab.SHORTS -> {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = tab.label,
                                tint = if (isSelected) TealAccent else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavTab.SEARCH -> {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = tab.label,
                                tint = if (isSelected) TealAccent else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavTab.WATCHLIST -> {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkBorder,
                                contentDescription = tab.label,
                                tint = if (isSelected) TealAccent else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        BottomNavTab.VIP -> {
                            Text(
                                text = "👑",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = tab.label,
                        color = when {
                            tab == BottomNavTab.VIP -> GoldVip
                            isSelected -> TealAccent
                            else -> TextMuted
                        },
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || tab == BottomNavTab.VIP) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Waveform equalizer animation
@Composable
fun EqualizerWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = modifier.height(14.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(bar1)
                .background(Color.White, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(bar2)
                .background(Color.White, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(bar3)
                .background(Color.White, RoundedCornerShape(1.dp))
        )
    }
}
