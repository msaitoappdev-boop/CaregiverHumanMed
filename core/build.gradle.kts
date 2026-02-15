plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")        // Hiltのアノテーション処理に必要
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "jp.msaitoappdev.caregiver.humanmed.core"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }
}

dependencies {
    implementation(project(":domain"))
    // Coroutine / Lifecycle（必要に応じて）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // ★ DataStore 型が公開API（DIコンストラクタ）に露出するため api で公開
    api("androidx.datastore:datastore-preferences:1.1.1")

    // Play Billing（BillingManager が依存）
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // Hilt（KSP 版）
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.13")

    // Android Test dependencies
    androidTestImplementation("androidx.test:runner:1.5.2") // <-- 追加
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
