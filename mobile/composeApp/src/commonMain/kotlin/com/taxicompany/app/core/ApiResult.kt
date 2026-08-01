package com.taxicompany.app.core

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val isAuthError: Boolean = false) : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(requiresAuth: Boolean = true, block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: ClientRequestException) {
        when (val status = e.response.status) {
            HttpStatusCode.Unauthorized if !requiresAuth -> {
                ApiResult.Error("Błędne dane logowania.", isAuthError = false)
            }
            HttpStatusCode.Unauthorized -> {
                ApiResult.Error("Sesja wygasła. Zaloguj się ponownie.", isAuthError = true)
            }
            HttpStatusCode.Forbidden -> {
                ApiResult.Error("Nie masz uprawnień do wykonania tej operacji.")
            }
            HttpStatusCode.NotFound -> {
                ApiResult.Error("Nie znaleziono żądanych danych.")
            }
            HttpStatusCode.BadRequest -> {
                ApiResult.Error(extractValidationMessage(e))
            }
            else -> {
                ApiResult.Error("Błąd żądania (${status.value}). Spróbuj ponownie.")
            }
        }
    } catch (_: ServerResponseException) {
        ApiResult.Error("Błąd serwera. Spróbuj ponownie za chwilę.")
    } catch (_: kotlinx.io.IOException) {
        ApiResult.Error("Brak połączenia. Sprawdź internet i spróbuj ponownie.")
    } catch (_: Exception) {
        ApiResult.Error("Wystąpił nieoczekiwany błąd. Spróbuj ponownie.")
    }
}

private suspend fun extractValidationMessage(e: ClientRequestException): String {
    return try {
        val rawBody: String = e.response.bodyAsText()
        val messages: List<String> = when (val json = Json.parseToJsonElement(rawBody)) {
            is JsonArray -> json.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }

            is JsonObject -> json.entries.flatMap { (_, value) ->
                when (value) {
                    is JsonArray -> value.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
                    is JsonPrimitive -> listOfNotNull(value.contentOrNull)
                    else -> emptyList()
                }
            }

            else -> emptyList()
        }

        if (messages.isNotEmpty()) messages.joinToString(" ") else "Sprawdź wprowadzone dane i spróbuj ponownie."
    } catch (_: Exception) {
        "Sprawdź wprowadzone dane i spróbuj ponownie."
    }
}