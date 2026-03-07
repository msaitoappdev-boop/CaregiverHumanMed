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
include(":core-common")
include(":quiz-core-data")
include(":quiz-core-domain")
include(":quiz-feature-review")
include(":quiz-feature-result")
include(":core-ads")
include(":feature-billing")
include(":feature-settings")
include(":core-notifications")
include(":quiz-feature-main")
include(":quiz-feature-history")
include(":quiz-feature-analysis")
include(":core-navigation")
include(":quiz-core-navigation")
include(":core-cloud-sync")
