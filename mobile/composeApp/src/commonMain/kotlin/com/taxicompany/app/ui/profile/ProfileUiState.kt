package com.taxicompany.app.ui.profile

import com.taxicompany.app.data.remote.dto.DriverDetailDto

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: DriverDetailDto? = null,
    val errorMessage: String? = null,
)
