package com.taxicompany.app.ui.schedule

import com.taxicompany.app.data.remote.dto.ShiftDto
import kotlinx.datetime.LocalDate

/**
 * Stan ekranu grafiku. weekStart wyznacza, ktory tydzien jest aktualnie
 * wyswietlany (poniedzialek danego tygodnia) - uzytkownik moze przejsc
 * do poprzedniego/nastepnego tygodnia, co przeladowuje shifts z nowym
 * zakresem dat.
 */
data class ScheduleUiState(
    val weekStart: LocalDate,
    val isLoading: Boolean = true,
    val shifts: List<ShiftDto> = emptyList(),
    val errorMessage: String? = null,
)
