package com.taxicompany.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.taxicompany.app.data.repository.AuthRepository
import com.taxicompany.app.di.appModules
import com.taxicompany.app.ui.navigation.AppNavHost
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.Module

@Composable
fun App(platformModule: Module) {
    KoinApplication(application = { modules(appModules + platformModule) }) {
        MaterialTheme {
            val authRepository = koinInject<AuthRepository>()
            AppNavHost(authRepository = authRepository)
        }
    }
}