package com.taxicompany.app.ui.schedule

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.taxicompany.app.core.ApiResult
import com.taxicompany.app.data.repository.AuthRepository
import com.taxicompany.app.data.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * State holder dla ekranu grafiku
 */
class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val authRepository: AuthRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    var uiState by mutableStateOf(ScheduleUiState(weekStart = currentWeekStart()))
        private set

    init {
        loadShifts()
    }

    fun goToPreviousWeek() {
        uiState = uiState.copy(weekStart = uiState.weekStart.minus(7, DateTimeUnit.DAY))
        loadShifts()
    }

    fun goToNextWeek() {
        uiState = uiState.copy(weekStart = uiState.weekStart.plus(7, DateTimeUnit.DAY))
        loadShifts()
    }

    fun retry() = loadShifts()

    private fun loadShifts() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        val weekEnd = uiState.weekStart.plus(7, DateTimeUnit.DAY)
        // UWAGA UWAGA UWAGA
        // Backend filtruje po DateTimeField (start_at__gte/lte), wiec
        // potrzebujemy pelnego ISO datetime z czasem, nie tylko daty -
        // patrz ScheduleApi.getShifts i komentarz tam o formacie.
        // Uzywamy poczatku dnia w UTC jako granic - to jest uproszczenie:
        // nie uwzgledniamy strefy czasowej uzytkownika, co moze pokazac
        // zmiane z 23:00-07:00 podzielona na dwa tygodnie w rzadkich
        // przypadkach.
        val dateFrom = "${uiState.weekStart}T00:00:00Z"
        val dateTo = "${weekEnd}T00:00:00Z"

        scope.launch {
            when (val result = scheduleRepository.getShifts(dateFrom, dateTo)) {
                is ApiResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        shifts = result.data.sortedBy { it.startAt },
                    )
                }
                is ApiResult.Error -> {
                    if (result.isAuthError) {
                        authRepository.notifyAuthError()
                    }
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun currentWeekStart(): LocalDate {
        val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
        return today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
    }
}
