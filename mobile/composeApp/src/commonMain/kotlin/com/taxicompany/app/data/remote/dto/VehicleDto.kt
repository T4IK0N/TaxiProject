package com.taxicompany.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VehicleDocumentDto(
    val id: String,
    val vehicle: String,
    val docType: String,
    val issuedAt: String,
    val expiresAt: String,
    val file: String? = null,
    val alertThresholdDays: Int = 30,
    val notes: String = "",
    val isExpired: Boolean = false,
)

@Serializable
data class MaintenanceLogDto(
    val id: String,
    val vehicle: String,
    val serviceType: String,
    val description: String,
    val serviceDate: String,
    val odometerKm: Int,
    val cost: String,           // DecimalField z DRF przychodzi jako string ("1234.50")
    val workshopName: String = "",
    val invoiceFile: String? = null,
    val nextServiceDueKm: Int? = null,
    val nextServiceDueDate: String? = null,
)

@Serializable
data class VehicleListItemDto(
    val id: String,
    val plateNumber: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: String,
    val ownershipType: String,
    val status: String,
    val odometerKm: Int,
)

@Serializable
data class VehicleDetailDto(
    val id: String,
    val plateNumber: String,
    val brand: String,
    val model: String,
    val year: Int,
    val fuelType: String,
    val ownershipType: String,
    val ownerDriver: String? = null,
    val status: String,
    val odometerKm: Int,
    val manufacturerAvgConsumption: String? = null,
    val documents: List<VehicleDocumentDto> = emptyList(),
    val maintenanceLogs: List<MaintenanceLogDto> = emptyList(),
)
