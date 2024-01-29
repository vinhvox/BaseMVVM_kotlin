pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "BaseMVVM"
include(":app")
include(":core:database")
include(":core:ui")
include(":feature:home")
include(":core:utils")
include(":core:navigation")
