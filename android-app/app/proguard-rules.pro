# JavaScript bridge: methods ko R2/R8 rename ya remove na kare
-keepclassmembers class com.revamine.games.bridge.RevaMineNativeBridge {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.revamine.games.bridge.RevaMineNativeBridge { *; }
-keepattributes JavascriptInterface
-keepattributes *Annotation*
