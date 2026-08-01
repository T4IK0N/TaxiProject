@file:Suppress("UnstableApiUsage")

// pluginManagement musi byc na samym poczatku - mowi Gradle, gdzie
// szukac pluginow (Android Gradle Plugin, Kotlin Multiplatform Plugin,
// Compose Multiplatform Plugin) zanim jakikolwiek modul zostanie zaladowany
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

// dependencyResolutionManagement mowi, gdzie szukac samych bibliotek
// (zaleznosci uzywanych w kodzie, nie pluginow budujacych projekt)
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TaxiCompanyApp"

// Jedyny modul na razie - composeApp zawiera w sobie wszystkie target
// platformowe (android, desktop) zdefiniowane w jego build.gradle.kts.
// To jest typowy uklad dla nowych projektow Compose Multiplatform -
// nie potrzeba osobnych modulow Gradle "androidApp"/"desktopApp" tak jak
// bywalo w starszych szablonach, bo Kotlin Multiplatform Plugin
// pozwala zdefiniowac wszystkie targety w jednym module.
include(":composeApp")
