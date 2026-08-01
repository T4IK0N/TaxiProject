package com.taxicompany.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DriverDocumentDto(
    val id: String,
    val driver: String,
    val docType: String,
    val documentNumber: String = "",
    val issuingAuthority: String = "",
    val issuedAt: String,       // format ISO: "2026-01-15"
    val expiresAt: String,
    val file: String? = null,
    val alertThresholdDays: Int = 30,
    val notes: String = "",
    val isExpired: Boolean = false,
    val daysUntilExpiry: Int? = null,
)

/** Lekki wariant do listy kierowcow - bez zagniezdzonych dokumentow */
@Serializable
data class DriverListItemDto(
    val id: String,
    val fullName: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val status: String,
    val hiredAt: String,
    val hasExpiredDocuments: Boolean = false,
)

/** Pelny wariant - widok szczegolowy / profil wlasny kierowcy */
@Serializable
data class DriverDetailDto(
    val id: String,
    val user: String? = null,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String = "",
    val pesel: String,
    val hiredAt: String,
    val status: String,
    val documents: List<DriverDocumentDto> = emptyList(),
    val hasExpiredDocuments: Boolean = false,
)
