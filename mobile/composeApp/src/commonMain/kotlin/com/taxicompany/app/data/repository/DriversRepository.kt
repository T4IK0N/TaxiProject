package com.taxicompany.app.data.repository

import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.core.safeApiCall
import com.taxicompany.app.data.remote.api.DriversApi
import com.taxicompany.app.data.remote.dto.DriverDetailDto
import com.taxicompany.app.data.remote.dto.DriverDocumentDto
import com.taxicompany.app.data.remote.dto.DriverListItemDto

class DriversRepository(private val driversApi: DriversApi) {

    suspend fun listDrivers(search: String? = null): ApiResult<List<DriverListItemDto>> =
        safeApiCall { driversApi.listDrivers(search).results }

    suspend fun getMyProfile(): ApiResult<DriverDetailDto> =
        safeApiCall { driversApi.getMyProfile() }

    suspend fun getDriver(driverId: String): ApiResult<DriverDetailDto> =
        safeApiCall { driversApi.getDriver(driverId) }

    suspend fun getDriverDocuments(driverId: String): ApiResult<List<DriverDocumentDto>> =
        safeApiCall { driversApi.getDriverDocuments(driverId).results }
}
