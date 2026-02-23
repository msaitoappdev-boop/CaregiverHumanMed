include(":quiz-feature-history")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CaregiverHumanMed"
include(":app")
include(":core")
include(":data")
include(":domain")
include(":quiz-feature-review")
include(":quiz-feature-result")
include(":quiz-core-ads")
