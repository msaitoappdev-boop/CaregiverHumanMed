plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")        // Room KSP
    id("com.google.dagger.hilt.android") // Hilt（@Module があるなら付与）
}

android {
    namespace = "jp.msaitoappdev.caregiver.humanmed.data"
    compileSdk = 35
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }
}

ksp {
    // ★ Room のスキーマ出力先は data モジュールへ
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    val room = "2.6.1"
    // AppDatabase が RoomDatabase を継承 = 公開APIに露出するので api で公開
    api("androidx.room:room-runtime:$room")
    api("androidx.room:room-ktx:$room")
    ksp("androidx.room:room-compiler:$room")

    // Hilt（Repositoryや@Moduleを置くなら）
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // JSON（assets 質問読み込みに必要なら）
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}
