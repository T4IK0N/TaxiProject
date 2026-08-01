package com.taxicompany.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShiftDto(
    val id: String,
    val driver: String,
    val driverName: String? = null,
    val vehicle: String,
    val vehiclePlate: String? = null,
    val startAt: String, // ISO datetime: "2026-06-20T08:00:00Z"
    val endAt: String,
    val status: String,
    val odometerStartKm: Int? = null,
    val odometerEndKm: Int? = null,
    val durationHours: Double? = null,
    val kmDriven: Int? = null,
    val notes: String = "",
    val createdBy: String? = null,
)

/** Body POST /api/v1/scheduling/shifts/ - bez pol read-only */
@Serializable
data class ShiftCreateDto(
    val driver: String,
    val vehicle: String,
    val startAt: String,
    val endAt: String,
    val notes: String = "",
)
