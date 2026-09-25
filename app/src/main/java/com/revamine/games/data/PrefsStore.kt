package com.revamine.games.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.util.Calendar

/**
 * Lightweight native storage layer — favourites, highscores, mute state,
 * theme choice, aur daily-login streak. SharedPreferences use kiya hai
 * (Room ki jagah) taaki build simple rahe; agar aage chal ke real backend
 * / Firebase add ho to isi jagah swap kar sakte ho.
 */
class PrefsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("revamine_prefs", Context.MODE_PRIVATE)

    // ---------------------------------------------------------------- Favourites
    fun isFavorite(gameId: String): Boolean = prefs.getStringSet(KEY_FAVORITES, emptySet())!!.contains(gameId)

    fun toggleFavorite(gameId: String): Boolean {
        val current = prefs.getStringSet(KEY_FAVORITES, emptySet())!!.toMutableSet()
        val nowFavorite = if (current.contains(gameId)) {
            current.remove(gameId); false
        } else {
            current.add(gameId); true
        }
        prefs.edit { putStringSet(KEY_FAVORITES, current) }
        return nowFavorite
    }

    fun favoriteIds(): Set<String> = prefs.getStringSet(KEY_FAVORITES, emptySet())!!

    // ---------------------------------------------------------------- Highscores
    fun highScore(gameId: String): Int = prefs.getInt("hs_$gameId", 0)

    fun submitScore(gameId: String, score: Int): Boolean {
        val best = highScore(gameId)
        return if (score > best) {
            prefs.edit { putInt("hs_$gameId", score) }
            true
        } else {
            false
        }
    }

    // ---------------------------------------------------------------- Sound / Theme
    var isMuted: Boolean
        get() = prefs.getBoolean(KEY_MUTED, false)
        set(value) = prefs.edit { putBoolean(KEY_MUTED, value) }

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit { putBoolean(KEY_DARK_THEME, value) }

    // ---------------------------------------------------------------- Ads
    var isAdsRemoved: Boolean
        get() = prefs.getBoolean(KEY_ADS_REMOVED, false)
        set(value) = prefs.edit { putBoolean(KEY_ADS_REMOVED, value) }

    // ---------------------------------------------------------------- Daily streak
    /** Har naye calendar din par +1; agar ek din bhi miss hua to streak 1 se restart hoti hai. */
    fun registerDailyVisitAndGetStreak(): Int {
        val today = dayStamp()
        val lastDay = prefs.getInt(KEY_LAST_VISIT_DAY, -1)
        val currentStreak = prefs.getInt(KEY_STREAK, 0)

        if (lastDay == today) return currentStreak

        val newStreak = if (lastDay == today - 1) currentStreak + 1 else 1
        prefs.edit {
            putInt(KEY_LAST_VISIT_DAY, today)
            putInt(KEY_STREAK, newStreak)
        }
        return newStreak
    }

    fun currentStreak(): Int = prefs.getInt(KEY_STREAK, 0)

    // ---------------------------------------------------------------- Recently Played
    fun recordRecentlyPlayed(gameId: String) {
        val current = recentlyPlayedIds().toMutableList()
        current.remove(gameId)
        current.add(0, gameId)
        val trimmed = current.take(12).joinToString(",")
        prefs.edit { putString(KEY_RECENTLY_PLAYED, trimmed) }
    }

    fun recentlyPlayedIds(): List<String> {
        val raw = prefs.getString(KEY_RECENTLY_PLAYED, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",").filter { it.isNotBlank() }
    }

    // ---------------------------------------------------------------- Offline Cache
    fun getCachedGamesJson(): String? = prefs.getString(KEY_CACHED_GAMES_JSON, null)

    fun saveCachedGamesJson(json: String) {
        prefs.edit { putString(KEY_CACHED_GAMES_JSON, json) }
    }

    private fun dayStamp(): Int {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_MUTED = "is_muted"
        private const val KEY_DARK_THEME = "is_dark_theme"
        private const val KEY_ADS_REMOVED = "ads_removed"
        private const val KEY_LAST_VISIT_DAY = "last_visit_day"
        private const val KEY_STREAK = "daily_streak"
        private const val KEY_RECENTLY_PLAYED = "recently_played_games"
        private const val KEY_CACHED_GAMES_JSON = "cached_games_json"
    }
}
