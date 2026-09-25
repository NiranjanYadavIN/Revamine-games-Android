package com.revamine.games.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revamine.games.data.Game
import com.revamine.games.data.GameCategory
import com.revamine.games.ui.components.CategoryChip
import com.revamine.games.ui.components.GameCard

@Composable
fun CategoriesScreen(
    games: List<Game>,
    favorites: Set<String>,
    selectedCategory: GameCategory,
    onCategorySelected: (GameCategory) -> Unit,
    onPlayGame: (Game) -> Unit,
    onToggleFavorite: (Game) -> Unit
) {
    val filtered = games.filter {
        selectedCategory == GameCategory.ALL || it.category == selectedCategory
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "Browse by Category",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        LazyRow(modifier = Modifier.padding(horizontal = 16.dp)) {
            items(GameCategory.entries) { category ->
                CategoryChip(
                    category = category,
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
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
}
