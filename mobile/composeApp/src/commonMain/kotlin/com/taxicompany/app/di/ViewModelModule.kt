package com.taxicompany.app.di

import com.taxicompany.app.ui.fuel.FuelViewModel
import com.taxicompany.app.ui.profile.ProfileViewModel
import com.taxicompany.app.ui.schedule.ScheduleViewModel
import org.koin.dsl.module

/**
 * Koin dla state holderów ekranów (ProfileViewModel itd) -
 * trzymany osobno od networkModule (warstwa sieciowa/repozytoria),
 * zeby rozdzielic logike.
 */
val viewModelModule = module {
    factory { ProfileViewModel(get(), get()) }
    factory { ScheduleViewModel(get(), get()) }
    factory { FuelViewModel(get(), get(), get()) }
}
