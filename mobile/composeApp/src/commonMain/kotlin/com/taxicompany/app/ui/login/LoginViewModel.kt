package com.taxicompany.app.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val onLoginSuccess: () -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onUsernameChange(value: String) {
        uiState = uiState.copy(username = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun onLoginClick() {
        if (uiState.username.isBlank() || uiState.password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Wpisz nazwę użytkownika i hasło.")
            return
        }

        uiState = uiState.copy(isLoading = true, errorMessage = null)

        scope.launch {
            when (val result = authRepository.login(uiState.username, uiState.password)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(isLoading = false, isLoggedIn = true)
                    onLoginSuccess()
                }
                is ApiResult.Error -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
