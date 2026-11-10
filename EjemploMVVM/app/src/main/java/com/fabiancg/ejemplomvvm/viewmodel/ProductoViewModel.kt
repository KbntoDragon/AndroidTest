package com.fabiancg.ejemplomvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fabiancg.ejemplomvvm.data.ProductoRepository
import com.fabiancg.ejemplomvvm.model.Producto
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Recibe acciones de la Vista, valida los datos y solicita operaciones al Repository. */
class ProductoViewModel(private val repository: ProductoRepository) : ViewModel() {

    // Estado interno que puede modificarse.
    private val _uiState = MutableStateFlow(ProductoUiState())

    // Estado público que la interfaz puede observar.
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    // Se ejecuta cuando se crea el ViewModel.
    init {
        cargarProductos()
    }

    // Consulta los productos almacenados en SQLite.
    private fun cargarProductos() {

        // Ejecutar una corrutina asociada al ViewModel.
        viewModelScope.launch {
            try {
                // Solicitar al repositorio la lista de productos.
                val productos = repository.listar()

                // Actualizar el estado con los productos obtenidos.
                _uiState.update { it.copy(productos = productos, cargando = false) }
            } catch (e: Exception) {

                // Informar si ocurrió un error al consultar.
                _uiState.update {
                    it.copy(cargando = false, error = "No se pudo leer la base de datos: ${e.message}")
                }
            }
        }
    }

    fun cambiarCodigo(valor: String) {
        if (_uiState.value.editandoCodigo != null || _uiState.value.procesando) return
        _uiState.update { it.copy(codigo = valor, error = null, mensaje = null) }
    }

    fun cambiarNombre(valor: String) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(nombre = valor, error = null, mensaje = null) }
    }


    fun cambiarPrecio(valor: String) {
        if (_uiState.value.procesando) return
        // Solo aceptar dígitos del 0 al 9.
        if (!valor.all { it in '0'..'9' }) return
        // Actualizar el precio del formulario.
        _uiState.update {
            it.copy(precio = valor, error = null, mensaje = null)
        }
    }


    fun cambiarDisponibilidad(valor: Boolean) {
        if (_uiState.value.procesando) return
        _uiState.update { it.copy(disponible = valor, error = null, mensaje = null) }
    }

    fun seleccionarParaEditar(producto: Producto) {
        if (_uiState.value.procesando) return

        // Colocar los datos del producto en el formulario.
        _uiState.update {
            it.copy(
                codigo = producto.codigo,
                nombre = producto.nombre,
                precio = producto.precio.toString(),
                disponible = producto.disponible,
                editandoCodigo = producto.codigo,
                fechaOriginal = producto.fechaRegistro,
                error = null,
                mensaje = null
            )
        }
    }

    fun cancelarEdicion() {
        if (_uiState.value.procesando) return
        _uiState.update { limpiarFormulario(it) }
    }

    fun guardar() {
        // Obtener el estado actual del formulario.
        val estado = _uiState.value

        // No guardar mientras se carga o procesa.
        if (estado.procesando || estado.cargando) return

        // Preparar los datos ingresados.
        val codigo = estado.codigo.trim().uppercase(Locale.ROOT)
        val nombre = estado.nombre.trim()
        val precio = estado.precio.trim().toIntOrNull()

        if (codigo.isEmpty()) {
            mostrarError("Debe ingresar el código del producto.")
            return
        }
        if (nombre.isEmpty()) {
            mostrarError("Debe ingresar el nombre del producto.")
            return
        }
        if (precio == null || precio <= 0) {
            mostrarError("El precio debe ser un número entero mayor que cero.")
            return
        }

        _uiState.update { it.copy(procesando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            try {
                // Comprobar si estamos editando un producto.
                val editando = estado.editandoCodigo != null

                val guardado = if (editando) {
                    // Modificar el producto existente.
                    repository.actualizar(
                        codigo = estado.editandoCodigo!!,
                        nombre = nombre,
                        precio = precio,
                        disponible = estado.disponible
                    )
                } else {
                    // Registrar un producto nuevo.
                    repository.registrar(
                        Producto(
                            codigo = codigo,
                            nombre = nombre,
                            precio = precio,
                            fechaRegistro = LocalDate.now().toString(),
                            disponible = estado.disponible
                        )
                    )
                }

                if (!guardado) {
                    _uiState.update {
                        it.copy(
                            procesando = false,
                            error = if (editando) "El producto ya no existe."
                                    else "Ya existe un producto con ese código."
                        )
                    }
                    return@launch
                }

                // Consultar nuevamente todos los productos desde SQLite.
                val listaActualizada = repository.listar()

                // Actualizar el estado de la interfaz.
                _uiState.update {
                    limpiarFormulario(it).copy(
                        productos = listaActualizada,
                        procesando = false,
                        mensaje = if (editando) "Producto actualizado correctamente."
                                  else "Producto registrado correctamente."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesando = false, error = "No se pudo guardar el producto: ${e.message}")
                }
            }
        }
    }

    fun eliminar(codigo: String) {

        // Evitar operaciones simultáneas.
        if (_uiState.value.procesando || _uiState.value.cargando)
            return

        // Informar que comienza el procesamiento.
        _uiState.update { it.copy(procesando = true, error = null, mensaje = null) }

        viewModelScope.launch {
            try {
                // Eliminar el producto desde SQLite.
                val eliminado = repository.eliminar(codigo)

                // Obtener la lista actualizada.
                val listaActualizada = repository.listar()

                // Actualizar el estado de la pantalla.
                _uiState.update { estado ->
                    // Limpiar el formulario si estamos editando el producto eliminado.
                    val nuevoEstado = if (estado.editandoCodigo == codigo) {
                        limpiarFormulario(estado)
                    } else {
                        estado
                    }
                    nuevoEstado.copy(
                        productos = listaActualizada,
                        procesando = false,
                        mensaje = if (eliminado) "Producto eliminado correctamente." else null,
                        error = if (eliminado) null else "El producto ya no existe."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(procesando = false, error = "No se pudo eliminar el producto: ${e.message}")
                }
            }
        }
    }

    private fun mostrarError(texto: String) {
        _uiState.update { it.copy(error = texto, mensaje = null) }
    }

    private fun limpiarFormulario(estado: ProductoUiState): ProductoUiState = estado.copy(
        codigo = "",
        nombre = "",
        precio = "",
        disponible = true,
        editandoCodigo = null,
        fechaOriginal = null,
        error = null,
        mensaje = null
    )

    // Permite crear el ViewModel entregándole el Repository.
    class Factory(private val repository: ProductoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Comprobar que se solicita ProductoViewModel.
            if (modelClass.isAssignableFrom(ProductoViewModel::class.java)) {
                // Crear el ViewModel con su repositorio.
                return ProductoViewModel(repository) as T
            }
            // Informar si se solicita otro ViewModel.
            throw IllegalArgumentException("ViewModel no reconocido: ${modelClass.name}")
        }
    }
}
