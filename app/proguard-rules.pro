# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class com.google.firebase.crashlytics.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.model.* { *; }

# Compose
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    void *;
}
