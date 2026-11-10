package com.fabiancg.ejemplomvvm.viewmodel

import com.fabiancg.ejemplomvvm.model.Producto

// Criterios disponibles para ordenar la lista de productos.
enum class OrdenProductos {
    NOMBRE, // De la A a la Z.
    PRECIO  // De menor a mayor precio.
}

// Almacena toda la información que necesita
// la pantalla del mantenedor de productos.
data class ProductoUiState(

    // Lista de productos que se mostrarán en pantalla.
    val productos: List<Producto> = emptyList(),

    // Criterio con el que se ordena la lista de productos.
    val orden: OrdenProductos = OrdenProductos.NOMBRE,

    // Datos ingresados en los campos del formulario.
    val codigo: String = "",
    val nombre: String = "",
    val precio: String = "",
    val largo: String = "", // Largo en centímetros.
    val disponible: Boolean = true,

    // Código del producto que estamos editando.
    // null significa que no estamos editando.
    val editandoCodigo: String? = null,

    // Fecha original del producto en edición.
    val fechaOriginal: String? = null,

    // Indica si se están cargando los productos.
    val cargando: Boolean = true,

    // Indica si se está guardando o eliminando.
    val procesando: Boolean = false,

    // Mensaje de error para mostrar al usuario.
    val error: String? = null,

    // Mensaje informativo o de confirmación.
    val mensaje: String? = null
)
