package com.revamine.games

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.revamine.games.ads.AdMobManager
import com.revamine.games.data.Game
import com.revamine.games.data.GameCatalog
import com.revamine.games.data.GameCategory
import com.revamine.games.data.PrefsStore
import com.revamine.games.ui.screens.CategoriesScreen
import com.revamine.games.ui.screens.ExploreScreen
import com.revamine.games.ui.screens.FavoritesScreen
import com.revamine.games.ui.screens.ProfileScreen
import com.revamine.games.ui.stage.GameStageActivity
import com.revamine.games.ui.theme.RevaMineGamesTheme

private enum class BottomTab(val label: String) {
    EXPLORE("Explore"),
    CATEGORIES("Categories"),
    FAVORITES("Favorites"),
    PROFILE("Profile")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = PrefsStore(this)
        AdMobManager.preloadAll(this)
        val streak = prefs.registerDailyVisitAndGetStreak()

        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }

            RevaMineGamesTheme(darkTheme = isDarkTheme) {
                RevaMineApp(
                    prefs = prefs,
                    streak = streak,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        isDarkTheme = it
                        prefs.isDarkTheme = it
                    },
                    onPlayGame = { game -> launchGame(game) }
                )
            }
        }
    }

    private fun launchGame(game: Game) {
        val intent = Intent(this, GameStageActivity::class.java).apply {
            putExtra(GameStageActivity.EXTRA_GAME_ID, game.webId)
            putExtra(GameStageActivity.EXTRA_GAME_TITLE, game.title)
        }
        startActivity(intent)
    }
}

@Composable
private fun RevaMineApp(
    prefs: PrefsStore,
    streak: Int,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onPlayGame: (Game) -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.EXPLORE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GameCategory.ALL) }
    var favorites by remember { mutableStateOf(prefs.favoriteIds()) }
    var isMuted by remember { mutableStateOf(prefs.isMuted) }
    var isAdsRemoved by remember { mutableStateOf(prefs.isAdsRemoved) }

    val games = GameCatalog.games

    fun toggleFavorite(game: Game) {
        prefs.toggleFavorite(game.webId)
        favorites = prefs.favoriteIds()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == BottomTab.EXPLORE,
                    onClick = { selectedTab = BottomTab.EXPLORE },
                    icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                    label = { Text(BottomTab.EXPLORE.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.CATEGORIES,
                    onClick = { selectedTab = BottomTab.CATEGORIES },
                    icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                    label = { Text(BottomTab.CATEGORIES.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.FAVORITES,
                    onClick = { selectedTab = BottomTab.FAVORITES },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                    label = { Text(BottomTab.FAVORITES.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.PROFILE,
                    onClick = { selectedTab = BottomTab.PROFILE },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(BottomTab.PROFILE.label) }
                )
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                BottomTab.EXPLORE -> ExploreScreen(
                    games = games,
                    favorites = favorites,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onPlayGame = onPlayGame,
                    onToggleFavorite = ::toggleFavorite
                )

                BottomTab.CATEGORIES -> CategoriesScreen(
                    games = games,
                    favorites = favorites,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    onPlayGame = onPlayGame,
                    onToggleFavorite = ::toggleFavorite
                )

                BottomTab.FAVORITES -> FavoritesScreen(
                    games = games,
                    favorites = favorites,
                    onPlayGame = onPlayGame,
                    onToggleFavorite = ::toggleFavorite
                )

                BottomTab.PROFILE -> ProfileScreen(
                    prefs = prefs,
                    streak = streak,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    isMuted = isMuted,
                    onToggleMute = {
                        isMuted = it
                        prefs.isMuted = it
                    },
                    isAdsRemoved = isAdsRemoved,
                    onRemoveAdsClick = {
                        // TODO: Google Play Billing IAP flow — abhi scope se bahar
                        // (spec ke "No Ads VIP" feature ke liye Play Console product
                        // aur Billing library setup chahiye).
                        isAdsRemoved = true
                        prefs.isAdsRemoved = true
                    }
                )
            }
        }
    }
}
