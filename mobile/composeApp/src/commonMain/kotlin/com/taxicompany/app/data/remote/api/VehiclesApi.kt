package com.taxicompany.app.data.remote.api

import com.taxicompany.app.core.PlatformConfig
import com.taxicompany.app.data.remote.dto.MaintenanceLogDto
import com.taxicompany.app.data.remote.dto.PaginatedResponseDto
import com.taxicompany.app.data.remote.dto.VehicleDetailDto
import com.taxicompany.app.data.remote.dto.VehicleDocumentDto
import com.taxicompany.app.data.remote.dto.VehicleListItemDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class VehiclesApi(private val httpClient: HttpClient) {

    private val baseUrl = PlatformConfig.apiBaseUrl

    /** GET /api/v1/vehicles/ - kazdy zalogowany moze przegladac flote */
    suspend fun listVehicles(
        status: String? = null,
        ownershipType: String? = null,
    ): PaginatedResponseDto<VehicleListItemDto> {
        return httpClient.get("$baseUrl/api/v1/vehicles/") {
            status?.let { parameter("status", it) }
            ownershipType?.let { parameter("ownership_type", it) }
        }.body()
    }

    /** GET /api/v1/vehicles/<id>/ */
    suspend fun getVehicle(vehicleId: String): VehicleDetailDto {
        return httpClient.get("$baseUrl/api/v1/vehicles/$vehicleId/").body()
    }

    /** GET /api/v1/vehicles/<vehicleId>/documents/ - OC, AC, przeglad techniczny */
    suspend fun getVehicleDocuments(vehicleId: String): PaginatedResponseDto<VehicleDocumentDto> {
        return httpClient.get("$baseUrl/api/v1/vehicles/$vehicleId/documents/").body()
    }

    /** GET /api/v1/vehicles/<vehicleId>/maintenance/ */
    suspend fun getMaintenanceLogs(vehicleId: String): PaginatedResponseDto<MaintenanceLogDto> {
        return httpClient.get("$baseUrl/api/v1/vehicles/$vehicleId/maintenance/").body()
    }
}
