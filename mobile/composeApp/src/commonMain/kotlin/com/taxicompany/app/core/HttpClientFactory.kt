package com.taxicompany.app.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

object HttpClientFactory {
    val json = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun create(tokenStorage: TokenStorage): HttpClient {
        return HttpClient(createHttpClientEngine()) {

            expectSuccess = true

            install(ContentNegotiation) {
                json(json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
            }

            install(Logging) {
                level = LogLevel.INFO
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val access = tokenStorage.getAccessToken()
                        val refresh = tokenStorage.getRefreshToken()
                        if (access != null && refresh != null) {
                            BearerTokens(access, refresh)
                        } else {
                            null
                        }
                    }

                    refreshTokens {
                        val refreshToken = tokenStorage.getRefreshToken()
                            ?: return@refreshTokens null

                        try {
                            // Uzywamy "client" bez zadnych pluginow Auth na tym
                            // wywolaniu (inaczej powstalaby petla: 401 -> refresh
                            // -> 401 -> refresh...). Ten "client" w refreshTokens
                            // to specjalna instancja dostarczona przez Ktor wlasnie
                            // do takich operacji bez nakladania sie na siebie auth.
                            val response = client.post("${PlatformConfig.apiBaseUrl}/api/v1/auth/refresh/") {
                                contentType(ContentType.Application.Json)
                                setBody(mapOf("refresh" to refreshToken))
                            }
                            val newAccess = response.body<Map<String, String>>()["access"]
                                ?: return@refreshTokens null

                            tokenStorage.saveAccessToken(newAccess)
                            BearerTokens(newAccess, refreshToken)
                        } catch (e: Exception) {
                            // refresh token tez wygasl/jest niewazny -> czysc
                            // sesje, uzytkownik bedzie musial sie zalogowac ponownie
                            tokenStorage.clear()
                            null
                        }
                    }
                }
            }
        }
    }
}
