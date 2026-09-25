package com.revamine.games.data

data class GamesResponse(
    val status: String,
    val total: Int,
    val games: List<GameItem>
)

data class GameItem(
    val id: String,
    val title: String,
    val shortTitle: String,
    val category: String,
    val coverUrl: String,
    val badge: String?,
    val badgeColor: String?,
    val tagline: String,
    val order: Int,
    val featured: Boolean,
    val gameUrl: String
)
