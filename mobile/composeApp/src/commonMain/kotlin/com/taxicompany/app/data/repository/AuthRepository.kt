package com.taxicompany.app.data.repository

import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.core.TokenStorage
import com.taxicompany.app.core.safeApiCall
import com.taxicompany.app.data.remote.api.AuthApi
import com.taxicompany.app.data.remote.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
) {
    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired

    suspend fun login(username: String, password: String): ApiResult<Unit> {
        // requiresAuth=false: /auth/login/ nie wymaga wczesniejszej sesji,
        // wiec 401 tutaj znaczy "podales zle dane logowania", NIE "twoja
        // sesja wygasla" - patrz szczegolowy komentarz w core/ApiResult.kt
        // przy parametrze requiresAuth. Bez tego flaga isAuthError=true
        // wycieklaby z bledu logowania i (jesli ktos w przyszlosci dodalby
        // globalna reakcje na isAuthError poza ta funkcja) mogloby to
        // bledzie wywolac notifyAuthError() w trakcie samego logowania.
        val result = safeApiCall(requiresAuth = false) {
            val tokens = authApi.login(username, password)
            tokenStorage.saveTokens(tokens.access, tokens.refresh)
        }
        if (result is ApiResult.Success) {
            _sessionExpired.value = false
        }
        return result
    }

    suspend fun getCurrentUser(): ApiResult<UserDto> {
        return safeApiCall { authApi.getCurrentUser() }
    }

    /**
     * Wywolywane przez kazdy ViewModel, gdy dostanie ApiResult.Error
     * z isAuthError=true. Czysci token i ustawia sygnal, na ktory
     * reaguje AppNavHost.
     */
    fun notifyAuthError() {
        tokenStorage.clear()
        _sessionExpired.value = true
    }

    fun consumeSessionExpiredSignal() {
        _sessionExpired.value = false
    }

    fun isLoggedIn(): Boolean = tokenStorage.getAccessToken() != null

    fun logout() {
        tokenStorage.clear()
    }
}
