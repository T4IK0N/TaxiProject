// Plik na poziomie roota tylko REJESTRUJE pluginy (apply false) tak,
// zeby byly dostepne do uzycia w composeApp/build.gradle.kts bez
// duplikowania numerow wersji. Same pluginy nie sa tu "aplikowane",
// bo root projekt nie ma kodu Kotlin do skompilowania - to robi
// wylacznie modul composeApp.
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
    alias(libs.plugins.sqldelight) apply false
}
