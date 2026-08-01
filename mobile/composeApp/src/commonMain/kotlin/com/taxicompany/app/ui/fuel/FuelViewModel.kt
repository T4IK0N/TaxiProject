package com.taxicompany.app.ui.fuel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.data.repository.AuthRepository
import com.taxicompany.app.data.repository.FuelRepository
import com.taxicompany.app.data.repository.OdometerInput
import com.taxicompany.app.data.repository.VehiclesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * State holder dla ekranu tankowan
 */
class FuelViewModel(
    private val fuelRepository: FuelRepository,
    private val vehiclesRepository: VehiclesRepository,
    private val authRepository: AuthRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var uiState by mutableStateOf(FuelUiState())
        private set

    init {
        loadData()
    }

    fun retry() = loadData()

    private fun loadData() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        scope.launch {
            // Lista pojazdow jest potrzebna do formularza (wybor, ktore
            // auto tankujemy) - ladujemy ja razem z lista tankowan, bo
            // obie sa wymagane, zeby ekran mial sens.
            val logsResult = fuelRepository.getFuelLogs()
            val vehiclesResult = vehiclesRepository.listVehicles()

            val authErrorOccurred = listOf(logsResult, vehiclesResult)
                .filterIsInstance<ApiResult.Error>()
                .any { it.isAuthError }
            if (authErrorOccurred) {
                authRepository.notifyAuthError()
            }

            when {
                logsResult is ApiResult.Success && vehiclesResult is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        fuelLogs = logsResult.data.sortedByDescending { it.fueledAt },
                        vehicles = vehiclesResult.data,
                    )
                }
                logsResult is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = logsResult.message)
                }
                vehiclesResult is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = vehiclesResult.message)
                }
            }
        }
    }

    fun openAddForm() {
        uiState = uiState.copy(
            showAddForm = true,
            formVehicleId = uiState.vehicles.firstOrNull()?.id,
            formFuelType = "petrol_95",
            formLiters = "",
            formPricePerLiter = "",
            formOdometerMode = OdometerInputMode.ABSOLUTE,
            formOdometerValue = "",
            formStationName = "",
            formError = null,
        )
    }

    fun dismissAddForm() {
        uiState = uiState.copy(showAddForm = false)
    }

    fun onFormVehicleSelected(vehicleId: String) {
        uiState = uiState.copy(formVehicleId = vehicleId, formError = null)
    }

    fun onFormFuelTypeSelected(fuelType: String) {
        uiState = uiState.copy(formFuelType = fuelType, formError = null)
    }

    fun onFormLitersChange(value: String) {
        uiState = uiState.copy(formLiters = value, formError = null)
    }

    fun onFormPriceChange(value: String) {
        uiState = uiState.copy(formPricePerLiter = value, formError = null)
    }

    fun onOdometerModeChanged(mode: OdometerInputMode) {
        uiState = uiState.copy(formOdometerMode = mode, formOdometerValue = "", formError = null)
    }

    fun onFormOdometerValueChange(value: String) {
        uiState = uiState.copy(formOdometerValue = value, formError = null)
    }

    fun onFormStationChange(value: String) {
        uiState = uiState.copy(formStationName = value)
    }

    fun onSaveClick() {
        val vehicleId = uiState.formVehicleId
        if (vehicleId == null) {
            uiState = uiState.copy(formError = "Wybierz pojazd.")
            return
        }

        val liters = uiState.formLiters.replace(",", ".").toDoubleOrNull()
        if (liters == null || liters <= 0) {
            uiState = uiState.copy(formError = "Podaj poprawną liczbę litrów.")
            return
        }

        val price = uiState.formPricePerLiter.replace(",", ".").toDoubleOrNull()
        if (price == null || price <= 0) {
            uiState = uiState.copy(formError = "Podaj poprawną cenę za litr.")
            return
        }

        val odometerValue = uiState.formOdometerValue.toIntOrNull()
        if (odometerValue == null || odometerValue < 0) {
            val message = when (uiState.formOdometerMode) {
                OdometerInputMode.ABSOLUTE -> "Podaj poprawny stan licznika."
                OdometerInputMode.DISTANCE_TRAVELED -> "Podaj poprawną liczbę przejechanych kilometrów."
            }
            uiState = uiState.copy(formError = message)
            return
        }

        val odometerInput = when (uiState.formOdometerMode) {
            OdometerInputMode.ABSOLUTE -> OdometerInput.AbsoluteReading(odometerValue)
            OdometerInputMode.DISTANCE_TRAVELED -> OdometerInput.DistanceSinceLastFillup(odometerValue)
        }

        uiState = uiState.copy(isSaving = true, formError = null)

        scope.launch {
            val nowIso = kotlin.time.Clock.System.now().toString()
            val result = fuelRepository.createFuelLog(
                vehicleId = vehicleId,
                fuelType = uiState.formFuelType,
                liters = liters.toString(),
                pricePerLiter = price.toString(),
                odometerInput = odometerInput,
                fueledAt = nowIso,
                stationName = uiState.formStationName,
            )
            when (result) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isSaving = false, showAddForm = false)
                    loadData() // odswiez liste, zeby nowy wpis (i wyliczone spalanie) byl widoczny
                }
                is ApiResult.Error -> {
                    if (result.isAuthError) {
                        authRepository.notifyAuthError()
                    }
                    uiState = uiState.copy(isSaving = false, formError = result.message)
                }
            }
        }
    }
}
