# Add project-specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in proguard-android-optimize.txt.

# Keep DTO classes intact so Moshi reflection sees the fields.
-keep class com.mtoanng.datastream.data.dto.** { *; }
-keepclassmembers class com.mtoanng.datastream.data.dto.** { <init>(...); }

# Retrofit + OkHttp + Moshi
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class kotlin.Metadata { *; }

# Coroutines (StackTraceRecovery)
-dontwarn kotlinx.coroutines.debug.**
