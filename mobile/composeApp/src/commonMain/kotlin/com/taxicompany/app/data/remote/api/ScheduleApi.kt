package com.taxicompany.app.data.remote.api

import com.taxicompany.app.core.PlatformConfig
import com.taxicompany.app.data.remote.dto.PaginatedResponseDto
import com.taxicompany.app.data.remote.dto.ShiftCreateDto
import com.taxicompany.app.data.remote.dto.ShiftDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ScheduleApi(private val httpClient: HttpClient) {

    private val baseUrl = PlatformConfig.apiBaseUrl

    /**
     * GET /api/v1/scheduling/shifts/?start_at__gte=...&start_at__lte=...
     *
     * dateFrom/dateTo w formacie ISO ("2026-06-23T00:00:00Z") - backend
     * filtruje po DateTimeField, wiec potrzebujemy pelnej daty+czasu,
     * nie tylko daty. Kierowca automatycznie widzi tylko swoje zmiany
     * (filtrowanie po roli dzieje sie w get_queryset w Django, nie tutaj).
     */
    suspend fun getShifts(dateFrom: String? = null, dateTo: String? = null): PaginatedResponseDto<ShiftDto> {
        return httpClient.get("$baseUrl/api/v1/scheduling/shifts/") {
            dateFrom?.let { parameter("start_at__gte", it) }
            dateTo?.let { parameter("start_at__lte", it) }
        }.body()
    }

    suspend fun getShiftById(shiftId: String): ShiftDto {
        return httpClient.get("$baseUrl/api/v1/scheduling/shifts/$shiftId/").body()
    }

    /** POST - tylko dyspozytor/admin (patrz IsDispatcherOrAdmin w backendzie) */
    suspend fun createShift(shift: ShiftCreateDto): ShiftDto {
        return httpClient.post("$baseUrl/api/v1/scheduling/shifts/") {
            contentType(ContentType.Application.Json)
            setBody(shift)
        }.body()
    }
}
