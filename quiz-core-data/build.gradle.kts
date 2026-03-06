plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.msaitodev.quiz.core.data"
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

ksp {
    arg("room.schemaLocation", "$projectDir/build/roomSchemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":quiz-core-domain"))
    implementation(project(":core-cloud-sync"))

    // Billing (core-common とバージョンを 7.1.1 に統一)
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    val room = "2.6.1"
    implementation("androidx.room:room-runtime:${room}")
    implementation("androidx.room:room-ktx:${room}")
    ksp("androidx.room:room-compiler:$room")

    // Hilt
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    // Android Test dependencies
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("org.mockito:mockito-android:5.12.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    androidTestImplementation("androidx.room:room-testing:$room")
    androidTestImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
