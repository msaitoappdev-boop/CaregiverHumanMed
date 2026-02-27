plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")        // Room KSP
    id("com.google.dagger.hilt.android") // Hilt（@Module があるなら付与）
}

// 強制再同期のためのコメント

android {
    namespace = "jp.msaitoappdev.caregiver.humanmed.data"
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
    // ★ Room のスキーマ出力先は data モジュールへ
    // ビルド配下へ出力（毎回 clean で消えるため“空ファイルの残骸”問題を避けやすい）
    arg("room.schemaLocation", "$projectDir/build/roomSchemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":quiz-core-common"))
    implementation(project(":quiz-core-domain"))

    // Billing
    implementation("com.android.billingclient:billing-ktx:6.2.1")

    val room = "2.6.1"
    // AppDatabase が RoomDatabase を継承 = 公開APIに露出するので api で公開
    implementation("androidx.room:room-runtime:${room}")
    implementation("androidx.room:room-ktx:${room}")
    ksp("androidx.room:room-compiler:$room")

    // Hilt（Repositoryや@Moduleを置くなら）
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // JSON（assets 質問読み込みに必要なら）
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Firebase
    val fbBom = platform("com.google.firebase:firebase-bom:32.7.4")
    implementation(fbBom)
    testImplementation(fbBom)
    androidTestImplementation(fbBom)
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

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
    androidTestImplementation("org.mockito:mockito-android:5.12.0") // <-- 追加
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    androidTestImplementation("androidx.room:room-testing:$room")
    androidTestImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
