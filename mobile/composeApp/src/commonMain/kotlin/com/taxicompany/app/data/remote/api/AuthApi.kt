package com.taxicompany.app.data.remote.api

import com.taxicompany.app.core.PlatformConfig
import com.taxicompany.app.data.remote.dto.LoginRequestDto
import com.taxicompany.app.data.remote.dto.TokenPairDto
import com.taxicompany.app.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AuthApi(private val httpClient: HttpClient) {

    private val baseUrl = PlatformConfig.apiBaseUrl

    /**
     * Logowanie NIE przechodzi przez plugin Auth.bearer (logiczne -
     * nie mamy jeszcze tokenu do wyslania), wiec to jest "goly" POST.
     */
    suspend fun login(username: String, password: String): TokenPairDto {
        return httpClient.post("$baseUrl/api/v1/auth/login/") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username = username, password = password))
        }.body()
    }

    /** Ten request automatycznie dostanie token przez plugin Auth.bearer */
    suspend fun getCurrentUser(): UserDto {
        return httpClient.get("$baseUrl/api/v1/auth/me/").body()
    }
}
