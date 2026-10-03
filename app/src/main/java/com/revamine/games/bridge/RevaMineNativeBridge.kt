package com.revamine.games.bridge

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.revamine.games.data.PrefsStore
import org.json.JSONObject

/**
 * RevaMine Native Bridge
 * Connects Web games to Native Android hardware features (Haptics, Back Navigation, Toasts, Scores).
 */
class RevaMineNativeBridge(
    private val activity: Activity,
    private val gameId: String? = null
) {
    private val prefs: PrefsStore by lazy { PrefsStore(activity) }
    private var onExitRequested: (() -> Unit)? = null
    private var onToggleMute: ((Boolean) -> Unit)? = null

    // Overloaded constructor for GameStageActivity compatibility
    constructor(
        activity: Activity,
        webView: WebView? = null,
        prefs: PrefsStore? = null,
        gameId: String? = null,
        onExitRequested: (() -> Unit)? = null,
        onToggleMute: ((Boolean) -> Unit)? = null
    ) : this(activity, gameId) {
        this.onExitRequested = onExitRequested
        this.onToggleMute = onToggleMute
    }

    // 1. Back button click: Closes game WebView Activity and returns cleanly to Native Home
    @JavascriptInterface
    fun exitGameToNativeHome() {
        activity.runOnUiThread {
            if (onExitRequested != null) {
                onExitRequested?.invoke()
            } else {
                activity.finish()
            }
        }
    }

    // 2. Hardware Haptic Feedback
    @JavascriptInterface
    fun triggerHaptic(type: String) {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val duration = when (type.lowercase()) {
                "heavy", "error" -> 50L
                "medium", "warning" -> 30L
                "success" -> 25L
                else -> 15L // "light"
            }
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // 3. Native Toast Notifications
    @JavascriptInterface
    fun showNativeToast(message: String) {
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    // 4. Score Submission
    @JavascriptInterface
    fun submitScore(payloadJson: String) {
        runCatching {
            val json = JSONObject(payloadJson)
            val id = json.optString("gameId", gameId ?: "game")
            val score = json.optInt("score", 0)
            prefs.submitScore(id, score)
        }
    }

    // 5. Game Over Hook
    @JavascriptInterface
    fun onGameOver(payloadJson: String) {
        runCatching {
            val json = JSONObject(payloadJson)
            val id = json.optString("gameId", gameId ?: "game")
            val finalScore = json.optInt("finalScore", 0)
            prefs.submitScore(id, finalScore)
        }
    }

    // 6. Game lifecycle & Rewards
    @JavascriptInterface
    fun onGameStarted(id: String) {
        // Lifecycle event
    }

    @JavascriptInterface
    fun showRewardedAd(optionsJson: String) {
        // Ad-free experience: instant reward callback
        activity.runOnUiThread {
            // Reward callback
        }
    }

    @JavascriptInterface
    fun showInterstitialAd(placement: String) {
        // Ad-free experience
    }

    @JavascriptInterface
    fun setAudioMuted(isMuted: Boolean) {
        activity.runOnUiThread {
            onToggleMute?.invoke(isMuted)
        }
    }

    // 7. Dynamic Status & Navigation Bar Theme Sync (Light Mode / Dark Mode)
    @JavascriptInterface
    fun updateTheme(isDark: Boolean) {
        activity.runOnUiThread {
            (activity as? com.revamine.games.MainActivity)?.applyDynamicTheme(isDark)
        }
    }
}
