package com.taxicompany.app.di

import com.russhwolf.settings.Settings
import com.taxicompany.app.core.HttpClientFactory
import com.taxicompany.app.core.SettingsTokenStorage
import com.taxicompany.app.core.TokenStorage
import com.taxicompany.app.data.remote.api.AuthApi
import com.taxicompany.app.data.remote.api.DriversApi
import com.taxicompany.app.data.remote.api.FuelApi
import com.taxicompany.app.data.remote.api.ScheduleApi
import com.taxicompany.app.data.remote.api.VehiclesApi
import com.taxicompany.app.data.repository.AuthRepository
import com.taxicompany.app.data.repository.DriversRepository
import com.taxicompany.app.data.repository.FuelRepository
import com.taxicompany.app.data.repository.ScheduleRepository
import com.taxicompany.app.data.repository.VehiclesRepository
import org.koin.dsl.module

val networkModule = module {
    single<Settings> { Settings() }
    single<TokenStorage> { SettingsTokenStorage(get()) }
    single { HttpClientFactory.create(get()) }

    single { AuthApi(get()) }
    single { AuthRepository(get(), get(), get()) }

    single { DriversApi(get()) }
    single { DriversRepository(get()) }

    single { VehiclesApi(get()) }
    single { VehiclesRepository(get()) }

    single { ScheduleApi(get()) }
    single { ScheduleRepository(get()) }

    single { FuelApi(get()) }
    single { FuelRepository(get()) }
}
