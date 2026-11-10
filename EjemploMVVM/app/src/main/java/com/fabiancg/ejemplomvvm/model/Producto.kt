package com.fabiancg.ejemplomvvm.model

/** Un producto guardado en la base de datos local SQLite. */
data class Producto(
    val codigo: String,
    val nombre: String,
    val precio: Int,
    val largo: Double,
    val fechaRegistro: String,
    val disponible: Boolean
)
