package com.revamine.games

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.revamine.games.bridge.RevaMineNativeBridge
import com.revamine.games.ui.screens.SplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var webViewRef: WebView? = null
    var isCurrentThemeDark by mutableStateOf(true)
        private set

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instant hardware splash screen handoff (0ms display upon icon tap)
        installSplashScreen()
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Default to dark gaming status & nav bar
        applyDynamicTheme(isDark = true)

        setContent {
            var isSiteLoaded by remember { mutableStateOf(false) }
            var minTimePassed by remember { mutableStateOf(false) }

            // Ensure nice animated splash presentation and fallback timeout
            LaunchedEffect(Unit) {
                delay(1500)
                minTimePassed = true
                delay(6500) // Fallback timeout in case of slow connection
                isSiteLoaded = true
            }

            val showSplash = !(isSiteLoaded && minTimePassed)

            // Handle hardware back press cleanly
            BackHandler {
                if (webViewRef?.canGoBack() == true) {
                    webViewRef?.goBack()
                } else {
                    finish()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // Main Fullscreen Live Game Engine (100% full size, zero resize shifts)
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                @Suppress("DEPRECATION")
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                mediaPlaybackRequiresUserGesture = false
                                cacheMode = WebSettings.LOAD_DEFAULT
                            }

                            setBackgroundColor(Color.parseColor("#070913"))

                            val bridge = RevaMineNativeBridge(
                                activity = this@MainActivity,
                                webView = this,
                                onExitRequested = {
                                    if (canGoBack()) goBack() else finish()
                                }
                            )
                            addJavascriptInterface(bridge, "RevaMineNativeBridge")
                            addJavascriptInterface(bridge, "AndroidNativeBridge")

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    if (url.contains("games.revamine.com") || url.startsWith("https://") || url.startsWith("http://")) {
                                        return false
                                    }
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                                        ctx.startActivity(intent)
                                        true
                                    } catch (e: Exception) {
                                        false
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    // Mark site loaded once web engine finishes
                                    isSiteLoaded = true

                                    // Real-time bidirectional theme sync between web and Android status bar
                                    val syncScript = """
                                        (function() {
                                            function checkTheme() {
                                                var isDark = true;
                                                if (document.documentElement.classList.contains('dark')) {
                                                    isDark = true;
                                                } else if (document.body && document.body.classList.contains('dark')) {
                                                    isDark = true;
                                                } else {
                                                    var stored = localStorage.getItem('theme');
                                                    if (stored === 'light') {
                                                        isDark = false;
                                                    } else if (stored === 'dark') {
                                                        isDark = true;
                                                    } else if (document.body) {
                                                        var bg = window.getComputedStyle(document.body).backgroundColor;
                                                        var match = bg.match(/\d+/g);
                                                        if (match && match.length >= 3) {
                                                            var r = parseInt(match[0]), g = parseInt(match[1]), b = parseInt(match[2]);
                                                            isDark = (r * 0.299 + g * 0.587 + b * 0.114) < 140;
                                                        }
                                                    }
                                                }
                                                if (window.RevaMineNativeBridge && window.RevaMineNativeBridge.updateTheme) {
                                                    window.RevaMineNativeBridge.updateTheme(isDark);
                                                }
                                            }
                                            checkTheme();
                                            var obs = new MutationObserver(checkTheme);
                                            obs.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] });
                                            if (document.body) {
                                                obs.observe(document.body, { attributes: true, attributeFilter: ['class', 'style'] });
                                            }
                                            window.addEventListener('storage', checkTheme);
                                        })();
                                    """.trimIndent()
                                    view?.evaluateJavascript(syncScript, null)
                                }
                            }

                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    if (newProgress >= 90) {
                                        isSiteLoaded = true
                                    }
                                }
                            }

                            loadUrl("https://games.revamine.com/?mode=native")
                            webViewRef = this
                        }
                    }
                )

                // Animated RevaMine Splash Screen shown continuously until site finishes loading
                AnimatedVisibility(
                    visible = showSplash,
                    exit = fadeOut(tween(400))
                ) {
                    SplashScreen(
                        isDarkTheme = isCurrentThemeDark
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyDynamicTheme(isCurrentThemeDark)
        webViewRef?.onResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyDynamicTheme(isCurrentThemeDark)
    }

    /**
     * Dynamically adjusts status bar and navigation bar colors:
     * - Dark Mode: #070913 background with crisp pure white icons
     * - Light Mode: #f4f6f9 background with sharp dark charcoal icons
     */
    fun applyDynamicTheme(isDark: Boolean) {
        runOnUiThread {
            isCurrentThemeDark = isDark
            WindowCompat.setDecorFitsSystemWindows(window, true)
            val barColor = if (isDark) Color.parseColor("#070913") else Color.parseColor("#f4f6f9")
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
            WindowCompat.getInsetsController(window, window.decorView).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webViewRef?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        webViewRef?.destroy()
        webViewRef = null
    }
}
