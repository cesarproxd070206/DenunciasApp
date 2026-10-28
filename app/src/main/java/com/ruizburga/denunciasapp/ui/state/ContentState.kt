package com.ruizburga.denunciasapp.ui.state

/** Estados listos para conectar a la carga del repositorio REST en la siguiente etapa. */
sealed interface ContentState<out T> {
    data object Loading : ContentState<Nothing>
    data object Empty : ContentState<Nothing>
    data class Error(val message: String) : ContentState<Nothing>
    data class Data<T>(val value: T) : ContentState<T>
}
