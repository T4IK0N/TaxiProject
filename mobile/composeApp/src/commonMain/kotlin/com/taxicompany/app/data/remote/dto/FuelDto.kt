package com.taxicompany.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FuelLogDto(
    val id: String,
    val driver: String,
    val driverName: String? = null,
    val vehicle: String,
    val vehiclePlate: String? = null,
    val shift: String? = null,
    val fuelType: String,
    val liters: String, // DecimalField -> string, patrz VehicleDto
    val pricePerLiter: String,
    val totalCost: String,
    val odometerKm: Int,
    val kmSinceLastFillup: Int? = null,
    val consumptionPer100km: String? = null,
    val stationName: String = "",
    val invoiceNumber: String = "",
    val receiptFile: String? = null,
    val fueledAt: String,
)

@Serializable
data class FuelLogCreateDto(
    val vehicle: String,
    val shift: String? = null,
    val fuelType: String,
    val liters: String,
    val pricePerLiter: String,
    val odometerKm: Int? = null,
    val kmDrivenSinceLastFillup: Int? = null,
    val stationName: String = "",
    val invoiceNumber: String = "",
    val fueledAt: String,
    // driver jest wymagany tylko gdy zglasza dyspozytor/admin w imieniu
    // kierowcy - patrz FuelLogCreateView w backendzie. Sam kierowca
    // nie podaje tego pola, bo system uzyje jego wlasnego profilu.
    val driver: String? = null,
)

@Serializable
data class FuelSummaryDto(
    val fillupCount: Int,
    val totalLiters: String,
    val totalCost: String,
    val logs: List<FuelLogDto> = emptyList(),
)
