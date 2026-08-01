package com.taxicompany.app.ui.fuel

import com.taxicompany.app.data.remote.dto.FuelLogDto
import com.taxicompany.app.data.remote.dto.VehicleListItemDto

enum class OdometerInputMode { ABSOLUTE, DISTANCE_TRAVELED }

data class FuelUiState(
    val isLoading: Boolean = true,
    val fuelLogs: List<FuelLogDto> = emptyList(),
    val vehicles: List<VehicleListItemDto> = emptyList(),
    val errorMessage: String? = null,

    // formularz "nowe tankowanie"
    val showAddForm: Boolean = false,
    val formVehicleId: String? = null,
    val formFuelType: String = "petrol_95",
    val formLiters: String = "",
    val formPricePerLiter: String = "",
    val formOdometerMode: OdometerInputMode = OdometerInputMode.ABSOLUTE,
    val formOdometerValue: String = "",  // znaczenie zalezy od formOdometerMode
    val formStationName: String = "",
    val isSaving: Boolean = false,
    val formError: String? = null,
)
