package com.taxicompany.app.ui.fuel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taxicompany.app.data.remote.dto.FuelLogDto
import com.taxicompany.app.data.remote.dto.VehicleListItemDto
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(viewModel: FuelViewModel = koinInject()) {
    val state = viewModel.uiState

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openAddForm) {
                Icon(Icons.Filled.Add, contentDescription = "Dodaj tankowanie")
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage, viewModel::retry)
                state.fuelLogs.isEmpty() -> EmptyState()
                else -> FuelLogList(state.fuelLogs)
            }
        }
    }

    if (state.showAddForm) {
        AddFuelLogSheet(viewModel = viewModel)
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
            "Brak zarejestrowanych tankowań. Dodaj pierwsze przyciskiem +.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun FuelLogList(logs: List<FuelLogDto>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(logs) { log -> FuelLogCard(log) }
    }
}


@Composable
private fun FuelLogCard(log: FuelLogDto) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(dateLabel(log.fueledAt), style = MaterialTheme.typography.titleSmall)
                Text(
                    "${log.totalCost} zł",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            InfoLine("Paliwo", fuelTypeLabel(log.fuelType))
            InfoLine(quantityListLabel(log.fuelType), "${log.liters} ${quantityUnitLabel(log.fuelType)}")
            InfoLine(priceFieldLabel(log.fuelType), "${log.pricePerLiter} zł")
            InfoLine("Przebieg", "${log.odometerKm} km")
            log.consumptionPer100km?.let {
                InfoLine("Spalanie", "$it ${consumptionUnitLabel(log.fuelType)}")
            }
            if (log.stationName.isNotBlank()) {
                InfoLine("Stacja", log.stationName)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.4f),
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFuelLogSheet(viewModel: FuelViewModel) {
    val state = viewModel.uiState

    ModalBottomSheet(onDismissRequest = viewModel::dismissAddForm) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Nowe tankowanie", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            VehicleDropdown(
                vehicles = state.vehicles,
                selectedVehicleId = state.formVehicleId,
                onVehicleSelected = viewModel::onFormVehicleSelected,
            )
            Spacer(modifier = Modifier.height(12.dp))

            FuelTypeDropdown(
                selectedFuelType = state.formFuelType,
                onFuelTypeSelected = viewModel::onFormFuelTypeSelected,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.formLiters,
                onValueChange = viewModel::onFormLitersChange,
                label = { Text(quantityFieldLabel(state.formFuelType)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.formPricePerLiter,
                onValueChange = viewModel::onFormPriceChange,
                label = { Text(priceFieldLabel(state.formFuelType)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))

            OdometerInputSection(
                mode = state.formOdometerMode,
                value = state.formOdometerValue,
                onModeChange = viewModel::onOdometerModeChanged,
                onValueChange = viewModel::onFormOdometerValueChange,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.formStationName,
                onValueChange = viewModel::onFormStationChange,
                label = { Text("Stacja (opcjonalnie)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            state.formError?.let { error ->
                Text(
                    error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = viewModel::onSaveClick,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (state.isSaving) "Zapisywanie..." else "Zapisz tankowanie")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OdometerInputSection(
    mode: OdometerInputMode,
    value: String,
    onModeChange: (OdometerInputMode) -> Unit,
    onValueChange: (String) -> Unit,
) {
    Column {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = mode == OdometerInputMode.ABSOLUTE,
                onClick = { onModeChange(OdometerInputMode.ABSOLUTE) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text("Stan licznika")
            }
            SegmentedButton(
                selected = mode == OdometerInputMode.DISTANCE_TRAVELED,
                onClick = { onModeChange(OdometerInputMode.DISTANCE_TRAVELED) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text("Przejechane km")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    when (mode) {
                        OdometerInputMode.ABSOLUTE -> "Stan licznika (km)"
                        OdometerInputMode.DISTANCE_TRAVELED -> "Przejechane km od ostatniego tankowania"
                    }
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VehicleDropdown(
    vehicles: List<VehicleListItemDto>,
    selectedVehicleId: String?,
    onVehicleSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = vehicles.firstOrNull { it.id == selectedVehicleId }?.plateNumber ?: "Wybierz pojazd"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Pojazd") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            // menuAnchor() jest WYMAGANY na TextField wewnatrz ExposedDropdownMenuBox -
            // bez niego klikniecie nie otwiera menu (nieoczywiste, nie jest to
            // udokumentowane jasno w API, latwo to przeoczyc).
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            vehicles.forEach { vehicle ->
                DropdownMenuItem(
                    text = { Text("${vehicle.plateNumber} (${vehicle.brand} ${vehicle.model})") },
                    onClick = {
                        onVehicleSelected(vehicle.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelTypeDropdown(selectedFuelType: String, onFuelTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "petrol_95" to "Benzyna PB95",
        "petrol_98" to "Benzyna PB98",
        "diesel" to "Diesel (ON)",
        "lpg" to "LPG",
        "electric" to "Ładowanie elektryczne (kWh)",
    )
    val selectedLabel = options.firstOrNull { it.first == selectedFuelType }?.second ?: selectedFuelType

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Typ paliwa") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onFuelTypeSelected(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun fuelTypeLabel(fuelType: String): String = when (fuelType) {
    "petrol_95" -> "Benzyna PB95"
    "petrol_98" -> "Benzyna PB98"
    "diesel" -> "Diesel (ON)"
    "lpg" -> "LPG"
    "electric" -> "Ładowanie elektryczne"
    else -> fuelType
}

private fun quantityUnitLabel(fuelType: String): String =
    if (fuelType == "electric") "kWh" else "l"

private fun quantityFieldLabel(fuelType: String): String =
    if (fuelType == "electric") "Energia (kWh)" else "Litry"

private fun priceFieldLabel(fuelType: String): String =
    if (fuelType == "electric") "Cena za kWh" else "Cena za litr"

private fun quantityListLabel(fuelType: String): String =
    if (fuelType == "electric") "Energia" else "Ilość"

private fun consumptionUnitLabel(fuelType: String): String =
    if (fuelType == "electric") "kWh/100km" else "l/100km"

private fun dateLabel(isoDateTime: String): String {
    val dt = kotlin.time.Instant.parse(isoDateTime).toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dt.date.day.toString().padStart(2, '0')
    val month = dt.date.month.number.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')
    return "$day.$month.${dt.date.year} $hour:$minute"
}
