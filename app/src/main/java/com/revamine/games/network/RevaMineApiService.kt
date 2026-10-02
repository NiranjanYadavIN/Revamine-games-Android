package com.revamine.games.network

import com.revamine.games.model.FeedResponse
import com.revamine.games.model.RecommendationsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RevaMineApiService {

    @GET("/api/feed")
    suspend fun getFeed(
        @Query("category") category: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 12,
        @Query("sort") sort: String? = null
    ): FeedResponse

    @GET("/api/recommendations")
    suspend fun getRecommendations(
        @Query("gameId") gameId: String,
        @Query("limit") limit: Int = 4
    ): RecommendationsResponse
}
