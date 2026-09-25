package com.revamine.games.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sparkle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.revamine.games.data.Game
import com.revamine.games.data.GameCategory
import com.revamine.games.ui.components.CategoryChip
import com.revamine.games.ui.components.GameCard

@Composable
fun ExploreScreen(
    games: List<Game>,
    favorites: Set<String>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: GameCategory,
    onCategorySelected: (GameCategory) -> Unit,
    onPlayGame: (Game) -> Unit,
    onToggleFavorite: (Game) -> Unit
) {
    val featured = games.firstOrNull { it.featured } ?: games.firstOrNull()

    val filtered = games.filter { game ->
        val matchesCategory = selectedCategory == GameCategory.ALL || game.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
            game.title.contains(searchQuery, ignoreCase = true) ||
            game.shortTitle.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero featured banner
        if (featured != null) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                HeroBanner(game = featured, onClick = { onPlayGame(featured) })
            }
        }

        // Search bar
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search games...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category chips
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            LazyRow {
                items(GameCategory.entries) { category ->
                    CategoryChip(
                        category = category,
                        selected = category == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }

        items(filtered, key = { it.webId }) { game ->
            GameCard(
                game = game,
                isFavorite = favorites.contains(game.webId),
                onClick = { onPlayGame(game) },
                onToggleFavorite = { onToggleFavorite(game) }
            )
        }
    }
}

@Composable
private fun HeroBanner(game: Game, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.7f)
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = game.title,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                            startY = 60f
                        )
                    )
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp),
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(50)
            ) {
                Row {
                    Icon(
                        Icons.Filled.Sparkle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        "FEATURED GAME",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                Text(
                    game.title,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    game.tagline,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                        Text(
                            "PLAY NOW",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
