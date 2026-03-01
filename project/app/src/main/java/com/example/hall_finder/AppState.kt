package com.example.hall_finder

sealed class AppState {
    object WaitingForQR: AppState()
    data class MapLoaded(val startNodeId: String): AppState()
}