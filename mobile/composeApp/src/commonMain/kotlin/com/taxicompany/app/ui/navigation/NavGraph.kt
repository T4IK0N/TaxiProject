package com.taxicompany.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.taxicompany.app.data.repository.AuthRepository
import com.taxicompany.app.ui.fuel.FuelScreen
import com.taxicompany.app.ui.login.LoginScreen
import com.taxicompany.app.ui.login.LoginViewModel
import com.taxicompany.app.ui.profile.ProfileScreen
import com.taxicompany.app.ui.schedule.ScheduleScreen
import com.taxicompany.app.ui.theme.AppTheme

@Composable
fun AppNavHost(authRepository: AuthRepository) {
    var sessionKey by remember { mutableStateOf(0) }

    key(sessionKey) {
        AppNavHostContent(
            authRepository = authRepository,
            onSessionChanged = { sessionKey++ },
        )
    }
}

@Composable
private fun AppNavHostContent(authRepository: AuthRepository, onSessionChanged: () -> Unit) {
    val navController = rememberNavController()

    val startDestination: Graph = if (authRepository.isLoggedIn()) Graph.Main else Graph.Auth

    val sessionExpired by authRepository.sessionExpired.collectAsState()
    LaunchedEffect(sessionExpired) {
        if (sessionExpired) {
            authRepository.consumeSessionExpiredSignal()
            onSessionChanged()
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        navigation<Graph.Auth>(startDestination = Route.Login) {
            composable<Route.Login> {
                val loginViewModel = remember(navController) {
                    LoginViewModel(
                        authRepository = authRepository,
                        onLoginSuccess = { onSessionChanged() },
                    )
                }
                AppTheme {
                    LoginScreen(viewModel = loginViewModel)
                }
            }
        }

        navigation<Graph.Main>(startDestination = Route.Schedule) {
            composable<Route.Schedule> {
                AppTheme {
                    MainScaffold(navController = navController, currentRoute = Route.Schedule) {
                        ScheduleScreen()
                    }
                }
            }
            composable<Route.Fuel> {
                AppTheme {
                    MainScaffold(navController = navController, currentRoute = Route.Fuel) {
                        FuelScreen()
                    }
                }
            }
            composable<Route.Profile> {
                AppTheme {
                    MainScaffold(navController = navController, currentRoute = Route.Profile) {
                        ProfileScreen(
                            onLogout = {
                                authRepository.logout()
                                onSessionChanged()
                            }
                        )
                    }
                }
            }
        }
    }
}
