package com.revamine.games

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.revamine.games.bridge.RevaMineNativeBridge

/**
 * GameActivity (WebView Integration)
 * Renders the cloud web game with hardware acceleration and Native Bridge attached.
 */
class GameActivity : ComponentActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lock to Portrait mode for consistent gameplay
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                @Suppress("DEPRECATION")
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
        }

        setContentView(webView)

        // Attach RevaMine Native Bridge
        val bridge = RevaMineNativeBridge(this)
        webView.addJavascriptInterface(bridge, "RevaMineNativeBridge")
        webView.addJavascriptInterface(bridge, "AndroidNativeBridge")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::webView.isInitialized && webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    finish()
                }
            }
        })

        val gameUrl = intent.getStringExtra("GAME_URL")
            ?: intent.getStringExtra("extra_game_url")
            ?: "https://games.revamine.com"
        webView.loadUrl(gameUrl)
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }
}
