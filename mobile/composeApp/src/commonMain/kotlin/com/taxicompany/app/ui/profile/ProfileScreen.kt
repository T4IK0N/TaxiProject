package com.taxicompany.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taxicompany.app.data.remote.dto.DriverDetailDto
import com.taxicompany.app.data.remote.dto.DriverDocumentDto
import com.taxicompany.app.ui.theme.AppTheme
import com.taxicompany.app.ui.theme.decorated
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinInject(),
) {
    val state = viewModel.uiState

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            state.isLoading -> LoadingState()
            state.errorMessage != null -> ErrorState(
                message = state.errorMessage,
                onRetry = viewModel::loadProfile,
                onLogout = onLogout,
            )
            state.profile != null -> ProfileContent(
                profile = state.profile,
                onLogout = onLogout,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Spróbuj ponownie") }
        Spacer(modifier = Modifier.height(8.dp))
        // Krytyczne: bez tego przycisku, jesli profil nigdy sie nie
        // zaladuje (np. niewazny/przeterminowany token po wyczyszczeniu
        // bazy danych backendu), uzytkownik jest uwieziony na tym ekranie
        // bez zadnej drogi wyjscia poza recznym czyszczeniem danych apki
        // w ustawieniach systemowych. Wylogowanie zawsze musi byc dostepne.
        OutlinedButton(onClick = onLogout) { Text("Wyloguj się") }
    }
}

@Composable
private fun ProfileContent(profile: DriverDetailDto, onLogout: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            Text(profile.fullName, style = MaterialTheme.typography.headlineSmall)
            Text(
                statusLabel(profile.status),
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor(profile.status),
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )

            InfoRow(label = "Telefon", value = profile.phoneNumber)
            if (profile.email.isNotBlank()) {
                InfoRow(label = "E-mail", value = profile.email)
            }
            InfoRow(label = "Zatrudniony od", value = profile.hiredAt)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Dokumenty", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (profile.documents.isEmpty()) {
            item {
                Text(
                    "Brak zarejestrowanych dokumentów.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            items(profile.documents) { document ->
                DocumentCard(document)
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Wyloguj się")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.4f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DocumentCard(document: DriverDocumentDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                docTypeLabel(document.docType),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Wygasa: ${document.expiresAt}",
                    style = MaterialTheme.typography.decorated,
                    modifier = Modifier.fillMaxWidth(0.6f),
                )
                Text(
                    documentStatusLabel(document),
                    style = MaterialTheme.typography.decorated,
                    color = documentStatusColor(document),
                )
            }
        }
    }
}

private fun docTypeLabel(docType: String): String = when (docType) {
    "taxi_license" -> "Licencja na wykonywanie transportu taksówką"
    "driving_license" -> "Prawo jazdy"
    "psychological_test" -> "Badania psychologiczne"
    "occupational_medicine" -> "Medycyna pracy"
    "medical_exam" -> "Badania lekarskie"
    "criminal_record" -> "Zapytanie o niekaralność"
    else -> "Inny dokument"
}

private fun documentStatusLabel(document: DriverDocumentDto): String {
    if (document.isExpired) return "Wygasł"
    val days = document.daysUntilExpiry
    return if (days != null && days <= document.alertThresholdDays) "Wygasa za $days dni" else "Aktualny"
}

@Composable
private fun documentStatusColor(document: DriverDocumentDto): Color {
    val days = document.daysUntilExpiry
    return when {
        document.isExpired -> MaterialTheme.colorScheme.error
        days != null && days <= document.alertThresholdDays -> Color(0xFFB8860B)
        else -> MaterialTheme.colorScheme.primary
    }
}

private fun statusLabel(status: String): String = when (status) {
    "active" -> "Aktywny"
    "suspended" -> "Zawieszony"
    "inactive" -> "Nieaktywny"
    "terminated" -> "Zwolniony"
    else -> status
}

@Composable
private fun statusColor(status: String): Color = when (status) {
    "suspended", "terminated" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}
