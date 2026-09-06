# Proguard rules for SEEMS-AI
-keep class org.forestdept.seemsai.model.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
