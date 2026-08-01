package com.taxicompany.app.core

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun createHttpClientEngine(): HttpClientEngine = Android.create()
