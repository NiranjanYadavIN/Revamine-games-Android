package com.revamine.games.data

/**
 * Ek game ka static catalog entry.
 * `webId` wahi ID hai jo games.revamine.com API/routes use karti hai
 * (e.g. "fruit-blade", "cyber-snake") — GameStageActivity isi ID se
 * https://games.revamine.com/game/{webId}?mode=native banata hai.
 */
data class Game(
    val webId: String,
    val title: String,
    val shortTitle: String,
    val category: GameCategory,
    val coverUrl: String,
    val badge: String,
    val tagline: String,
    val featured: Boolean = false
)

enum class GameCategory(val label: String) {
    ALL("All Games"),
    ARCADE("Arcade"),
    PUZZLE("Puzzle"),
    ACTION("Action")
}
