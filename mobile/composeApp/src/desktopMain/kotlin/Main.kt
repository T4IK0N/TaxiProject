package com.taxicompany.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.taxicompany.app.ui.App

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Taxi Company App") {
        // App() inicjalizuje Koin wewnetrznie przez KoinApplication
        // composable - nie trzeba wolac startKoin() recznie tutaj.
        App()
    }
}
