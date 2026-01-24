
plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")        // Hiltのアノテーション処理に必要
}

android {
    namespace = "jp.msaitoappdev.caregiver.humanmed.core"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }
}

dependencies {
    // Coroutine / Lifecycle（必要に応じて）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Hilt（KSP 版）
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // Play Billing（BillingManager が依存）
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // DataStore（Premium 状態の保存など）
    implementation("androidx.datastore:datastore-preferences:1.1.1")
}
