package com.revamine.games.model

import com.google.gson.annotations.SerializedName

data class FeedResponse(
    @SerializedName("status") val status: String = "success",
    @SerializedName("platform") val platform: String? = null,
    @SerializedName("featuredGame") val featuredGame: GameItem? = null,
    @SerializedName("trendingGames") val trendingGames: List<GameItem>? = null,
    @SerializedName("categories") val categories: List<CategoryItem>? = null,
    @SerializedName("pagination") val pagination: PaginationInfo? = null,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("games") val games: List<GameItem> = emptyList()
)

data class RecommendationsResponse(
    @SerializedName("status") val status: String = "success",
    @SerializedName("targetGameId") val targetGameId: String = "",
    @SerializedName("limit") val limit: Int = 4,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("recommendations") val recommendations: List<GameItem> = emptyList()
)

data class GameItem(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("shortTitle") val shortTitle: String = "",
    @SerializedName("category") val category: String = "arcade",
    @SerializedName("coverUrl") val coverUrl: String,
    @SerializedName("badge") val badge: String? = null,
    @SerializedName("badgeColor") val badgeColor: String? = null,
    @SerializedName("tagline") val tagline: String = "",
    @SerializedName("order") val order: Int = 0,
    @SerializedName("status") val status: String = "Active",
    @SerializedName("featured") val featured: Boolean = false,
    @SerializedName("totalPlays") val totalPlays: Int = 0,
    @SerializedName("gameUrl") val gameUrl: String
)

data class CategoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("label") val label: String,
    @SerializedName("icon") val icon: String? = null
)

data class PaginationInfo(
    @SerializedName("currentPage") val currentPage: Int = 1,
    @SerializedName("limit") val limit: Int = 12,
    @SerializedName("total") val total: Int = 0,
    @SerializedName("hasMore") val hasMore: Boolean = false
)
