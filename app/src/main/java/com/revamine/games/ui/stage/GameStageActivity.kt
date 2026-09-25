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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.revamine.games.BuildConfig
import com.revamine.games.bridge.RevaMineNativeBridge
import com.revamine.games.data.GameCatalog
import com.revamine.games.data.PrefsStore
import com.revamine.games.ui.theme.RevaMineGamesTheme

/**
 * Fullscreen "Game Stage" — hardware-accelerated WebView jo
 * games.revamine.com/game/{id}?mode=native load karta hai aur
 * RevaMineNativeBridge inject karta hai (haptics, AdMob, scores).
 *
 * Security: sirf games.revamine.com (aur localhost dev builds) origin
 * ko WebView me navigate karne diya jaata hai.
 */
class GameStageActivity : ComponentActivity() {

    private var webViewRef: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gameId = intent.getStringExtra(EXTRA_GAME_ID) ?: run { finish(); return }
        val gameTitle = intent.getStringExtra(EXTRA_GAME_TITLE) ?: gameId
        val prefs = PrefsStore(this)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val webView = webViewRef
                    if (webView != null && webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
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

        /** Bridge sirf in trusted origins par hi inject hoti hai. */
        val TRUSTED_HOSTS = setOf("games.revamine.com")
    }
}

@Composable
private fun GameStageScreen(
    gameId: String,
    gameTitle: String,
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
                    webView.loadUrl(GameCatalog.stageUrl(gameId))
                }
            }
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        // Top floating controls: Back | title | Mute
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            FloatingCircleButton(icon = Icons.Filled.ArrowBack, onClick = onExit)

            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(50)
            ) {
                Text(
                    gameTitle,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            FloatingCircleButton(
                icon = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                onClick = {
                    val newMuted = !isMuted
                    isMuted = newMuted
                    prefs.isMuted = newMuted
                }
            )
        }
    }
}

@Composable
private fun FloatingCircleButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(40.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.4f)
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = Color.White)
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
        addJavascriptInterface(bridge, "AndroidNativeBridge")

        val bridgeShimJs = """
            (function() {
              if (window.RevaMineNativeBridge) return;
              window.RevaMineNativeBridge = {
                isNativeApp: true,
                appVersion: ${BuildConfig.VERSION_NAME.let { "\"$it\"" }},
                platform: 'android',
                triggerHaptic: function(type) { AndroidNativeBridge.triggerHaptic(type); },
                showRewardedAd: function(optionsJson) { AndroidNativeBridge.showRewardedAd(optionsJson); },
                showInterstitialAd: function(placement) { AndroidNativeBridge.showInterstitialAd(placement || 'game_over'); },
                onGameStarted: function(gameId) { AndroidNativeBridge.onGameStarted(gameId); },
                submitScore: function(payloadJson) { AndroidNativeBridge.submitScore(payloadJson); },
                onGameOver: function(payloadJson) { AndroidNativeBridge.onGameOver(payloadJson); },
                setAudioMuted: function(isMuted) { AndroidNativeBridge.setAudioMuted(isMuted); },
                exitGameToNativeHome: function() { AndroidNativeBridge.exitGameToNativeHome(); },
                showNativeToast: function(message) { AndroidNativeBridge.showNativeToast(message); }
              };
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

        webChromeClient = WebChromeClient()
    }
}
