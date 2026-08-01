package com.taxicompany.app.data.repository

import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.core.safeApiCall
import com.taxicompany.app.data.remote.api.VehiclesApi
import com.taxicompany.app.data.remote.dto.MaintenanceLogDto
import com.taxicompany.app.data.remote.dto.VehicleDetailDto
import com.taxicompany.app.data.remote.dto.VehicleDocumentDto
import com.taxicompany.app.data.remote.dto.VehicleListItemDto

class VehiclesRepository(private val vehiclesApi: VehiclesApi) {

    suspend fun listVehicles(
        status: String? = null,
        ownershipType: String? = null,
    ): ApiResult<List<VehicleListItemDto>> =
        safeApiCall { vehiclesApi.listVehicles(status, ownershipType).results }

    suspend fun getVehicle(vehicleId: String): ApiResult<VehicleDetailDto> =
        safeApiCall { vehiclesApi.getVehicle(vehicleId) }

    suspend fun getVehicleDocuments(vehicleId: String): ApiResult<List<VehicleDocumentDto>> =
        safeApiCall { vehiclesApi.getVehicleDocuments(vehicleId).results }

    suspend fun getMaintenanceLogs(vehicleId: String): ApiResult<List<MaintenanceLogDto>> =
        safeApiCall { vehiclesApi.getMaintenanceLogs(vehicleId).results }
}
