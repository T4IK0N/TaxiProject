package com.taxicompany.app.data.repository

import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.core.TokenStorage
import com.taxicompany.app.core.safeApiCall
import com.taxicompany.app.data.remote.api.AuthApi
import com.taxicompany.app.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage,
    private val httpClient: HttpClient,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired

    suspend fun login(username: String, password: String): ApiResult<Unit> {
        val result = safeApiCall(requiresAuth = false) {
            val tokens = authApi.login(username, password)
            tokenStorage.saveTokens(tokens.access, tokens.refresh)
        }
        if (result is ApiResult.Success) {
            _sessionExpired.value = false
            clearBearerTokenCache()
        }
        return result
    }

    suspend fun getCurrentUser(): ApiResult<UserDto> {
        return safeApiCall { authApi.getCurrentUser() }
    }

    fun notifyAuthError() {
        tokenStorage.clear()
        scope.launch { clearBearerTokenCache() }
        _sessionExpired.value = true
    }

    fun consumeSessionExpiredSignal() {
        _sessionExpired.value = false
    }

    fun isLoggedIn(): Boolean = tokenStorage.getAccessToken() != null

    fun logout() {
        tokenStorage.clear()
        scope.launch { clearBearerTokenCache() }
    }

    // BearerAuthProvider cache'uje BearerTokens w pamieci po pierwszym
    // loadTokens() i nie odpytuje TokenStorage ponownie, dopoki cache nie
    // zostanie jawnie wyczyszczony przez clearToken() (suspend fun). Bez
    // tego HttpClient dalej wysylalby stary token po login/logout,
    // niezaleznie od tego co jest w TokenStorage.
    private suspend fun clearBearerTokenCache() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}