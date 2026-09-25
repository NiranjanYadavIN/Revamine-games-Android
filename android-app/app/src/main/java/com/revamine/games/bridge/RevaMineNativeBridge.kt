package com.revamine.games.bridge

import android.app.Activity
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import com.revamine.games.ads.AdMobManager
import com.revamine.games.data.PrefsStore
import org.json.JSONObject
import java.lang.ref.WeakReference

/**
 * Native <-> Web bridge. Games.revamine.com ke games isko
 * `window.RevaMineNativeBridge` ke through call karte hain
 * (dekho src/utils/nativeBridge.ts web project me).
 *
 * @JavascriptInterface methods sirf String/primitive args le sakte hain
 * (JS functions/objects pass nahi ho sakte), isliye complex payloads
 * JSON string ke roop me aate hain aur reward ka result callback JS
 * global functions (`window._revaMineRewardSuccess/_Failure`) call
 * karke wapas bheja jaata hai — GameStageActivity is class ko
 * "AndroidNativeBridge" naam se inject karti hai aur ek chhota JS shim
 * window.RevaMineNativeBridge banata hai jo in methods ko wrap karta hai.
 */
class RevaMineNativeBridge(
    activity: Activity,
    webView: WebView,
    private val prefs: PrefsStore,
    private val gameId: String,
    private val onExitRequested: () -> Unit,
    private val onToggleMute: (Boolean) -> Unit
) {
    private val activityRef = WeakReference(activity)
    private val webViewRef = WeakReference(webView)

    // ------------------------------------------------------------ Haptics
    @JavascriptInterface
    fun triggerHaptic(type: String) {
        val activity = activityRef.get() ?: return
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = activity.getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            activity.getSystemService(Vibrator::class.java)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val durationMs = when (type) {
            "light", "selection" -> 15L
            "medium" -> 30L
            "heavy", "error" -> 50L
            "success" -> 25L
            "warning" -> 35L
            else -> 20L
        }
        vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    // ------------------------------------------------------------ Rewarded Ad
    @JavascriptInterface
    fun showRewardedAd(optionsJson: String) {
        val activity = activityRef.get() ?: return
        val rewardType = runCatching { JSONObject(optionsJson).optString("rewardType", "revive") }
            .getOrDefault("revive")

        activity.runOnUiThread {
            AdMobManager.showRewarded(
                activity = activity,
                rewardType = rewardType,
                onSuccess = { type, amount ->
                    runJs("window._revaMineRewardSuccess && window._revaMineRewardSuccess(${jsString(type)}, $amount);")
                },
                onFailure = { reason ->
                    runJs("window._revaMineRewardFailure && window._revaMineRewardFailure(${jsString(reason)});")
                }
            )
        }
    }

    // ------------------------------------------------------------ Interstitial Ad
    @JavascriptInterface
    fun showInterstitialAd(placement: String) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            AdMobManager.showInterstitial(activity)
        }
    }

    // ------------------------------------------------------------ Game lifecycle
    @JavascriptInterface
    fun onGameStarted(gameId: String) {
        // Local play-count analytics jagah — abhi no-op (Google Analytics
        // web side already track karta hai; yahan future me local stats
        // ya server ping add kar sakte ho).
    }

    @JavascriptInterface
    fun submitScore(payloadJson: String) {
        runCatching {
            val json = JSONObject(payloadJson)
            val id = json.optString("gameId", gameId)
            val score = json.optInt("score", 0)
            prefs.submitScore(id, score)
        }
    }

    @JavascriptInterface
    fun onGameOver(payloadJson: String) {
        runCatching {
            val json = JSONObject(payloadJson)
            val id = json.optString("gameId", gameId)
            val finalScore = json.optInt("finalScore", 0)
            prefs.submitScore(id, finalScore)
        }
    }

    @JavascriptInterface
    fun exitGameToNativeHome() {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread { onExitRequested() }
    }

    @JavascriptInterface
    fun showNativeToast(message: String) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    @JavascriptInterface
    fun setAudioMuted(isMuted: Boolean) {
        val activity = activityRef.get() ?: return
        activity.runOnUiThread { onToggleMute(isMuted) }
    }

    // ------------------------------------------------------------ Helpers
    private fun runJs(script: String) {
        val activity = activityRef.get() ?: return
        val webView = webViewRef.get() ?: return
        activity.runOnUiThread {
            webView.evaluateJavascript(script, null)
        }
    }

    private fun jsString(value: String): String =
        JSONObject.quote(value)
}
