import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("maven-publish")
}

android {
    namespace = "com.msaitodev.core.ads"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        buildConfig = true
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
                artifactId = "core-ads"
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // ローカルMavenインストール済みのcore-commonを参照
    implementation("com.msaitodev.core:core-common:1.0.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.google.android.gms:play-services-ads:22.6.0")
    implementation("com.google.android.ump:user-messaging-platform:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")
}
