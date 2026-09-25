package com.revamine.games.data

/**
 * RevaMine Games ka static 9-game catalog.
 * IDs aur cover art web project (games.revamine.com) ke DEFAULT_GAMES /
 * server /api/games response se match karte hain, taaki dono taraf
 * (web aur native) same games consistent dikhein.
 *
 * NOTE: Web app ke live "Google Sheet" se order/badge/featured update
 * hote hain. Yahan hum sirf static fallback rakhte hain (offline-first
 * native shell).
 */
private const val R2_BASE = "https://pub-db6074b2d7284990868083ef848a645c.r2.dev"

object GameCatalog {

    val games: List<Game> = listOf(
        Game(
            webId = "fruit-blade",
            title = "Fruit Blade Pop",
            shortTitle = "Fruit Blade",
            category = GameCategory.ARCADE,
            coverUrl = "$R2_BASE/covers/fruit-blade.webp",
            badge = "HOT",
            tagline = "Slice flying fruits, score combos, dodge bombs, and unlock legendary blades!",
            featured = true
        ),
        Game(
            webId = "cyber-snake",
            title = "Snake Adventure",
            shortTitle = "Snake Island",
            category = GameCategory.ARCADE,
            coverUrl = "$R2_BASE/covers/cyber-snake-2088.webp",
            badge = "POPULAR",
            tagline = "Navigate lush 3D grass, gobble juicy golden apples, and slither to victory!"
        ),
        Game(
            webId = "sling-dunk",
            title = "Sling Dunk Pop",
            shortTitle = "Sling Dunk",
            category = GameCategory.ARCADE,
            coverUrl = "$R2_BASE/covers/sling-dunk.webp",
            badge = "NEW",
            tagline = "Sling and shoot basketballs into moving hoops, trigger swish combos, and ignite fiery streaks!"
        ),
        Game(
            webId = "bubble-blast",
            title = "Bubble Blast",
            shortTitle = "Bubble Blast",
            category = GameCategory.PUZZLE,
            coverUrl = "$R2_BASE/covers/bubble-blast.webp",
            badge = "NEW",
            tagline = "Aim the cannon, shoot colorful bubbles, pop 3+ clusters, and trigger explosive bomb blasts!"
        ),
        Game(
            webId = "candy-match",
            title = "Candy Match Pop",
            shortTitle = "Candy Match",
            category = GameCategory.PUZZLE,
            coverUrl = "$R2_BASE/covers/candy-match-pop.webp",
            badge = "TRENDING",
            tagline = "Match 3 delicious candies, create striped sweets, and trigger chocolate bombs!"
        ),
        Game(
            webId = "happy-bird",
            title = "Happy Flappy Bird",
            shortTitle = "Happy Bird",
            category = GameCategory.ARCADE,
            coverUrl = "$R2_BASE/covers/happy-flappy-bird.webp",
            badge = "CLASSIC",
            tagline = "Tap to flap through cute pipes, grab golden coins, and set unbeatable highscores!"
        ),
        Game(
            webId = "dino-dash",
            title = "Dino Sky Dash",
            shortTitle = "Dino Dash",
            category = GameCategory.ACTION,
            coverUrl = "$R2_BASE/covers/dino-sky-dash.webp",
            badge = "POPULAR",
            tagline = "Sprint through prehistoric lands, jump cacti, duck under pterodactyls, and collect golden eggs!"
        ),
        Game(
            webId = "cyber-drift",
            title = "Neon Cyber Drift",
            shortTitle = "Cyber Drift",
            category = GameCategory.ACTION,
            coverUrl = "$R2_BASE/covers/cyber-drift.webp",
            badge = "TOP",
            tagline = "Drift high-speed neon sports cars through glowing turns, dodge traffic, and grab nitros!"
        ),
        Game(
            webId = "neon-2048",
            title = "2048 Neon Fusion",
            shortTitle = "2048 Neon",
            category = GameCategory.PUZZLE,
            coverUrl = "$R2_BASE/covers/2048-neon.webp",
            badge = "PUZZLE",
            tagline = "Slide and merge glowing number tiles to reach the legendary 2048 tile and beyond!"
        )
    )

    fun byWebId(webId: String): Game? = games.find { it.webId == webId }

    /** Games.revamine.com par actual game load karne ka URL, native mode flag ke saath. */
    fun stageUrl(webId: String): String = "https://games.revamine.com/game/$webId?mode=native"
}
