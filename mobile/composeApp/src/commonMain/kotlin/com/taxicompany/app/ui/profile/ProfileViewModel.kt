package com.taxicompany.app.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.data.repository.AuthRepository
import com.taxicompany.app.data.repository.DriversRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class ProfileViewModel(
    private val driversRepository: DriversRepository,
    private val authRepository: AuthRepository,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)
        scope.launch {
            when (val result = driversRepository.getMyProfile()) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false, profile = result.data)
                }
                is ApiResult.Error -> {
                    // isAuthError=true znaczy, ze token jest niewazny (np.
                    // sesja wygasla, albo token z poprzedniej bazy danych
                    // po jej wyczyszczeniu) i auto-refresh tez sie nie udal.
                    // notifyAuthError() ustawia globalny sygnal, na ktory
                    // reaguje AppNavHost (automatyczne przekierowanie na login) -
                    // uzytkownik NIE zostaje uwieziony na ekranie z bledem.
                    if (result.isAuthError) {
                        authRepository.notifyAuthError()
                    }
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
