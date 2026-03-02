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
include(":quiz-core-common")
include(":quiz-core-data")
include(":quiz-core-domain")
include(":quiz-feature-review")
include(":quiz-feature-result")
include(":core-ads")
include(":feature-billing")
include(":quiz-feature-settings")
include(":quiz-core-notifications")
include(":quiz-feature-main")
include(":quiz-core-navigation")
