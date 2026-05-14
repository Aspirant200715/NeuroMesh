-keep class com.neuromesh.crisis.data.model.** { *; }
-keep class com.neuromesh.crisis.data.local.entity.** { *; }
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }

# Room
-keep @androidx.room.* class * { *; }

# MediaPipe
-keep class com.google.mediapipe.** { *; }
-keep class com.google.mediapipe.tasks.genai.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# Nearby
-keep class com.google.android.gms.nearby.** { *; }
