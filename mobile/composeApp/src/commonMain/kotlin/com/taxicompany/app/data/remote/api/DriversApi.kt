package com.taxicompany.app.data.remote.api

import com.taxicompany.app.core.PlatformConfig
import com.taxicompany.app.data.remote.dto.DriverDetailDto
import com.taxicompany.app.data.remote.dto.DriverDocumentDto
import com.taxicompany.app.data.remote.dto.DriverListItemDto
import com.taxicompany.app.data.remote.dto.PaginatedResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class DriversApi(private val httpClient: HttpClient) {

    private val baseUrl = PlatformConfig.apiBaseUrl

    /** GET /api/v1/drivers/ - tylko dla staff (dyspozytor/admin), patrz backend */
    suspend fun listDrivers(search: String? = null): PaginatedResponseDto<DriverListItemDto> {
        return httpClient.get("$baseUrl/api/v1/drivers/") {
            search?.let { parameter("search", it) }
        }.body()
    }

    /** GET /api/v1/drivers/me/ - profil zalogowanego kierowcy */
    suspend fun getMyProfile(): DriverDetailDto {
        return httpClient.get("$baseUrl/api/v1/drivers/me/").body()
    }

    /** GET /api/v1/drivers/<id>/ */
    suspend fun getDriver(driverId: String): DriverDetailDto {
        return httpClient.get("$baseUrl/api/v1/drivers/$driverId/").body()
    }

    /** GET /api/v1/drivers/<driverId>/documents/ */
    suspend fun getDriverDocuments(driverId: String): PaginatedResponseDto<DriverDocumentDto> {
        return httpClient.get("$baseUrl/api/v1/drivers/$driverId/documents/").body()
    }
}
