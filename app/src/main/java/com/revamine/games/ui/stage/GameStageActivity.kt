package com.revamine.games.ui.stage

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revamine.games.BuildConfig
import com.revamine.games.bridge.RevaMineNativeBridge
import com.revamine.games.data.PrefsStore
import com.revamine.games.ui.theme.RevaMineGamesTheme

/**
 * Fullscreen "Game Stage" — hardware-accelerated WebView jo
 * gameUrl (e.g. games.revamine.com/game/{id}?mode=native) load karta hai aur
 * RevaMineNativeBridge inject karta hai (haptics, AdMob, scores).
 */
class GameStageActivity : ComponentActivity() {

    private var webViewRef: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: run { finish(); return }
        val gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE) ?: gameId
        val rawUrl = intent.getStringExtra(EXTRA_GAME_URL)
        // Construct targetUrl: Always point to /game/{id}?mode=native for Poki/CrazyGames style detail screen
        val targetUrl = if (!rawUrl.isNullOrBlank()) {
            val normalized = if (rawUrl.contains("/play/")) rawUrl.replace("/play/", "/game/") else rawUrl
            if (!normalized.contains("mode=native")) {
                if (normalized.contains("?")) "$normalized&mode=native" else "$normalized?mode=native"
            } else {
                normalized
            }
        } else {
            "https://games.revamine.com/game/$gameId?mode=native"
        }
        val prefs = PrefsStore(this)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val webView = webViewRef
                    if (webView != null && webView.canGoBack()) {
                        webView.goBack() // Returns from Game canvas back to Game Detail Screen
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed() // Returns to Native App Home
                        isEnabled = true
                    }
                }
            }
        )

        setContent {
            RevaMineGamesTheme(darkTheme = true) {
                GameStageScreen(
                    gameId = gameId,
                    gameTitle = gameTitle,
                    gameUrl = targetUrl,
                    prefs = prefs,
                    onWebViewReady = { webViewRef = it },
                    onExit = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        webViewRef?.destroy()
        webViewRef = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_GAME_ID = "extra_game_id"
        const val EXTRA_GAME_TITLE = "extra_game_title"
        const val EXTRA_GAME_URL = "extra_game_url"

        /** Bridge sirf in trusted origins par hi inject hoti hai. */
        val TRUSTED_HOSTS = setOf("games.revamine.com", "revamine.com")
    }
}

@Composable
private fun GameStageScreen(
    gameId: String,
    gameTitle: String,
    gameUrl: String,
    prefs: PrefsStore,
    onWebViewReady: (WebView) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(prefs.isMuted) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                createGameWebView(
                    context = ctx,
                    activity = context as androidx.activity.ComponentActivity,
                    prefs = prefs,
                    gameId = gameId,
                    onPageStarted = { isLoading = true },
                    onPageFinished = { isLoading = false },
                    onExitRequested = onExit,
                    onMuteChanged = { isMuted = it }
                ).also { webView ->
                    onWebViewReady(webView)
                    webView.loadUrl(gameUrl)
                }
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF6366F1),
                        strokeWidth = 3.5.dp,
                        modifier = Modifier.size(46.dp)
                    )
                    Text(
                        text = "Loading $gameTitle...",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun createGameWebView(
    context: android.content.Context,
    activity: androidx.activity.ComponentActivity,
    prefs: PrefsStore,
    gameId: String,
    onPageStarted: () -> Unit,
    onPageFinished: () -> Unit,
    onExitRequested: () -> Unit,
    onMuteChanged: (Boolean) -> Unit
): WebView {
    return WebView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            setSupportZoom(false)
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = false
            allowContentAccess = false
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        }

        val bridge = RevaMineNativeBridge(
            activity = activity,
            webView = this,
            prefs = prefs,
            gameId = gameId,
            onExitRequested = onExitRequested,
            onToggleMute = onMuteChanged
        )
        // Attach both direct interface and AndroidNativeBridge for backward/forward compatibility
        addJavascriptInterface(bridge, "RevaMineNativeBridge")
        addJavascriptInterface(bridge, "AndroidNativeBridge")

        val bridgeShimJs = """
            (function() {
              if (!window.RevaMineNativeBridge || typeof window.RevaMineNativeBridge.isNativeApp === 'undefined') {
                var rawBridge = window.RevaMineNativeBridge || window.AndroidNativeBridge;
                window.RevaMineNativeBridge = {
                  isNativeApp: true,
                  appVersion: ${BuildConfig.VERSION_NAME.let { "\"$it\"" }},
                  platform: 'android',
                  triggerHaptic: function(type) { if (rawBridge && rawBridge.triggerHaptic) rawBridge.triggerHaptic(type); },
                  showRewardedAd: function(optionsJson) { if (rawBridge && rawBridge.showRewardedAd) rawBridge.showRewardedAd(optionsJson); },
                  showInterstitialAd: function(placement) { if (rawBridge && rawBridge.showInterstitialAd) rawBridge.showInterstitialAd(placement || 'game_over'); },
                  onGameStarted: function(gameId) { if (rawBridge && rawBridge.onGameStarted) rawBridge.onGameStarted(gameId); },
                  submitScore: function(payloadJson) { if (rawBridge && rawBridge.submitScore) rawBridge.submitScore(payloadJson); },
                  onGameOver: function(payloadJson) { if (rawBridge && rawBridge.onGameOver) rawBridge.onGameOver(payloadJson); },
                  setAudioMuted: function(isMuted) { if (rawBridge && rawBridge.setAudioMuted) rawBridge.setAudioMuted(isMuted); },
                  exitGameToNativeHome: function() { if (rawBridge && rawBridge.exitGameToNativeHome) rawBridge.exitGameToNativeHome(); },
                  showNativeToast: function(message) { if (rawBridge && rawBridge.showNativeToast) rawBridge.showNativeToast(message); }
                };
              }
            })();
        """.trimIndent()

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): Boolean {
                val host = request.url.host ?: return true
                // Sirf trusted origin ke andar hi navigation allowed; baahar
                // ka link (e.g. external ad landing page) system browser me khule.
                return if (GameStageActivity.TRUSTED_HOSTS.any { host == it || host.endsWith(".$it") }) {
                    false
                } else {
                    runCatching {
                        context.startActivity(
                            android.content.Intent(android.content.Intent.ACTION_VIEW, request.url)
                        )
                    }
                    true
                }
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                onPageStarted()
                view.evaluateJavascript(bridgeShimJs, null)
            }

            override fun onPageFinished(view: WebView, url: String?) {
                view.evaluateJavascript(bridgeShimJs, null)
                onPageFinished()
            }
        }

        // Attach WebChromeClient with HTML5 FullScreen support
        webChromeClient = object : WebChromeClient() {
            private var customView: View? = null
            private var customViewCallback: CustomViewCallback? = null

            @Suppress("DEPRECATION")
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                super.onShowCustomView(view, callback)
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback

                // Hide Android status bar and navigation bar for immersive Fullscreen
                activity.window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
            }

            @Suppress("DEPRECATION")
            override fun onHideCustomView() {
                super.onHideCustomView()
                customView = null
                customViewCallback?.onCustomViewHidden()
                activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
        }
    }
}
