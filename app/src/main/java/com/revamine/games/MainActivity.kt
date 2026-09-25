package com.revamine.games

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.revamine.games.ads.AdMobManager
import com.revamine.games.data.GameItem
import com.revamine.games.data.GamesRepository
import com.revamine.games.data.PrefsStore
import com.revamine.games.ui.components.RevaMineHeader
import com.revamine.games.ui.screens.CategoriesScreen
import com.revamine.games.ui.screens.ExploreScreen
import com.revamine.games.ui.screens.FavoritesScreen
import com.revamine.games.ui.screens.ProfileScreen
import com.revamine.games.ui.stage.GameStageActivity
import com.revamine.games.ui.theme.RevaIndigoLight
import com.revamine.games.ui.theme.RevaIndigoPrimary
import com.revamine.games.ui.theme.RevaMineGamesTheme
import kotlinx.coroutines.launch

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
        val repository = GamesRepository(this, prefs)
        AdMobManager.preloadAll(this)
        val streak = prefs.registerDailyVisitAndGetStreak()

        setContent {
            var isDarkTheme by remember { mutableStateOf(prefs.isDarkTheme) }

            RevaMineGamesTheme(darkTheme = isDarkTheme) {
                RevaMineApp(
                    repository = repository,
                    prefs = prefs,
                    streak = streak,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = {
                        isDarkTheme = it
                        prefs.isDarkTheme = it
                    },
                    onPlayGame = { game -> launchGame(game, prefs) }
                )
            }
        }
    }

    private fun launchGame(game: GameItem, prefs: PrefsStore) {
        prefs.recordRecentlyPlayed(game.id)
        val intent = Intent(this, GameStageActivity::class.java).apply {
            putExtra(GameStageActivity.EXTRA_GAME_ID, game.id)
            putExtra(GameStageActivity.EXTRA_GAME_TITLE, game.title)
            putExtra(GameStageActivity.EXTRA_GAME_URL, game.gameUrl)
        }
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RevaMineApp(
    repository: GamesRepository,
    prefs: PrefsStore,
    streak: Int,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onPlayGame: (GameItem) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(BottomTab.EXPLORE) }
    var searchQuery by remember { mutableStateOf("") }
    var favorites by remember { mutableStateOf(prefs.favoriteIds()) }
    var isMuted by remember { mutableStateOf(prefs.isMuted) }
    var isAdsRemoved by remember { mutableStateOf(prefs.isAdsRemoved) }
    var showMenuSheet by remember { mutableStateOf(false) }

    var games by remember { mutableStateOf<List<GameItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Dynamically derive categories from live API response
    val categories = remember(games) {
        val unique = games.map { it.category.lowercase() }.distinct()
        if (unique.isEmpty()) listOf("all", "arcade", "puzzle", "action") else listOf("all") + unique
    }
    var selectedCategory by remember { mutableStateOf("all") }

    LaunchedEffect(Unit) {
        isLoading = true
        games = repository.fetchGames()
        isLoading = false
    }

    val onRefresh: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            games = repository.fetchGames(forceRefresh = true)
            isRefreshing = false
        }
    }

    fun toggleFavorite(game: GameItem) {
        prefs.toggleFavorite(game.id)
        favorites = prefs.favoriteIds()
    }

    Scaffold(
        topBar = {
            RevaMineHeader(
                isDarkTheme = isDarkTheme,
                onToggleTheme = { onToggleTheme(!isDarkTheme) },
                onMenuClick = { showMenuSheet = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == BottomTab.EXPLORE,
                    onClick = { selectedTab = BottomTab.EXPLORE },
                    icon = { Icon(Icons.Filled.Explore, contentDescription = null) },
                    label = { Text(BottomTab.EXPLORE.label, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RevaIndigoPrimary,
                        selectedTextColor = RevaIndigoPrimary,
                        indicatorColor = RevaIndigoPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.CATEGORIES,
                    onClick = { selectedTab = BottomTab.CATEGORIES },
                    icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                    label = { Text(BottomTab.CATEGORIES.label, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RevaIndigoPrimary,
                        selectedTextColor = RevaIndigoPrimary,
                        indicatorColor = RevaIndigoPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.FAVORITES,
                    onClick = { selectedTab = BottomTab.FAVORITES },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
                    label = { Text(BottomTab.FAVORITES.label, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RevaIndigoPrimary,
                        selectedTextColor = RevaIndigoPrimary,
                        indicatorColor = RevaIndigoPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == BottomTab.PROFILE,
                    onClick = { selectedTab = BottomTab.PROFILE },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(BottomTab.PROFILE.label, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RevaIndigoPrimary,
                        selectedTextColor = RevaIndigoPrimary,
                        indicatorColor = RevaIndigoPrimary.copy(alpha = 0.16f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                BottomTab.EXPLORE -> ExploreScreen(
                    games = games,
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    favorites = favorites,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = { selectedCategory = it },
                    onPlayGame = onPlayGame,
                    onToggleFavorite = ::toggleFavorite
                )

                BottomTab.CATEGORIES -> CategoriesScreen(
                    games = games,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    favorites = favorites,
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = { selectedCategory = it },
                    onPlayGame = onPlayGame,
                    onToggleFavorite = ::toggleFavorite
                )

                BottomTab.FAVORITES -> FavoritesScreen(
                    games = games,
                    favorites = favorites,
                    recentlyPlayedIds = prefs.recentlyPlayedIds(),
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
                        isAdsRemoved = true
                        prefs.isAdsRemoved = true
                    }
                )
            }
        }
    }

    // Quick Menu Bottom Sheet
    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "RevaMine Games",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = RevaIndigoPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "🔥 $streak Days Streak",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = RevaIndigoPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Menu items
                MenuItemRow(
                    icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    title = if (isMuted) "Unmute Game Audio" else "Mute Game Audio",
                    subtitle = if (isMuted) "Audio is currently muted" else "Sound effects active",
                    onClick = {
                        isMuted = !isMuted
                        prefs.isMuted = isMuted
                    }
                )

                MenuItemRow(
                    icon = Icons.Filled.Language,
                    title = "Visit Official Website",
                    subtitle = "games.revamine.com",
                    onClick = {
                        showMenuSheet = false
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://games.revamine.com"))
                        context.startActivity(intent)
                    }
                )

                MenuItemRow(
                    icon = Icons.Filled.Share,
                    title = "Share RevaMine Games",
                    subtitle = "Invite friends to play free online games",
                    onClick = {
                        showMenuSheet = false
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Play awesome free games on RevaMine Games! https://games.revamine.com")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share RevaMine Games"))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(42.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RevaIndigoPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
