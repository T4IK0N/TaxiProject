package com.taxicompany.app.ui.schedule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.taxicompany.app.data.remote.dto.ShiftDto
import com.taxicompany.app.ui.theme.AppTheme
import com.taxicompany.app.ui.theme.decorated
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = koinInject(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val state = viewModel.uiState

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            WeekSelector(
                weekStart = state.weekStart,
                onPreviousWeek = viewModel::goToPreviousWeek,
                onNextWeek = viewModel::goToNextWeek,
            )

            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage, viewModel::retry)
                state.shifts.isEmpty() -> EmptyState()
                else -> ShiftList(state.shifts)
            }
        }
    }
}

@Composable
private fun WeekSelector(weekStart: LocalDate, onPreviousWeek: () -> Unit, onNextWeek: () -> Unit) {
    val weekEnd = weekStart.plus(6, DateTimeUnit.DAY)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Poprzedni tydzień")
        }
        Text(
            "${formatDate(weekStart)} – ${formatDate(weekEnd)}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(0.dp, 13.dp, 0.dp, 0.dp)
        )
        IconButton(onClick = onNextWeek) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Następny tydzień")
        }
    }
}

@Composable
private fun ShiftList(shifts: List<ShiftDto>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(shifts) { shift -> ShiftCard(shift) }
    }
}

@Composable
private fun ShiftCard(shift: ShiftDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(dayLabel(shift.startAt), style = MaterialTheme.typography.titleSmall)
                Text(
                    statusLabel(shift.status),
                    style = MaterialTheme.typography.decorated,
                    color = statusColor(shift.status),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${timeLabel(shift.startAt)} – ${timeLabel(shift.endAt)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            shift.vehiclePlate?.let { plate ->
                Text(
                    "Pojazd: $plate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            shift.kmDriven?.let { km ->
                Text(
                    "Przejechano: $km km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Spróbuj ponownie") }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "Brak zmian w tym tygodniu.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun statusLabel(status: String): String = when (status) {
    "planned" -> "Zaplanowana"
    "in_progress" -> "W trakcie"
    "completed" -> "Zakończona"
    "cancelled" -> "Odwołana"
    else -> status
}

@Composable
private fun statusColor(status: String): Color = when (status) {
    "in_progress" -> MaterialTheme.colorScheme.primary
    "cancelled" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun dayLabel(isoDateTime: String): String {
    val localDateTime = Instant.parse(isoDateTime).toLocalDateTime(TimeZone.currentSystemDefault())
    val dayNames = listOf("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")
    return dayNames[localDateTime.date.dayOfWeek.isoDayNumber - 1]
}

private fun timeLabel(isoDateTime: String): String {
    val localDateTime = Instant.parse(isoDateTime).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

private fun formatDate(date: LocalDate): String {
    return "${date.dayOfMonth.toString().padStart(2, '0')}.${date.monthNumber.toString().padStart(2, '0')}"
}
