package com.taxicompany.app.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Login : Route

    @Serializable
    data object Schedule : Route

    @Serializable
    data object Fuel : Route

    @Serializable
    data object Profile : Route

    @Serializable
    data class ShiftDetail(val shiftId: String) : Route
}

sealed interface Graph {
    @Serializable
    data object Auth : Graph

    @Serializable
    data object Main : Graph
}
