package com.ruizburga.denunciasapp.data.model

/** Modelo de la guía. Las fechas y el identificador quedan como texto hasta conocer el contrato REST. */
data class Denuncia(
    val id: String,
    val dni: String,
    val fecha: String,
    val tipo: String,
    val direccion: String,
    val descripcion: String,
    val estado: String
)
