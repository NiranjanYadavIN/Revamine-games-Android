package com.revamine.games

import android.app.Application
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RevaMineApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Google Mobile Ads SDK ko background thread par init karo (blocking nahi hona chahiye).
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@RevaMineApplication) {}
        }
    }
}
