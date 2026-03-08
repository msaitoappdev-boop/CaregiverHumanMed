import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("maven-publish")
}

android {
    namespace = "com.msaitodev.quiz.core.domain"
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
    }
    kotlinOptions { jvmTarget = JavaVersion.VERSION_17.toString() }

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
                groupId = "com.msaitodev.quiz"
                artifactId = "quiz-core-domain"
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}

dependencies {
    // 汎用コアを Maven 形式へ
    implementation("com.msaitodev.core:core-ads:1.0.0")
    implementation("com.msaitodev.core:core-common:1.0.0")

    // Hilt (KSP)
    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
