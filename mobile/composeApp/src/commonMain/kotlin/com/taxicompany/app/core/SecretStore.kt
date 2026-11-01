package com.taxicompany.app.core

// TODO: Add an iOS SecretStore implementation backed by Keychain
//  Only needed if an iOS target is added to the project.
//  labels: enhancement

interface SecretStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
    fun remove(key: String)
}