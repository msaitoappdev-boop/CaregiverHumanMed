# --- 共通の難読化設定 ---
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions

# --- Jetpack Compose & WindowInsets 内部構造の完全保護 ---
# 難読化によって TopAppBar の高さ計算が壊れるのを防ぎます
-keep class androidx.compose.foundation.layout.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.ui.platform.** { *; }
-keep class androidx.core.view.WindowInsetsCompat { *; }
-keep class androidx.core.view.WindowInsetsControllerCompat { *; }

# --- クイズデータモデル & Room エンティティ ---
# @Serializable や @Entity を付与したクラスを難読化から保護します
-keep class com.msaitodev.quiz.core.data.local.dto.** { *; }
-keep @kotlinx.serialization.Serializable class * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase

# --- Kotlinx Serialization ---
-keep class kotlinx.serialization.** { *; }
-keep class **$$serializer { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# --- Hilt / Dagger ---
-keep class dagger.hilt.internal.** { *; }
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keep class * implements dagger.hilt.EntryPoint

# --- Google Play Billing ---
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# --- AdMob / UMP ---
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.ads.**

# --- WorkManager ---
-keep class androidx.work.** { *; }
