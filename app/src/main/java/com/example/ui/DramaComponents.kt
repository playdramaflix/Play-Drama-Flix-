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
fun VipCrownBadge(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(width = 38.dp, height = 30.dp)
            .clickable { onClick() }
            .testTag("top_vip_icon_button"),
        contentAlignment = Alignment.Center
    ) {
        // High-fidelity Custom Vector Crown with 3 Jeweled points and Golden gradient
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Center jewel, left jewel, right jewel coordinates
            val leftJewelX = w * 0.17f
            val leftJewelY = h * 0.40f
            val leftJewelRadius = w * 0.14f

            val centerJewelX = w * 0.50f
            val centerJewelY = h * 0.22f
            val centerJewelRadius = w * 0.16f

            val rightJewelX = w * 0.83f
            val rightJewelY = h * 0.40f
            val rightJewelRadius = w * 0.14f

            // Crown Base Path
            val crownPath = androidx.compose.ui.graphics.Path().apply {
                // Bottom left rounded corner start
                moveTo(w * 0.22f, h * 0.90f)
                // Bottom horizontal line with subtle curve
                quadraticTo(w * 0.50f, h * 0.94f, w * 0.78f, h * 0.90f)
                // Bottom right corner up to right jewel
                quadraticTo(w * 0.84f, h * 0.85f, rightJewelX, rightJewelY)
                // Right valley dipping in towards center
                quadraticTo(w * 0.68f, h * 0.54f, centerJewelX, centerJewelY)
                // Left valley dipping in towards left
                quadraticTo(w * 0.32f, h * 0.54f, leftJewelX, leftJewelY)
                // Left wall down to bottom left
                quadraticTo(w * 0.16f, h * 0.85f, w * 0.22f, h * 0.90f)
                close()
            }

            // Outer Golden Glow Border
            drawPath(
                path = crownPath,
                color = Color(0xFFFFEB3B),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
            )

            // Crown Body Gradient Fill (Bright Golden Yellow -> Rich Warm Orange)
            drawPath(
                path = crownPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFD54F),
                        Color(0xFFFFB300),
                        Color(0xFFFF8F00)
                    )
                )
            )

            // Inner Highlight line for 3D look
            val innerPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.24f, h * 0.52f)
                quadraticTo(w * 0.50f, h * 0.36f, w * 0.76f, h * 0.52f)
            }
            drawPath(
                path = innerPath,
                color = Color(0x66FFFFFF),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
            )

            // Helper to draw jeweled peaks (Outer yellow rim + Inner vibrant red gem)
            fun drawJewel(cx: Float, cy: Float, radius: Float) {
                // Outer Yellow Ring
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = radius,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                // Inner Coral-Red Gem
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFF5252), Color(0xFFE53935)),
                        center = androidx.compose.ui.geometry.Offset(cx - radius * 0.2f, cy - radius * 0.2f),
                        radius = radius * 0.75f
                    ),
                    radius = radius * 0.72f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy)
                )
                // Gem glint shine
                drawCircle(
                    color = Color(0x99FFFFFF),
                    radius = radius * 0.22f,
                    center = androidx.compose.ui.geometry.Offset(cx - radius * 0.28f, cy - radius * 0.28f)
                )
            }

            // Draw Left, Center, Right Jewels
            drawJewel(leftJewelX, leftJewelY, leftJewelRadius)
            drawJewel(centerJewelX, centerJewelY, centerJewelRadius)
            drawJewel(rightJewelX, rightJewelY, rightJewelRadius)
        }

        // Bold Italic "VIP" Text centered on crown body
        Text(
            text = "VIP",
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0x88000000),
                    offset = androidx.compose.ui.geometry.Offset(1.5f, 1.5f),
                    blurRadius = 2f
                )
            ),
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-3.5).dp)
        )
    }
}

@Composable
fun TopNavigationBar(
    categories: List<String>,
    selectedCategory: String,
    notificationCount: Int = 3,
    onCategorySelected: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onVipClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFA0D0F14),
                        Color(0xD90D0F14),
                        Color(0x000D0F14)
                    )
                )
            )
            .padding(top = 0.dp, bottom = 2.dp)
    ) {
        // TOP ROW: Compact Name (PDFlix) + Search Pill + Custom VIP Crown + Notification Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Compact Brand Title: PDFlix
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onCategorySelected("Home") }
                    .testTag("app_brand_logo")
                    .padding(end = 4.dp)
            ) {
                Text(
                    text = "PD",
                    color = Color(0xFF00D2FF),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Flix",
                    color = Color(0xFFFF9900),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Rounded Search Pill Bar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x2EFFFFFF))
                    .border(0.8.dp, Color(0x1FFFFFFF), RoundedCornerShape(50))
                    .clickable { onSearchClick() }
                    .padding(horizontal = 12.dp)
                    .testTag("top_search_bar_pill"),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Search show...",
                        color = Color(0xFFADB2BE),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFCCD0DB),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(7.dp))

            // Exact Custom 3D VIP Crown Button
            VipCrownBadge(
                onClick = onVipClick
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Notification Bell Icon with Badge Counter (Compact)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x2EFFFFFF))
                    .border(0.8.dp, Color(0x1FFFFFFF), CircleShape)
                    .clickable { onNotificationClick() }
                    .testTag("top_notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )

                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 1.dp, y = (-1).dp)
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2A4B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                            color = Color.White,
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // SECOND ROW: Horizontal Category Tabs (Home, Shorts, Drama, Anime, Movie, Variety, Kids, Doc)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabList = if (categories.contains("Home") || categories.contains("All")) {
                categories.map { if (it == "All") "Home" else it }
            } else {
                listOf("Home", "Shorts", "Drama", "Anime", "Movie", "Variety", "Kids", "Doc")
            }

            tabList.forEach { tab ->
                val isSelected = (tab == "Home" && (selectedCategory == "Home" || selectedCategory == "All")) ||
                                 tab.equals(selectedCategory, ignoreCase = true)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onCategorySelected(if (tab == "Home") "All" else tab) }
                        .padding(vertical = 2.dp)
                        .testTag("nav_tab_$tab")
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else Color(0x99FFFFFF),
                        fontSize = if (isSelected) 17.sp else 14.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = if (isSelected) 0.sp else 0.2.sp
                    )

                    Spacer(modifier = Modifier.height(2.5.dp))

                    // Sleek Active Indicator Underline
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.5.dp))
                    }
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

            // Bottom Bar: Episodes Count (Left) & Real Views Count (Right)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${drama.totalEpisodes} Ep",
                    color = Color(0xFFE2E4EB),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = drama.viewsDisplay,
                        color = Color(0xFFFFD54F),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
