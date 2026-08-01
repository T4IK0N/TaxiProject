package com.taxicompany.app.data.remote.dto

import kotlinx.serialization.Serializable

/** Body POST /api/v1/auth/login/ */
@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

/** Response /api/v1/auth/login/ - dwa tokeny JWT */
@Serializable
data class TokenPairDto(
    val access: String,
    val refresh: String,
)

/** Body POST /api/v1/auth/refresh/ */
@Serializable
data class TokenRefreshRequestDto(
    val refresh: String,
)

/** Response /api/v1/auth/refresh/ - tylko nowy access token */
@Serializable
data class TokenRefreshResponseDto(
    val access: String,
)

/** GET/PATCH /api/v1/auth/me/ */
@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val role: String,
    val phoneNumber: String? = null,
)
