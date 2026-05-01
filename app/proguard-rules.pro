# Readium Kotlin Toolkit Core Rules
-keep class org.readium.** { *; }

# Readium Navigator (WebView) Specific Rules
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# Joda-Time (Often used by Readium dependencies)
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString
-dontwarn org.joda.time.**

# Coil (Image Loading)
-keep class coil.** { *; }
-dontwarn coil.**

# Hilt
-keep class com.google.dagger.** { *; }
-dontwarn com.google.dagger.**

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Android PDF Viewer
-keep class com.github.barteksc.pdfviewer.** { *; }
