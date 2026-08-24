package com.example.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentItemDto
import com.example.data.model.EpisodeDto
import com.example.data.model.ServerDto
import com.example.ui.theme.TealAccent

@Composable
fun VideoPlayerContainer(
    content: ContentItemDto,
    episode: EpisodeDto,
    servers: List<ServerDto>,
    selectedServer: ServerDto?,
    onSelectServer: (ServerDto) -> Unit,
    onProgressUpdate: (progressMs: Long, durationMs: Long) -> Unit,
    onNextEpisode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val activeServer = selectedServer ?: servers.find { it.episodeId == episode.episodeId } ?: servers.firstOrNull()
    val mediaUrl = activeServer?.url?.takeIf { it.isNotBlank() } ?: episode.embedUrl ?: episode.videoUrl ?: "https://byse.sx/e/pdflix"
    val isEmbed = activeServer?.type == "embed" || mediaUrl.contains("byse") || mediaUrl.contains("embed") || mediaUrl.contains("/e/") || !mediaUrl.endsWith(".mp4") && !mediaUrl.endsWith(".m3u8")

    Column(modifier = modifier.fillMaxWidth()) {
        // Main Player Aspect Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color.Black)
        ) {
            if (isEmbed) {
                WebEmbedPlayer(
                    embedUrl = mediaUrl,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ExoPlayerView(
                    videoUrl = mediaUrl,
                    title = content.title,
                    episodeTitle = episode.epTitle,
                    onProgressUpdate = onProgressUpdate,
                    onNextEpisode = onNextEpisode,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Server Switching Pills Row
        if (servers.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F111A))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = "Servers",
                    tint = TealAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Server:",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(servers) { server ->
                        val isSelected = server.id == (activeServer?.id ?: "")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) TealAccent else Color(0xFF1E2230)
                                )
                                .clickable { onSelectServer(server) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = server.name,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
