# Keep rules for release, add if needed

# --- BillingClient（公開APIは基本Keepされるが保険として） ---
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# --- Hilt / Dagger ---
-keep class dagger.hilt.internal.** { *; }
-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
-keep class * implements dagger.hilt.EntryPoint

# --- Room（エンティティ/Daoのメタデータ保持） ---
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.* class * { *; }

# --- Kotlinx Serialization（使っているため） ---
-keepattributes *Annotation*, InnerClasses
-keep class kotlinx.serialization.** { *; }
-keep class **$$serializer { *; }
