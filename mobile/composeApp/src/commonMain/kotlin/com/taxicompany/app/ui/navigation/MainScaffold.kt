package com.taxicompany.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.ui.unit.dp

@Composable
fun MainScaffold(
    navController: NavHostController,
    currentRoute: Route,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isDesktop = maxWidth >= 700.dp

        val drawerContent = @Composable {
            PermanentDrawerSheet(
                modifier = Modifier
                    .width(250.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Taxi Company",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = { Text("Grafik") },
                    selected = currentRoute == Route.Schedule,
                    onClick = { navigateToTab(navController, Route.Schedule) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.LocalGasStation, contentDescription = null) },
                    label = { Text("Tankowania") },
                    selected = currentRoute == Route.Fuel,
                    onClick = { navigateToTab(navController, Route.Fuel) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    label = { Text("Profil") },
                    selected = currentRoute == Route.Profile,
                    onClick = { navigateToTab(navController, Route.Profile) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }

        // 2. Jeśli to desktop, owijamy CAŁY Scaffold w PermanentNavigationDrawer
        if (isDesktop) {
            PermanentNavigationDrawer(
                drawerContent = drawerContent
            ) {
                // To jest ciało Drawera – wszystko tutaj wyświetli się OBOK panelu bocznego
                AppScaffold(isDesktop = true, currentRoute = currentRoute, navController = navController, content = content)
            }
        } else {
            // Jeśli to telefon, wyświetlamy sam Scaffold (bez bocznego menu)
            AppScaffold(isDesktop = false, currentRoute = currentRoute, navController = navController, content = content)
        }
    }
}

// 3. Wydzielony Scaffold, który teraz poprawnie dostosowuje się do platformy
@Composable
private fun AppScaffold(
    isDesktop: Boolean,
    currentRoute: Route,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            // Dolny pasek pokazuje się tylko, gdy NIE jesteśmy na desktopie
            if (!isDesktop) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Route.Schedule,
                        onClick = { navigateToTab(navController, Route.Schedule) },
                        icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                        label = { Text("Grafik") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Fuel,
                        onClick = { navigateToTab(navController, Route.Fuel) },
                        icon = { Icon(Icons.Filled.LocalGasStation, contentDescription = null) },
                        label = { Text("Tankowania") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Profile,
                        onClick = { navigateToTab(navController, Route.Profile) },
                        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                        label = { Text("Profil") },
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

private fun navigateToTab(navController: NavHostController, route: Route) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}