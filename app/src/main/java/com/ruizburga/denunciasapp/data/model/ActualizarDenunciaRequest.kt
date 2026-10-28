package com.ruizburga.denunciasapp.data.model

data class ActualizarDenunciaRequest(
    val dni: String,
    val fecha: String,
    val tipo: String,
    val direccion: String,
    val descripcion: String,
    val estado: Int
)
