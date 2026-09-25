package com.revamine.games.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revamine.games.data.GameItem
import com.revamine.games.ui.components.GameCard
import com.revamine.games.ui.theme.RevaIndigoLight
import com.revamine.games.ui.theme.RevaIndigoPrimary
import com.revamine.games.ui.theme.RevaRoseAccent

@Composable
fun FavoritesScreen(
    games: List<GameItem>,
    favorites: Set<String>,
    recentlyPlayedIds: List<String>,
    onPlayGame: (GameItem) -> Unit,
    onToggleFavorite: (GameItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val favoriteGames = remember(games, favorites) {
        games.filter { favorites.contains(it.id) }
    }

    val recentGames = remember(games, recentlyPlayedIds) {
        recentlyPlayedIds.mapNotNull { id -> games.firstOrNull { it.id == id } }
    }

    val suggestedGames = remember(games, favorites) {
        games.filter { !favorites.contains(it.id) }.take(6)
    }

    val searchResults = remember(games, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else games.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.shortTitle.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // Expandable Search Bar Header
        item(span = { GridItemSpan(2) }) {
            if (isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search games to favorite...") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = RevaIndigoPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            searchQuery = ""
                            isSearchExpanded = false
                        }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Close search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RevaIndigoPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = RevaRoseAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Favorite Games (${favoriteGames.size})",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Sleek Expand Search Button
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isSearchExpanded = true },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Open search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Search Results
        if (isSearchExpanded && searchQuery.isNotBlank()) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Search Results (${searchResults.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(searchResults, key = { "search_${it.id}" }) { game ->
                GameCard(
                    game = game,
                    isFavorite = favorites.contains(game.id),
                    onClick = { onPlayGame(game) },
                    onToggleFavorite = { onToggleFavorite(game) }
                )
            }
        } else {
            // Favorite Games Grid or Empty State
            if (favoriteGames.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(22.dp)
                        ) {
                            Text(
                                "No Favorites Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Long-press any game card to add it to favorites, or play suggestions below!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(favoriteGames, key = { "fav_${it.id}" }) { game ->
                    GameCard(
                        game = game,
                        isFavorite = true,
                        onClick = { onPlayGame(game) },
                        onToggleFavorite = { onToggleFavorite(game) }
                    )
                }
            }

            // Recently Played Section
            if (recentGames.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = RevaIndigoLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Recently Played",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                items(recentGames, key = { "recent_${it.id}" }) { game ->
                    GameCard(
                        game = game,
                        isFavorite = favorites.contains(game.id),
                        onClick = { onPlayGame(game) },
                        onToggleFavorite = { onToggleFavorite(game) }
                    )
                }
            }

            // Suggested for You Section
            item(span = { GridItemSpan(2) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 14.dp, bottom = 2.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = RevaIndigoLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Suggested For You",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(suggestedGames, key = { "sug_${it.id}" }) { game ->
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
