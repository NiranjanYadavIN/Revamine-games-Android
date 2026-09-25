package com.revamine.games.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FiberNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.revamine.games.ui.components.GameCard
import com.revamine.games.ui.theme.RevaAmberAccent
import com.revamine.games.ui.theme.RevaEmeraldAccent
import com.revamine.games.ui.theme.RevaIndigoLight
import com.revamine.games.ui.theme.RevaIndigoPrimary
import com.revamine.games.ui.theme.RevaRoseAccent
import com.revamine.games.ui.theme.RevaTealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    games: List<GameItem>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    favorites: Set<String>,
    selectedCategory: String,
    categories: List<String>,
    onCategorySelected: (String) -> Unit,
    onPlayGame: (GameItem) -> Unit,
    onToggleFavorite: (GameItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullToRefreshState = rememberPullToRefreshState()

    // Local active category selection for smooth drilling down into genres
    var activeCategory by remember { mutableStateOf<String?>(null) }

    // If user presses system back while inside a category view, return to hub
    if (activeCategory != null) {
        BackHandler {
            activeCategory = null
        }
    }

    // Identify newly released games (badge is NEW or newest order)
    val newGames = remember(games) {
        val taggedNew = games.filter { it.badge?.contains("new", ignoreCase = true) == true }
        if (taggedNew.isNotEmpty()) taggedNew else games.takeLast(6).reversed()
    }

    // Filter actual distinct categories (excluding "all")
    val actualCategories = remember(categories, games) {
        val distinctKeys = games.map { it.category.trim().lowercase() }.distinct().filter { it.isNotBlank() }
        if (distinctKeys.isNotEmpty()) distinctKeys else categories.filter { it != "all" }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = pullToRefreshState,
        modifier = modifier.fillMaxSize()
    ) {
        AnimatedContent(
            targetState = activeCategory,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "category_transition"
        ) { categoryKey ->
            if (categoryKey == null) {
                // ================== CATEGORY HUB VIEW ==================
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Newly Released Games Spotlight Section (Directly boosts new drops!)
                    if (newGames.isNotEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    Surface(
                                        color = RevaAmberAccent.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.RocketLaunch,
                                            contentDescription = null,
                                            tint = RevaAmberAccent,
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Fresh Drops & New Releases",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.5.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Newly added games – try them first!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Horizontal Reel of New Games
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(newGames, key = { "new_${it.id}" }) { game ->
                                        NewReleaseCard(
                                            game = game,
                                            onClick = { onPlayGame(game) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                            }
                        }
                    }

                    // 2. Genre Categories Grid Header
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            Surface(
                                color = RevaIndigoPrimary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Widgets,
                                    contentDescription = null,
                                    tint = RevaIndigoLight,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Explore by Genre",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Find games matching your playstyle",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 3. Rich Category Hub Cards (Play Store / Steam style)
                    items(actualCategories, key = { "hub_$it" }) { catKey ->
                        val catGames = games.filter { it.category.equals(catKey, ignoreCase = true) }
                        CategoryHubCard(
                            categoryKey = catKey,
                            gameCount = catGames.size,
                            previewGames = catGames.take(3),
                            onClick = { activeCategory = catKey }
                        )
                    }
                }
            } else {
                // ================== CATEGORY DETAIL VIEW ==================
                val categoryGames = games.filter { it.category.equals(categoryKey, ignoreCase = true) }
                val formattedTitle = categoryKey.replaceFirstChar { it.uppercase() }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header with Back Button
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            IconButton(
                                onClick = { activeCategory = null },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to categories",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Column {
                                Text(
                                    text = "$formattedTitle Games",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${categoryGames.size} games available",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Games Grid in this Category
                    items(categoryGames, key = { it.id }) { game ->
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
}

/**
 * High-appeal card for newly released games in the Category feed
 */
@Composable
private fun NewReleaseCard(
    game: GameItem,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.width(135.dp)
    ) {
        Column(
            modifier = Modifier.padding(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
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
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = game.shortTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = game.category.replaceFirstChar { it.uppercase() },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Rich Genre Hub Card showing genre badge, game count, and mini preview thumbnails
 */
@Composable
private fun CategoryHubCard(
    categoryKey: String,
    gameCount: Int,
    previewGames: List<GameItem>,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val (iconEmoji, gradientColors) = getCategoryStyle(categoryKey)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Gradient banner area with emoji and game count
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Brush.linearGradient(gradientColors))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = iconEmoji,
                        fontSize = 28.sp
                    )

                    Surface(
                        color = Color.Black.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "$gameCount Games",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Category Title + Mini Game Thumbnails
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = categoryKey.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Overlapping or spaced mini thumbnails of top games
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    previewGames.forEach { game ->
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(game.coverUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = game.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }
        }
    }
}

private fun getCategoryStyle(category: String): Pair<String, List<Color>> {
    val cat = category.lowercase().trim()
    return when {
        cat.contains("puzzle") || cat.contains("brain") ->
            "🧩" to listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
        cat.contains("action") || cat.contains("fight") ->
            "⚔️" to listOf(Color(0xFFE11D48), Color(0xFFBE123C))
        cat.contains("arcade") || cat.contains("retro") ->
            "🕹️" to listOf(Color(0xFF0D9488), Color(0xFF0F766E))
        cat.contains("racing") || cat.contains("car") || cat.contains("drive") ->
            "🏎️" to listOf(Color(0xFFEA580C), Color(0xFFC2410C))
        cat.contains("sport") ->
            "⚽" to listOf(Color(0xFF059669), Color(0xFF047857))
        cat.contains("strategy") ->
            "🎯" to listOf(Color(0xFF4F46E5), Color(0xFF3730A3))
        cat.contains("casual") ->
            "🎲" to listOf(Color(0xFFD97706), Color(0xFFB45309))
        cat.contains("adventure") ->
            "🗺️" to listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
        else ->
            "🎮" to listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
    }
}
