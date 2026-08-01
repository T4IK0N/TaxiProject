package com.taxicompany.app.core

import com.russhwolf.settings.Settings

interface TokenStorage {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(access: String, refresh: String)
    fun saveAccessToken(access: String)
    fun clear()
}

class SettingsTokenStorage(private val settings: Settings) : TokenStorage {

    private companion object {
        const val KEY_ACCESS = "jwt_access_token"
        const val KEY_REFRESH = "jwt_refresh_token"
    }

    override fun getAccessToken(): String? = settings.getStringOrNull(KEY_ACCESS)

    override fun getRefreshToken(): String? = settings.getStringOrNull(KEY_REFRESH)

    override fun saveTokens(access: String, refresh: String) {
        settings.putString(KEY_ACCESS, access)
        settings.putString(KEY_REFRESH, refresh)
    }

    override fun saveAccessToken(access: String) {
        settings.putString(KEY_ACCESS, access)
    }

    override fun clear() {
        settings.remove(KEY_ACCESS)
        settings.remove(KEY_REFRESH)
    }
}
