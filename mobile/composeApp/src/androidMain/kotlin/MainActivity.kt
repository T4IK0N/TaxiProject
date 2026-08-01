package com.taxicompany.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.taxicompany.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // App() inicjalizuje Koin wewnetrznie przez KoinApplication
            // composable - nie trzeba wolac startKoin() recznie tutaj.
            App()
        }
    }
}
