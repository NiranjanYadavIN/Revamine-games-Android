package com.revamine.games

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.revamine.games.bridge.RevaMineNativeBridge
import com.revamine.games.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {

    private var webViewRef: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_RevaMineGames)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Match status and navigation bar styling with dark gaming palette
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.parseColor("#070913")
        window.navigationBarColor = Color.parseColor("#070913")

        setContent {
            var showSplash by remember { mutableStateOf(true) }

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
                                domStorageEnabled = true // Required for User Personalization Algorithm
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
                                        return false // Stay inside WebView
                                    }
                                    return try {
                                        val intent = Intent(Intent.ACTION_VIEW, request.url)
                                        ctx.startActivity(intent)
                                        true
                                    } catch (e: Exception) {
                                        false
                                    }
                                }
                            }

                            webChromeClient = WebChromeClient()
                            loadUrl("https://games.revamine.com/?mode=native")
                            webViewRef = this
                        }
                    }
                )

                // Original Animated RevaMine Splash Screen (Centered Logo + Pulsing Dots + 'from RevaMine')
                AnimatedVisibility(
                    visible = showSplash,
                    exit = fadeOut()
                ) {
                    SplashScreen(
                        isDarkTheme = true,
                        onSplashFinished = {
                            showSplash = false
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        webViewRef?.onResume()
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
