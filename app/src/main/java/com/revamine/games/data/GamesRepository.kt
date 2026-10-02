package com.revamine.games.data

import android.content.Context
import android.util.Log
import com.revamine.games.model.FeedResponse
import com.revamine.games.model.GameItem
import com.revamine.games.model.RecommendationsResponse
import com.revamine.games.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GamesRepository(
    private val context: Context,
    private val prefs: PrefsStore
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GamesRepository"
        const val R2_COVERS_BASE = "https://pub-db6074b2d7284990868083ef848a645c.r2.dev/covers"
        const val GOOGLE_SHEET_ID = "1CG_awisqeGUP-gCeGJOuSTKuT0Lll6G566UXt-oI3mQ"
        const val GOOGLE_SHEET_URL = "https://docs.google.com/spreadsheets/d/$GOOGLE_SHEET_ID/gviz/tq?tqx=out:json"

        fun normalizeGameId(id: String): String {
            val s = id.lowercase().trim()
            return when (s) {
                "2048-neon", "neon-2048", "2048" -> "neon-2048"
                "cyber-bird", "happy-bird", "flappy-bird" -> "happy-bird"
                "dino-run", "dino-dash", "dino-sky-dash", "dino" -> "dino-dash"
                "fruit-blade", "fruit-blade-pop", "fruit-ninja" -> "fruit-blade"
                "candy-match", "candy-match-pop" -> "candy-match"
                "bubble-blast", "bubble" -> "bubble-blast"
                "sling-dunk", "dunk-shot" -> "sling-dunk"
                "cyber-drift", "drift" -> "cyber-drift"
                "cyber-snake", "snake" -> "cyber-snake"
                else -> s
            }
        }

        fun coverFilenameForId(id: String): String {
            return when (normalizeGameId(id)) {
                "fruit-blade" -> "fruit-blade.webp"
                "cyber-snake" -> "cyber-snake-2088.webp"
                "sling-dunk" -> "sling-dunk.webp"
                "bubble-blast" -> "bubble-blast.webp"
                "candy-match" -> "candy-match-pop.webp"
                "happy-bird" -> "happy-flappy-bird.webp"
                "dino-dash" -> "dino-sky-dash.webp"
                "cyber-drift" -> "cyber-drift.webp"
                "neon-2048" -> "neon-2048.webp"
                else -> "$id.webp"
            }
        }

        fun normalizeCoverUrl(rawUrl: String, gameId: String): String {
            if (rawUrl.isBlank() || rawUrl.contains("images.revamine.com") || !rawUrl.startsWith("http")) {
                return "$R2_COVERS_BASE/${coverFilenameForId(gameId)}"
            }
            return rawUrl
        }

        fun badgeColorForBadge(badge: String): String {
            val upper = badge.uppercase().trim()
            return when {
                upper.contains("HOT") || upper.contains("FLAGSHIP") -> "bg-rose-500"
                upper.contains("POPULAR") || upper.contains("TRENDING") -> "bg-emerald-500"
                upper.contains("NEW") -> "bg-amber-500"
                upper.contains("TOP") || upper.contains("PRO") -> "bg-purple-600"
                upper.contains("PUZZLE") -> "bg-cyan-500"
                else -> "bg-indigo-600"
            }
        }
    }

    /**
     * Fetches dynamic home feed via Retrofit from RevaMine Cloud Games Engine
     * (/api/feed with fallback to Google Sheet and offline cache).
     */
    suspend fun fetchFeed(
        category: String? = null,
        page: Int = 1,
        limit: Int = 12,
        sort: String? = null
    ): FeedResponse = withContext(Dispatchers.IO) {
        // 1. Primary: RevaMine Cloud Games Engine Feed API
        try {
            val queryCat = if (category == "all" || category.isNullOrBlank()) null else category
            val response = ApiClient.apiService.getFeed(
                category = queryCat,
                page = page,
                limit = limit,
                sort = sort
            )

            if (response.games.isNotEmpty() || response.featuredGame != null) {
                val normalizedGames = response.games.map { g ->
                    g.copy(coverUrl = normalizeCoverUrl(g.coverUrl, g.id))
                }
                val normalizedFeatured = response.featuredGame?.let { f ->
                    f.copy(coverUrl = normalizeCoverUrl(f.coverUrl, f.id))
                }
                val normalizedTrending = response.trendingGames?.map { t ->
                    t.copy(coverUrl = normalizeCoverUrl(t.coverUrl, t.id))
                }

                val fullResponse = response.copy(
                    featuredGame = normalizedFeatured,
                    trendingGames = normalizedTrending,
                    games = normalizedGames
                )

                // Cache for offline resilience
                prefs.saveCachedGamesJson(serializeGamesToJson(normalizedGames))
                Log.d(TAG, "Successfully fetched feed from Retrofit API (${normalizedGames.size} games)")
                return@withContext fullResponse
            }
        } catch (e: Exception) {
            Log.w(TAG, "Retrofit API /api/feed call failed: ${e.message}")
        }

        // 2. Secondary: Fallback to existing Games endpoint / Google Sheet
        val fallbackGames = fetchGames()
        val featured = fallbackGames.firstOrNull { it.featured } ?: fallbackGames.firstOrNull()

        FeedResponse(
            status = "success",
            platform = "RevaMine Local Engine (Offline/Fallback)",
            featuredGame = featured,
            trendingGames = fallbackGames.take(4),
            categories = listOf(
                com.revamine.games.model.CategoryItem("all", "All Games", "Grid"),
                com.revamine.games.model.CategoryItem("arcade", "Arcade", "Zap"),
                com.revamine.games.model.CategoryItem("puzzle", "Puzzle", "Layers"),
                com.revamine.games.model.CategoryItem("action", "Action", "Flame")
            ),
            pagination = com.revamine.games.model.PaginationInfo(1, fallbackGames.size, fallbackGames.size, false),
            total = fallbackGames.size,
            games = fallbackGames
        )
    }

    /**
     * Recommendations for Game Detail Screen
     */
    suspend fun fetchRecommendations(gameId: String, limit: Int = 4): List<GameItem> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.apiService.getRecommendations(gameId = gameId, limit = limit)
            if (response.recommendations.isNotEmpty()) {
                return@withContext response.recommendations.map { g ->
                    g.copy(coverUrl = normalizeCoverUrl(g.coverUrl, g.id))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch recommendations: ${e.message}")
        }

        // Fallback: Other games excluding target
        fetchGames().filter { it.id != gameId }.take(limit)
    }

    /**
     * Backward-compatible games fetch method with Google Sheet and Offline Cache.
     */
    suspend fun fetchGames(forceRefresh: Boolean = false): List<GameItem> = withContext(Dispatchers.IO) {
        val baseGames = loadBaseGames()

        // 1. Try Google Sheet sync
        try {
            val liveGames = syncWithGoogleSheet(baseGames)
            if (liveGames.isNotEmpty()) {
                val jsonToCache = serializeGamesToJson(liveGames)
                prefs.saveCachedGamesJson(jsonToCache)
                Log.d(TAG, "Successfully synced ${liveGames.size} games from Google Sheet")
                return@withContext liveGames
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google Sheet sync failed: ${e.message}")
        }

        // 2. Fallback to cached games
        val cachedJson = prefs.getCachedGamesJson()
        if (!cachedJson.isNullOrBlank()) {
            val cached = parseGamesJson(cachedJson)
            if (cached != null && cached.games.isNotEmpty()) {
                Log.d(TAG, "Using cached games (${cached.games.size} games)")
                return@withContext cached.games.map { g ->
                    g.copy(coverUrl = normalizeCoverUrl(g.coverUrl, g.id))
                }
            }
        }

        baseGames
    }

    private fun loadBaseGames(): List<GameItem> {
        val cachedJson = prefs.getCachedGamesJson()
        if (!cachedJson.isNullOrBlank()) {
            val cached = parseGamesJson(cachedJson)
            if (cached != null && cached.games.isNotEmpty()) {
                return cached.games.map { g ->
                    g.copy(coverUrl = normalizeCoverUrl(g.coverUrl, g.id))
                }
            }
        }
        return loadGamesFromAssets()
    }

    private fun syncWithGoogleSheet(baseGames: List<GameItem>): List<GameItem> {
        val request = Request.Builder()
            .url(GOOGLE_SHEET_URL)
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return emptyList()
            val text = response.body?.string() ?: return emptyList()
            val start = text.indexOf('{')
            val end = text.lastIndexOf('}')
            if (start == -1 || end == -1 || start >= end) return emptyList()

            val json = JSONObject(text.substring(start, end + 1))
            val table = json.optJSONObject("table") ?: return emptyList()
            val rows = table.optJSONArray("rows") ?: return emptyList()

            data class SheetEntry(
                val status: String,
                val badge: String?,
                val order: Int,
                val featured: Boolean
            )

            val sheetMap = mutableMapOf<String, SheetEntry>()

            for (i in 0 until rows.length()) {
                val row = rows.getJSONObject(i)
                val c = row.optJSONArray("c") ?: continue

                fun cellVal(idx: Int): String? {
                    if (idx >= c.length() || c.isNull(idx)) return null
                    val cell = c.optJSONObject(idx) ?: return null
                    if (cell.isNull("v")) return null
                    return cell.optString("v", "").trim()
                }

                val rawId = cellVal(0) ?: continue
                if (rawId.isBlank()) continue
                val normId = normalizeGameId(rawId)

                val status = cellVal(1) ?: "Active"
                val badge = cellVal(2)?.takeIf { it.isNotBlank() }
                val order = cellVal(3)?.toDoubleOrNull()?.toInt() ?: 99
                val featured = cellVal(4)?.equals("true", ignoreCase = true) ?: false

                sheetMap[normId] = SheetEntry(
                    status = if (status.equals("Hidden", ignoreCase = true)) "Hidden" else "Active",
                    badge = badge,
                    order = order,
                    featured = featured
                )
            }

            if (sheetMap.isEmpty()) return emptyList()

            val updatedGames = baseGames.mapNotNull { base ->
                val normId = normalizeGameId(base.id)
                val sheet = sheetMap[normId]
                if (sheet != null && sheet.status.equals("Hidden", ignoreCase = true)) {
                    null
                } else if (sheet != null) {
                    val updatedBadge = sheet.badge?.takeIf { it.isNotBlank() }
                    base.copy(
                        badge = updatedBadge,
                        badgeColor = if (!updatedBadge.isNullOrBlank()) badgeColorForBadge(updatedBadge) else null,
                        order = if (sheet.order in 1..99) sheet.order else base.order,
                        featured = sheet.featured,
                        coverUrl = normalizeCoverUrl(base.coverUrl, base.id)
                    )
                } else {
                    base.copy(coverUrl = normalizeCoverUrl(base.coverUrl, base.id))
                }
            }

            return updatedGames.sortedBy { it.order }
        }
    }

    private fun serializeGamesToJson(games: List<GameItem>): String {
        val root = JSONObject()
        root.put("status", "success")
        root.put("total", games.size)
        val array = JSONArray()
        for (g in games) {
            val obj = JSONObject()
            obj.put("id", g.id)
            obj.put("title", g.title)
            obj.put("shortTitle", g.shortTitle ?: g.title)
            obj.put("category", g.category)
            obj.put("coverUrl", normalizeCoverUrl(g.coverUrl, g.id))
            if (g.badge != null) obj.put("badge", g.badge)
            if (g.badgeColor != null) obj.put("badgeColor", g.badgeColor)
            obj.put("tagline", g.tagline ?: "")
            obj.put("order", g.order)
            obj.put("featured", g.featured)
            obj.put("gameUrl", g.gameUrl)
            array.put(obj)
        }
        root.put("games", array)
        return root.toString()
    }

    private fun parseGamesJson(jsonStr: String): FeedResponse? {
        return try {
            val root = JSONObject(jsonStr)
            val status = root.optString("status", "success")
            val total = root.optInt("total", 0)
            val jsonGames = root.optJSONArray("games") ?: return null
            val list = mutableListOf<GameItem>()

            for (i in 0 until jsonGames.length()) {
                val obj = jsonGames.getJSONObject(i)
                val id = obj.optString("id", "")
                val title = obj.optString("title", "")
                val shortTitle = obj.optString("shortTitle", title)
                val category = obj.optString("category", "arcade").trim()
                val rawCover = obj.optString("coverUrl", "")
                val coverUrl = normalizeCoverUrl(rawCover, id)
                val badge = if (obj.has("badge") && !obj.isNull("badge")) obj.getString("badge") else null
                val badgeColor = if (obj.has("badgeColor") && !obj.isNull("badgeColor")) obj.getString("badgeColor") else null
                val tagline = obj.optString("tagline", "")
                val order = obj.optInt("order", i + 1)
                val featured = obj.optBoolean("featured", false)
                val rawGameUrl = obj.optString("gameUrl", "")
                val gameUrl = if (rawGameUrl.isNotBlank()) {
                    rawGameUrl
                } else {
                    "https://games.revamine.com/game/$id?mode=native"
                }

                list.add(
                    GameItem(
                        id = id,
                        title = title,
                        shortTitle = shortTitle,
                        category = category,
                        coverUrl = coverUrl,
                        badge = badge,
                        badgeColor = badgeColor,
                        tagline = tagline,
                        order = order,
                        featured = featured,
                        gameUrl = gameUrl
                    )
                )
            }
            FeedResponse(status = status, total = total, games = list.sortedBy { it.order })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON: ${e.message}")
            null
        }
    }

    private fun loadGamesFromAssets(): List<GameItem> {
        return try {
            context.assets.open("default_games.json").use { stream ->
                BufferedReader(InputStreamReader(stream)).use { reader ->
                    val json = reader.readText()
                    prefs.saveCachedGamesJson(json)
                    parseGamesJson(json)?.games ?: emptyList()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading default games asset: ${e.message}")
            emptyList()
        }
    }
}
