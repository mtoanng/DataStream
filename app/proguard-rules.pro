# Add project-specific ProGuard rules here.
# Applied on top of `proguard-android-optimize.txt` from AGP.
#
# Most rules below only matter for the `release` build (which has
# `isMinifyEnabled = true`). The `debug` build is unaffected.

# ---- Project DTOs (Moshi reflection) ---------------------------------------
# Keep DTO classes + their default constructors so Moshi's reflection adapter
# can instantiate them. Annotation @JsonClass(generateAdapter=true) is a no-op
# at runtime because we rely on KotlinJsonAdapterFactory (reflection).
-keep class com.mtoanng.datastream.data.dto.** { *; }
-keepclassmembers class com.mtoanng.datastream.data.dto.** {
    <init>(...);
}

# ---- Kotlin metadata -------------------------------------------------------
# Required so Moshi's KotlinJsonAdapterFactory can read constructor params.
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
# Belt-and-braces: keep runtime annotations even if a future R8 release
# changes the meaning of `*Annotation*` (it was tightened in R8 8.3+).
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes RuntimeVisibleTypeAnnotations

# ---- Retrofit + OkHttp + Okio ---------------------------------------------
-keepattributes Exceptions, RuntimeVisibleAnnotations, AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Keep Retrofit method/parameter annotations.
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ---- Moshi -----------------------------------------------------------------
# Project currently uses KotlinJsonAdapterFactory (reflection). The rules
# below ALSO cover the case where someone enables Moshi codegen later via
# `kapt(libs.moshi.codegen)` — we want zero R8 surprises either way.
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.JsonAdapter { *; }
-keep class com.squareup.moshi.JsonReader { *; }
-keep class com.squareup.moshi.JsonWriter { *; }
# Generated codegen adapters end with "JsonAdapter" — keep them whole.
-keep class **JsonAdapter { *; }
-keepclassmembers class **JsonAdapter {
    <init>(com.squareup.moshi.Moshi);
    <init>(com.squareup.moshi.Moshi, java.lang.reflect.Type[]);
}
# Any class annotated @JsonClass (even DTOs added by future devs).
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}

# ---- MPAndroidChart --------------------------------------------------------
# JitPack 3.1.0 release; library uses reflection for animations + value formatters.
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ---- Coroutines ------------------------------------------------------------
-dontwarn kotlinx.coroutines.debug.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ---- Timber (drops logs in release via no-op tree) ------------------------
-dontwarn org.jetbrains.annotations.**

# ---- Android components ----------------------------------------------------
# Keep custom views referenced from XML.
-keep public class com.mtoanng.datastream.ui.common.PillarScoreView
-keep public class com.mtoanng.datastream.ui.common.StatusBadge

# Keep ViewModels (instantiated reflectively by ViewModelFactory).
-keep class com.mtoanng.datastream.ui.**.ViewModel { *; }
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Application + Activities + Fragments (already kept by the AGP default
# rules but we re-state for clarity — cheaper than discovering at runtime
# that some shrinker config dropped a Fragment that nav_graph.xml needs).
-keep class com.mtoanng.datastream.DataStreamApp
-keep class com.mtoanng.datastream.ui.login.LoginActivity
-keep class com.mtoanng.datastream.ui.main.MainActivity
-keep class * extends androidx.fragment.app.Fragment

# ---- Misc safety net ------------------------------------------------------
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
