package com.taxicompany.app.core

// TODO: Add unit tests for SecureTokenStorage
//  Use a fake in-memory SecretStore to cover save/get/clear and the
//  saveAccessToken-only path.
//  labels: enhancement

class SecureTokenStorage(private val store: SecretStore) : TokenStorage {

    private companion object {
        const val KEY_ACCESS = "jwt_access_token"
        const val KEY_REFRESH = "jwt_refresh_token"
    }

    override fun getAccessToken() = store.get(KEY_ACCESS)
    override fun getRefreshToken() = store.get(KEY_REFRESH)

    override fun saveTokens(access: String, refresh: String) {
        store.put(KEY_ACCESS, access)
        store.put(KEY_REFRESH, refresh)
    }

    override fun saveAccessToken(access: String) = store.put(KEY_ACCESS, access)

    override fun clear() {
        store.remove(KEY_ACCESS)
        store.remove(KEY_REFRESH)
    }
}