package com.fabiancg.ejemplomvvm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.fabiancg.ejemplomvvm.data.ProductoRepository
import com.fabiancg.ejemplomvvm.ui.ProductosScreen
import com.fabiancg.ejemplomvvm.ui.theme.EjemploMVVMTheme
import com.fabiancg.ejemplomvvm.viewmodel.ProductoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Crear el repositorio que administra el acceso a SQLite.
        val repository = ProductoRepository(applicationContext)

        // Crear u obtener el ViewModel y entregarle el repositorio.
        val viewModel = ViewModelProvider(
            this,                                  // Activity propietaria del ViewModel.
            ProductoViewModel.Factory(repository)  // Factory para crear el ViewModel.
        )[ProductoViewModel::class.java]           // Clase del ViewModel que necesitamos.

        setContent {
            EjemploMVVMTheme {

                // Observar y actualizar la intefaz automáticamente cuando cambien los datos.
                val estado by viewModel.uiState.collectAsStateWithLifecycle()

                // Mostrar la pantalla del mantenedor de productos.
                ProductosScreen(
                    estado = estado, // Datos que mostrará la interfaz.

                    // Funciones que se ejecutan cuando cambia un campo.
                    onCodigoChange = { nuevoCodigo -> viewModel.cambiarCodigo(nuevoCodigo) },
                    onNombreChange = { nuevoNombre -> viewModel.cambiarNombre(nuevoNombre) },
                    onPrecioChange = { nuevoPrecio -> viewModel.cambiarPrecio(nuevoPrecio) },
                    onDisponibleChange = { disponible -> viewModel.cambiarDisponibilidad(disponible) },

                    // Funciones para realizar las operaciones del mantenedor.

                    // Guardar un producto nuevo o modificado.
                    onGuardar = { viewModel.guardar() },
                    // Seleccionar el producto que se desea modificar.
                    onEditar = { producto -> viewModel.seleccionarParaEditar(producto) },
                    // Cancelar la edición actual.
                    onCancelar = { viewModel.cancelarEdicion() },
                    // Eliminar un producto según su código.
                    onEliminar = { codigo -> viewModel.eliminar(codigo) }
                )

            }
        }
    }
}
