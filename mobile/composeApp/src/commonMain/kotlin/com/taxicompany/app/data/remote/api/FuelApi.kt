package com.taxicompany.app.data.remote.api

import com.taxicompany.app.core.PlatformConfig
import com.taxicompany.app.data.remote.dto.FuelLogCreateDto
import com.taxicompany.app.data.remote.dto.FuelLogDto
import com.taxicompany.app.data.remote.dto.FuelSummaryDto
import com.taxicompany.app.data.remote.dto.PaginatedResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class FuelApi(private val httpClient: HttpClient) {

    private val baseUrl = PlatformConfig.apiBaseUrl

    /** GET /api/v1/fuel/logs/ - kierowca widzi tylko swoje (filtr w Django) */
    suspend fun getFuelLogs(vehicleId: String? = null): PaginatedResponseDto<FuelLogDto> {
        return httpClient.get("$baseUrl/api/v1/fuel/logs/") {
            vehicleId?.let { parameter("vehicle", it) }
        }.body()
    }

    suspend fun getFuelLogById(fuelLogId: String): FuelLogDto {
        return httpClient.get("$baseUrl/api/v1/fuel/logs/$fuelLogId/").body()
    }

    /**
     * POST /api/v1/fuel/logs/create/ - kierowca wpisuje dane po
     * zatankowaniu. Backend (apps.fuel.services.create_fuel_log)
     * sam wylicza total_cost i consumption_per_100km - klient
     * wysyla tylko surowe dane wejsciowe, nigdy wyliczen.
     */
    suspend fun createFuelLog(fuelLog: FuelLogCreateDto): FuelLogDto {
        return httpClient.post("$baseUrl/api/v1/fuel/logs/create/") {
            contentType(ContentType.Application.Json)
            setBody(fuelLog)
        }.body()
    }

    /**
     * GET /api/v1/fuel/my-summary/?date_from=...&date_to=...
     * Odpowiada na wymaganie "kierowca sprawdza dane do faktury" -
     * suma litrow/kosztow/tankowan w danym okresie (np. miesiac).
     */
    suspend fun getMySummary(dateFrom: String, dateTo: String): FuelSummaryDto {
        return httpClient.get("$baseUrl/api/v1/fuel/my-summary/") {
            parameter("date_from", dateFrom)
            parameter("date_to", dateTo)
        }.body()
    }
}
