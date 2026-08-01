package com.taxicompany.app.data.repository

import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.core.safeApiCall
import com.taxicompany.app.data.remote.api.ScheduleApi
import com.taxicompany.app.data.remote.dto.ShiftCreateDto
import com.taxicompany.app.data.remote.dto.ShiftDto

class ScheduleRepository(private val scheduleApi: ScheduleApi) {

    suspend fun getShifts(dateFrom: String? = null, dateTo: String? = null): ApiResult<List<ShiftDto>> {
        return safeApiCall { scheduleApi.getShifts(dateFrom, dateTo).results }
    }

    suspend fun getShiftById(shiftId: String): ApiResult<ShiftDto> {
        return safeApiCall { scheduleApi.getShiftById(shiftId) }
    }

    suspend fun createShift(
        driverId: String,
        vehicleId: String,
        startAt: String,
        endAt: String,
        notes: String = "",
    ): ApiResult<ShiftDto> {
        return safeApiCall {
            scheduleApi.createShift(
                ShiftCreateDto(driver = driverId, vehicle = vehicleId, startAt = startAt, endAt = endAt, notes = notes)
            )
        }
    }
}
