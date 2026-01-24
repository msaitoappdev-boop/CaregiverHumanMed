plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "jp.msaitoappdev.caregiver.humanmed.domain"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }
}

dependencies {
    // Flow（kotlinx.coroutines）のみを追加
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
}
