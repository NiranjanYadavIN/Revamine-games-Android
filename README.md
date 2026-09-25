# RevaMine Games — Native Android App (Kotlin + Jetpack Compose)

Package: `com.revamine.games`
Min SDK: 26 · Target/Compile SDK: 36 (Google Play ka Aug 31, 2026 wala naya requirement)

## Kya hai isme

- **100% Native Shell** (Kotlin + Jetpack Compose + Material 3): Explore, Categories,
  Favorites, Profile — bottom navigation ke saath. Hero featured banner, search,
  category filters, daily-login streak, dark/light theme toggle.
- **GameStageActivity**: hardware-accelerated fullscreen WebView jo
  `https://games.revamine.com/game/{gameId}?mode=native` load karta hai.
- **RevaMineNativeBridge**: `window.RevaMineNativeBridge` JS bridge — haptics,
  AdMob rewarded/interstitial ads, score submit, exit-to-home, native toast.
  Ye tumhare web project ke `src/utils/nativeBridge.ts` ke saath directly
  compatible hai (maine wo file check ki — already sahi format me hai,
  koi web-side change nahi chahiye).
- **AdMobManager**: rewarded + interstitial ads background me pre-load karta hai
  (Google ke test IDs abhi lage hain).
- **PrefsStore**: favorites, per-game highscores, mute, theme, daily streak —
  SharedPreferences based (Room ki jagah, build simple rakhne ke liye).

## Setup — Android Studio me kaise open karein

1. Android Studio (latest stable, Narwhal ya newer) me **Open** → is
   `android-app` folder ko select karo.
2. Gradle sync hone do (internet chahiye hoga — Gradle 8.13 + dependencies
   download hongi).
3. `app/build.gradle.kts` ke top par 4 AdMob test IDs dikhengi — Play Store
   par release se **PEHLE** apni real AdMob App ID aur Ad Unit IDs se replace
   karna. AdMob console me app register karke IDs milengi.
4. Run karo — emulator ya real device (min Android 8.0 / API 26).

## Jo maine scope se bahar rakha (aur kyun)

Tumhari 3 spec files (`native.md`, `prompt.txt`, `ANDROID_MASTER_PROMPT.md`) me
kuch cheezein thi jo abhi is zip me **nahi** hain, kyunki unke liye external
setup chahiye jo maine khud nahi kar sakta:

- **Firebase Cloud Messaging (push notifications)** — `google-services.json`
  chahiye (Firebase Console se apna project banake download karo), fir bata
  dena, main integrate kar dunga.
- **Google Play Billing (real IAP)** — "Remove Ads" button abhi sirf local flag
  set karta hai (UI demo). Real purchase ke liye Play Console me product
  banana hoga, fir Billing library wire karunga.
- **Offline game caching** (games ka HTML/JS bundle cache karke bina internet
  chalana) — iske liye games.revamine.com par Service Worker / cache headers
  set hone chahiye pehle; abhi sirf game **list** metadata local hai
  (static catalog), games khud internet se load hote hain.

## Test karte waqt

- `GameStageActivity` sirf `games.revamine.com` (aur subdomains) par navigate
  hone deta hai — koi bhi external link automatically system browser me khulega.
- Agar koi game crash ho ya bridge kaam na kare, WebView console errors dekhne
  ke liye Android Studio ke Logcat me `chromium` tag filter karo.
