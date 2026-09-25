package com.revamine.games.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.revamine.games.data.Game
import com.revamine.games.ui.components.GameCard

@Composable
fun FavoritesScreen(
    games: List<Game>,
    favorites: Set<String>,
    onPlayGame: (Game) -> Unit,
    onToggleFavorite: (Game) -> Unit
) {
    val favoriteGames = games.filter { favorites.contains(it.webId) }

    if (favoriteGames.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 8.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text("No favorites yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Tap the heart on any game to save it here",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(favoriteGames, key = { it.webId }) { game ->
            GameCard(
                game = game,
                isFavorite = true,
                onClick = { onPlayGame(game) },
                onToggleFavorite = { onToggleFavorite(game) }
            )
        }
    }
}
