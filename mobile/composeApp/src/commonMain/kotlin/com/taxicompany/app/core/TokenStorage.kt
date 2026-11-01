package com.taxicompany.app.core

interface TokenStorage {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(access: String, refresh: String)
    fun saveAccessToken(access: String)
    fun clear()
}