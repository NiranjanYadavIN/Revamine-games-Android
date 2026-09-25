package com.revamine.games.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.revamine.games.data.GameItem
import com.revamine.games.ui.theme.RevaAmberAccent
import com.revamine.games.ui.theme.RevaEmeraldAccent
import com.revamine.games.ui.theme.RevaIndigoPrimary
import com.revamine.games.ui.theme.RevaRoseAccent
import com.revamine.games.ui.theme.RevaTealAccent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameCard(
    game: GameItem,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleFavorite()
                    val msg = if (!isFavorite) {
                        "Added \"${game.shortTitle}\" to Favorites ❤️"
                    } else {
                        "Removed \"${game.shortTitle}\" from Favorites"
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            ),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1:1 Square Cover Image (100% clean & open, zero buttons covering the artwork)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            ) {
                AsyncImage(
                    model = remember(game.coverUrl) {
                        ImageRequest.Builder(context)
                            .data(game.coverUrl)
                            .crossfade(true)
                            .build()
                    },
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Sleek, Ultra-Slim Compact Dynamic Badge (Reduced height, tanik matra)
                if (!game.badge.isNullOrBlank()) {
                    val badgeBg = getBadgeColor(game.badge, game.badgeColor)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 5.dp, top = 5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeBg)
                            .padding(horizontal = 4.5.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = game.badge.uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 7.5.sp,
                            lineHeight = 8.sp,
                            letterSpacing = 0.3.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // Centered Bold Game Title (Exact website style)
            Text(
                text = game.shortTitle,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp)
            )
        }
    }
}

private fun getBadgeColor(badge: String, badgeColor: String?): Color {
    val b = badge.uppercase().trim()
    val c = badgeColor?.lowercase() ?: ""
    return when {
        b.contains("TRENDING") || c.contains("teal") || c.contains("cyan") -> RevaTealAccent
        b.contains("POPULAR") || c.contains("emerald") || c.contains("green") -> RevaEmeraldAccent
        b.contains("NEW") || c.contains("amber") || c.contains("yellow") -> RevaAmberAccent
        b.contains("HOT") || b.contains("FLAGSHIP") || c.contains("rose") || c.contains("red") -> RevaRoseAccent
        else -> RevaIndigoPrimary
    }
}
