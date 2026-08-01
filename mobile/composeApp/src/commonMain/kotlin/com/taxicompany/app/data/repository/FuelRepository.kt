package com.taxicompany.app.data.repository

import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.core.safeApiCall
import com.taxicompany.app.data.remote.api.FuelApi
import com.taxicompany.app.data.remote.dto.FuelLogCreateDto
import com.taxicompany.app.data.remote.dto.FuelLogDto
import com.taxicompany.app.data.remote.dto.FuelSummaryDto

sealed class OdometerInput {
    data class AbsoluteReading(val odometerKm: Int) : OdometerInput()
    data class DistanceSinceLastFillup(val km: Int) : OdometerInput()
}

class FuelRepository(private val fuelApi: FuelApi) {

    suspend fun getFuelLogs(vehicleId: String? = null): ApiResult<List<FuelLogDto>> {
        return safeApiCall { fuelApi.getFuelLogs(vehicleId).results }
    }

    suspend fun getFuelLogById(fuelLogId: String): ApiResult<FuelLogDto> {
        return safeApiCall { fuelApi.getFuelLogById(fuelLogId) }
    }

    suspend fun createFuelLog(
        vehicleId: String,
        fuelType: String,
        liters: String,
        pricePerLiter: String,
        odometerInput: OdometerInput,
        fueledAt: String,
        shiftId: String? = null,
        stationName: String = "",
        invoiceNumber: String = "",
        driverId: String? = null,
    ): ApiResult<FuelLogDto> {
        return safeApiCall {
            fuelApi.createFuelLog(
                FuelLogCreateDto(
                    vehicle = vehicleId,
                    shift = shiftId,
                    fuelType = fuelType,
                    liters = liters,
                    pricePerLiter = pricePerLiter,
                    odometerKm = (odometerInput as? OdometerInput.AbsoluteReading)?.odometerKm,
                    kmDrivenSinceLastFillup = (odometerInput as? OdometerInput.DistanceSinceLastFillup)?.km,
                    stationName = stationName,
                    invoiceNumber = invoiceNumber,
                    fueledAt = fueledAt,
                    driver = driverId,
                )
            )
        }
    }

    suspend fun getMySummary(dateFrom: String, dateTo: String): ApiResult<FuelSummaryDto> {
        return safeApiCall { fuelApi.getMySummary(dateFrom, dateTo) }
    }
}
