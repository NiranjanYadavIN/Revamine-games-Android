package com.revamine.games.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.revamine.games.BuildConfig

/**
 * Singleton jo Rewarded aur Interstitial ads ko background me pre-cache
 * rakhta hai taaki games ko zero-latency par ad mil sake.
 *
 * Abhi Google ke official TEST Ad Unit IDs (BuildConfig se) use ho rahe
 * hain — Play Store release se pehle apne real AdMob IDs se replace karo.
 */
object AdMobManager {

    private const val TAG = "AdMobManager"

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private var isLoadingRewarded = false
    private var isLoadingInterstitial = false

    fun preloadAll(context: Context) {
        loadRewarded(context)
        loadInterstitial(context)
    }

    // ------------------------------------------------------------------ Rewarded
    private fun loadRewarded(context: Context) {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true
        RewardedAd.load(
            context,
            BuildConfig.ADMOB_REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                    isLoadingRewarded = false
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        rewardType: String,
        onSuccess: (rewardType: String, amount: Int) -> Unit,
        onFailure: (reason: String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            onFailure("Ad not ready, please try again shortly")
            loadRewarded(activity)
            return
        }

        var earnedReward = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded(activity)
                if (!earnedReward) onFailure("Ad dismissed or failed to load")
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadRewarded(activity)
                onFailure(error.message ?: "Ad failed to show")
            }
        }

        ad.show(activity) { rewardItem ->
            earnedReward = true
            onSuccess(rewardType, rewardItem.amount)
        }
    }

    // ------------------------------------------------------------------ Interstitial
    private fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onClosed: () -> Unit = {}) {
        val ad = interstitialAd
        if (ad == null) {
            onClosed()
            loadInterstitial(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitial(activity)
                onClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitial(activity)
                onClosed()
            }
        }
        ad.show(activity)
    }
}
