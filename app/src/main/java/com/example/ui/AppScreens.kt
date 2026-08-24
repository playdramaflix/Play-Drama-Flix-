package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.ContentItemDto
import com.example.data.model.NotificationItemDto
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    allContents: List<ContentItemDto>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onContentClick: (ContentItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filteredList = remember(searchQuery, allContents) {
        if (searchQuery.isBlank()) allContents
        else allContents.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.language.contains(searchQuery, ignoreCase = true) ||
            it.categories.any { cat -> cat.contains(searchQuery, ignoreCase = true) } ||
            (it.dubBadge?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search Bangla / Hindi Dubbed Dramas, Anime...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TealAccent)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceVariantDark,
                unfocusedContainerColor = CardBackgroundDark,
                focusedBorderColor = TealAccent,
                unfocusedBorderColor = BorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_text_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Tag Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Bangla", "Hindi", "Anime", "Shorts").forEach { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceVariantDark)
                        .clickable { onSearchQueryChange(tag) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (tag == "Bangla" || tag == "Hindi") "$tag Dub" else tag,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Results (${filteredList.size})",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredList) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onContentClick(item) }
                        .testTag("search_result_${item.id}")
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
                                .data(item.posterUrl)
                                .crossfade(true)
                                .error(R.drawable.img_anime_overflow)
                                .build(),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(bottomStart = 6.dp, topEnd = 12.dp))
                                .background(if (item.dubBadge?.contains("Bangla", ignoreCase = true) == true) BadgeBangla else BadgeHindi)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.dubBadge ?: item.language,
                                color = Color.White,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistHistoryScreen(
    continueWatchingList: List<WatchHistoryEntity>,
    watchlist: List<WatchlistEntity>,
    allContents: List<ContentItemDto>,
    onContentClick: (ContentItemDto) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf("Watchlist") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Toggle Switch: Watchlist / Continue Watching
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSection == "Watchlist") TealAccent else Color.Transparent)
                    .clickable { selectedSection = "Watchlist" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "My Watchlist (${watchlist.size})",
                    color = if (selectedSection == "Watchlist") BackgroundDark else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedSection == "History") TealAccent else Color.Transparent)
                    .clickable { selectedSection = "History" }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Continue Watching (${continueWatchingList.size})",
                    color = if (selectedSection == "History") BackgroundDark else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedSection == "Watchlist") {
            if (watchlist.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Text("Your Watchlist is Empty", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Add your favorite dramas to stream anytime!", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(watchlist) { item ->
                        val matchedContent = allContents.find { it.slug == item.id }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    matchedContent?.let { onContentClick(it) }
                                }
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
                                        .data(item.posterUrl)
                                        .crossfade(true)
                                        .error(R.drawable.img_drama_hidden_love)
                                        .build(),
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .clip(RoundedCornerShape(bottomStart = 6.dp, topEnd = 12.dp))
                                        .background(BadgeBangla)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.dubBadge,
                                        color = Color.White,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            // Continue Watching List
            if (continueWatchingList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Text("No Watch History Yet", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Start watching any drama to save progress automatically.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved Progress", color = TextSecondary, fontSize = 12.sp)
                    TextButton(onClick = onClearHistory) {
                        Text("Clear All", color = Color.Red, fontSize = 12.sp)
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(continueWatchingList) { history ->
                        val matchedContent = allContents.find { it.slug == history.contentSlug }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardBackgroundDark)
                                .clickable {
                                    matchedContent?.let { onContentClick(it) }
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(55.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceVariantDark)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(history.posterUrl)
                                        .crossfade(true)
                                        .error(R.drawable.img_derailment)
                                        .build(),
                                    contentDescription = history.contentTitle,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Resume",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = history.contentTitle,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Episode ${history.episodeNumber} • ${history.episodeTitle}",
                                    color = TealAccent,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Progress Indicator Bar
                                LinearProgressIndicator(
                                    progress = { history.progressPercentage.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = TealAccent,
                                    trackColor = SurfaceVariantDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscribeVipScreen(
    onSubscribeSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Crown Badge
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF282212))
                    .border(2.dp, GoldVip, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "👑", fontSize = 34.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PlayDramaFlix VIP",
                color = GoldVip,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "Unlock all ad-free episodes & 1080p Ultra HD streaming",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Plan 1: Monthly VIP
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(GoldVip, BorderDark))
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("VIP Monthly Pass", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Full catalog access • Instant Ad Skip", color = TextSecondary, fontSize = 11.sp)
                        }
                        Text("৳ 99", color = GoldVip, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onSubscribeSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldVip, contentColor = GoldButtonText),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Subscribe with bKash / Nagad", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Plan 2: Yearly VIP Best Value
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(GoldVip, GoldVip))
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldVip)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("BEST VALUE - 50% OFF", color = GoldButtonText, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("VIP Annual Pass", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("12 Months Unlimited HD Drama Access", color = TextSecondary, fontSize = 11.sp)
                        }
                        Text("৳ 599", color = GoldVip, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onSubscribeSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldVip, contentColor = GoldButtonText),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Get Annual VIP Pass", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItemDto>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Notifications", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications right now", color = TextMuted, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications) { notif ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackgroundDark),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(BorderDark, BorderDark))
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(TealAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = TealAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.timeAgo,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
