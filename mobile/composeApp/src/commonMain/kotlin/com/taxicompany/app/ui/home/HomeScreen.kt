package com.taxicompany.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tymczasowy ekran glowny - potwierdza, ze logowanie zadzialalo
 * i token jest zapisany. Zostanie zastapiony realnym dashboardem
 * (grafik, tankowania, lista pojazdow) w kolejnym etapie.
 */
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Zalogowano poprawnie",
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Tu pojawi się grafik, tankowania i dane pojazdów.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            Button(onClick = onLogout) {
                Text("Wyloguj się")
            }
        }
    }
}
