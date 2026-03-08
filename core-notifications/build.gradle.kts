import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp") // KSP導入
    id("maven-publish")
}

android {
    namespace = "com.msaitodev.core.notifications"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.msaitodev.core"
                artifactId = "core-notifications"
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // 汎用コアおよびクイズドメインを Maven 形式で参照
    implementation("com.msaitodev.core:core-common:1.0.0")
    implementation("com.msaitodev.quiz:quiz-core-domain:1.0.0")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Hilt (KSPへ移行)
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    implementation("androidx.core:core-ktx:1.13.1")

    // Testing
    androidTestImplementation("androidx.test.ext:junit-ktx:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.truth:truth:1.4.2")
    androidTestImplementation("org.mockito:mockito-core:5.12.0")
    androidTestImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    androidTestImplementation("org.mockito:mockito-inline:5.2.1")
    androidTestImplementation("androidx.work:work-testing:2.9.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
