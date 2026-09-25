package com.revamine.games.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.revamine.games.data.GameItem
import com.revamine.games.ui.components.CategoryChip
import com.revamine.games.ui.components.GameCard
import com.revamine.games.ui.components.GamesShimmerGrid
import com.revamine.games.ui.theme.RevaAmberAccent
import com.revamine.games.ui.theme.RevaEmeraldAccent
import com.revamine.games.ui.theme.RevaFeaturedRose
import com.revamine.games.ui.theme.RevaIndigoLight
import com.revamine.games.ui.theme.RevaIndigoPrimary
import com.revamine.games.ui.theme.RevaRoseAccent
import com.revamine.games.ui.theme.RevaTealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    games: List<GameItem>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    favorites: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onPlayGame: (GameItem) -> Unit,
    onToggleFavorite: (GameItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize()
    ) {
        if (isLoading && games.isEmpty()) {
            GamesShimmerGrid()
        } else if (games.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        "No games available",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Check your network connection and pull down to refresh.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Button(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Text(" Retry", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        } else {
            // Dynamic featured game from Sheet / API
            val featured = games.firstOrNull { it.featured } ?: games.firstOrNull()

            val filtered = games.filter { game ->
                selectedCategory == "all" ||
                    game.category.equals(selectedCategory, ignoreCase = true)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Featured Hero Banner
                if (featured != null && selectedCategory == "all") {
                    item(span = { GridItemSpan(2) }) {
                        HeroBanner(game = featured, onClick = { onPlayGame(featured) })
                    }
                }

                // 2. Category Filter Pills (Website style horizontal scrolling)
                item(span = { GridItemSpan(2) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(categories) { catKey ->
                            val label = if (catKey == "all") "All Games" else catKey.replaceFirstChar { it.uppercase() }
                            CategoryChip(
                                label = label,
                                selected = catKey.equals(selectedCategory, ignoreCase = true),
                                onClick = { onCategorySelected(catKey) },
                                categoryKey = catKey
                            )
                        }
                    }
                }

                // 3. 2-Column Game Cards Grid
                items(filtered, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        isFavorite = favorites.contains(game.id),
                        onClick = { onPlayGame(game) },
                        onToggleFavorite = { onToggleFavorite(game) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBanner(game: GameItem, onClick: () -> Unit) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.78f)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(game.coverUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Multi-stop gradient: leaves the center bright and vivid, darkens top for badges and bottom for text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Black.copy(alpha = 0.50f),
                                0.22f to Color.Transparent,
                                0.55f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )

            // Top Badges Row (FEATURED GAME + Sheet Badge)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FEATURED GAME Pill
                Surface(
                    color = RevaFeaturedRose,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "FEATURED GAME",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }

            // Bottom Content: Title & Tagline on Left, "Play Now" Button on Right
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = game.title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = game.tagline,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Glowing Rose-Red "Play Now" Pill Button
                Surface(
                    onClick = onClick,
                    color = RevaRoseAccent,
                    shape = RoundedCornerShape(50),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Play Now",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
