import java.util.Properties
import java.io.FileInputStream

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(FileInputStream(f))
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val admobTestAppId: String? = (project.findProperty("admob.test.app.id") as? String)
val admobProdAppId: String? = localProps.getProperty("admob.app.id")

android {
    namespace = "jp.msaitoappdev.caregiver.humanmed"
    compileSdk = 35

    defaultConfig {
        applicationId = "jp.msaitoappdev.caregiver.humanmed"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }


// buildTypes の manifestPlaceholders も削除
// debug/release の admob_app_id プレースホルダー定義も削除
    buildTypes {
        debug {
//            manifestPlaceholders["admob_app_id"] = admobTestAppId
//                ?: "ca-app-pub-3940256099942544~3347511713" // Google 提供のテスト App ID
        }
        release {
            isMinifyEnabled = false
//            val prodId = admobProdAppId ?: throw GradleException(
//                "Missing admob.app.id in local.properties (e.g. ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy)"
//            )
//            manifestPlaceholders["admob_app_id"] = prodId
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}


dependencies {
    implementation(project(":core"))
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation("androidx.core:core-ktx:1.13.1")

    // Compose（BOM で統一）
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")

    // （重複していたので1つに整理）ui-tooling-preview は上で追加済み
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Compose のマテリアルアイコン
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation / Activity
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Lifecycle / ViewModel / Coroutine
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // AdMob
    // dependencies から削除
    //implementation("com.google.android.gms:play-services-ads:23.6.0")

    // ▼ これを追加（XML の M3 テーマ解決に必要）
    implementation("com.google.android.material:material:1.12.0")

    // --- Hilt（KSP 版） ---
    val hiltVersion = "2.51.1" // 例：必要なら上げてOK（2.58 など）
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    // （任意）Hiltのテスト支援
    testImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hiltVersion")
    kspTest("com.google.dagger:hilt-compiler:$hiltVersion")
    kspAndroidTest("com.google.dagger:hilt-compiler:$hiltVersion")

    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // DataStore（権利フラグのローカル保持）
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // HiltのViewModelをComposeで使う
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // プロセスライフサイクル（起動/復帰での購読状態リフレッシュに使用）
    implementation("androidx.lifecycle:lifecycle-process:2.8.6")

    implementation("androidx.work:work-runtime-ktx:2.9.1")
}
