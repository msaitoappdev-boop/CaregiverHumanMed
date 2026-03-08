import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("maven-publish")
}

android {
    namespace = "com.msaitodev.core.common"
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
                groupId = "com.msaitodev.core"
                artifactId = "core-common"
                version = "1.0.0"
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    api("androidx.datastore:datastore-preferences:1.1.1")
    api("com.android.billingclient:billing-ktx:7.1.1")

    val hiltVersion = "2.51.1"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.13")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.google.truth:truth:1.1.5")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
