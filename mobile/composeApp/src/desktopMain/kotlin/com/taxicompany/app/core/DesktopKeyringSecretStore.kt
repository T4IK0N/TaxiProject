package com.taxicompany.app.core

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException

class DesktopKeyringSecretStore : SecretStore {

    private val keyring = Keyring.create()
    private val service = "com.taxicompany.app"

    override fun get(key: String): String? = try {
        keyring.getPassword(service, key)
    } catch (_: PasswordAccessException) {
        null // brak wpisu
    }

    override fun put(key: String, value: String) {
        keyring.setPassword(service, key, value)
    }

    override fun remove(key: String) {
        try { keyring.deletePassword(service, key) } catch (_: PasswordAccessException) {}
    }
}