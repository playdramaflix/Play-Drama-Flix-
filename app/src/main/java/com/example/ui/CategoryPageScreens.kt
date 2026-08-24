package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.ContentItemDto
import com.example.ui.theme.*

@Composable
fun CategoryPageScreen(
    categoryName: String,
    allContents: List<ContentItemDto>,
    onContentClick: (ContentItemDto) -> Unit,
    onPlayClick: (ContentItemDto) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter contents strictly for selected category to ensure no cross-category bleeding
    val categoryContents = remember(categoryName, allContents) {
        when {
            categoryName.contains("Popular", ignoreCase = true) -> {
                allContents.sortedByDescending { it.numericViews }
            }
            categoryName.contains("Shorts", ignoreCase = true) -> {
                allContents.filter { it.type == "shorts" }
            }
            categoryName.contains("Drama", ignoreCase = true) -> {
                allContents.filter { it.type == "series" }
            }
            categoryName.contains("Movie", ignoreCase = true) -> {
                allContents.filter { it.type == "movie" }
            }
            categoryName.contains("Anime", ignoreCase = true) -> {
                allContents.filter { it.type == "anime" || it.categories.any { c -> c.contains("Anime", ignoreCase = true) } }
            }
            categoryName.contains("Bangla", ignoreCase = true) -> {
                allContents.filter { 
                    (it.language.contains("Bangla", ignoreCase = true) ||
                     it.dubBadge?.contains("Bangla", ignoreCase = true) == true ||
                     it.title.contains("Bangla", ignoreCase = true) ||
                     it.title.contains("Bengali", ignoreCase = true)) &&
                    !it.language.startsWith("Hindi", ignoreCase = true)
                }
            }
            categoryName.contains("Hindi", ignoreCase = true) -> {
                allContents.filter { 
                    (it.language.contains("Hindi", ignoreCase = true) ||
                     it.dubBadge?.contains("Hindi", ignoreCase = true) == true ||
                     it.title.contains("Hindi", ignoreCase = true)) &&
                    !it.language.startsWith("Bangla", ignoreCase = true)
                }
            }
            else -> allContents
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(start = 5.dp, end = 5.dp, top = 4.dp, bottom = 80.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("category_grid_${categoryName.replace(" ", "_").lowercase()}")
    ) {
        if (categoryContents.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No shows available in $categoryName",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            items(categoryContents) { drama ->
                CategoryGridPosterCard(
                    drama = drama,
                    onClick = { onContentClick(drama) }
                )
            }
        }
    }
}

/**
 * Exact Match Card Component matching the reference screenshot:
 * - 3-column tall vertical poster (0.58 aspect ratio) with 4dp corner radius
 * - Warm Golden-Amber badge at top right ("Bangla" or "Hindi")
 * - White "X Episodes" at bottom-left over soft gradient
 * - Clean title beneath poster with tight spacing
 */
@Composable
fun CategoryGridPosterCard(
    drama: ContentItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val badgeLabel = when {
        drama.dubBadge?.contains("Bangla", ignoreCase = true) == true || drama.language.contains("Bangla", ignoreCase = true) -> "Bangla"
        drama.dubBadge?.contains("Hindi", ignoreCase = true) == true || drama.language.contains("Hindi", ignoreCase = true) -> "Hindi"
        else -> "Bangla"
    }

    val episodesLabel = when {
        drama.type == "movie" -> "Movie"
        drama.totalEpisodes > 0 -> "${drama.totalEpisodes} Episodes"
        else -> "Full Episode"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("category_item_${drama.id}")
    ) {
        // Poster Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.58f)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBackgroundDark)
                .border(0.5.dp, Color(0xFF1E2433).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
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

            // Bottom Gradient for Episode Text Readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Top-Right Golden-Amber Tag ("Bangla" / "Hindi") matching screenshot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, topEnd = 4.dp))
                    .background(Color(0xFFEAA61A))
                    .padding(horizontal = 4.5.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = badgeLabel,
                    color = Color(0xFF111111),
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                )
            }

            // Bottom-Left "X Episodes" Text
            Text(
                text = episodesLabel,
                color = Color.White,
                fontSize = 7.8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.5.dp))

        // Title below Poster
        Text(
            text = drama.title,
            color = Color(0xFFD4D8E2),
            fontSize = 8.8.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            lineHeight = 12.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}
