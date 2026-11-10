package com.fabiancg.ejemplomvvm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabiancg.ejemplomvvm.model.Producto
import com.fabiancg.ejemplomvvm.viewmodel.ProductoUiState
import java.text.NumberFormat
import java.util.Locale

/** Vista: únicamente dibuja el estado y comunica eventos al ViewModel. */
@Composable
fun ProductosScreen(
    // Contiene los datos que se mostrarán en pantalla.
    estado: ProductoUiState,

    // Funciones que reciben los valores ingresados por el usuario.
    onCodigoChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onLargoChange: (String) -> Unit,
    onDisponibleChange: (Boolean) -> Unit,

    // Función para registrar o actualizar un producto.
    onGuardar: () -> Unit,

    // Función que recibe el producto que se desea modificar.
    onEditar: (Producto) -> Unit,

    // Función para cancelar la edición.
    onCancelar: () -> Unit,

    // Función que recibe el código del producto que se eliminará.
    onEliminar: (String) -> Unit

    // Unit: La función no devuelve ningún valor. Similar a void en Java
) {
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Mantenedor de productos", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Ejemplo MVVM con base de datos SQLite local",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            item {
                FormularioProducto(
                    estado = estado,
                    onCodigoChange = onCodigoChange,
                    onNombreChange = onNombreChange,
                    onPrecioChange = onPrecioChange,
                    onLargoChange = onLargoChange,
                    onDisponibleChange = onDisponibleChange,
                    onGuardar = onGuardar,
                    onCancelar = onCancelar
                )
            }

            item {
                Text(
                    "Productos registrados (${estado.productos.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (estado.cargando) {
                item {
                    CircularProgressIndicator()
                }
            } else if (estado.productos.isEmpty()) {
                item {
                    Text("No hay productos registrados.")
                }
            } else {
                items(items = estado.productos, key = { it.codigo }) { producto ->
                    TarjetaProducto(
                        producto = producto,
                        habilitado = !estado.procesando,
                        onEditar = { onEditar(producto) },
                        onEliminar = { productoAEliminar = producto }
                    )
                }
            }
        }
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Eliminar el producto ${producto.codigo} - ${producto.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar(producto.codigo)
                    productoAEliminar = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FormularioProducto(
    estado: ProductoUiState,
    onCodigoChange: (String) -> Unit,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onLargoChange: (String) -> Unit,
    onDisponibleChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    val editando = estado.editandoCodigo != null
    val habilitado = !estado.procesando && !estado.cargando

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (editando) "Editar producto" else "Registrar producto",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = estado.codigo,
                onValueChange = onCodigoChange,
                label = { Text("Código") },
                enabled = habilitado && !editando,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre") },
                enabled = habilitado,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.precio,
                onValueChange = onPrecioChange,
                label = { Text("Precio (CLP)") },
                enabled = habilitado,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = estado.largo,
                onValueChange = onLargoChange,
                label = { Text("Largo (cm)") },
                enabled = habilitado,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Disponible")
                Switch(
                    checked = estado.disponible,
                    onCheckedChange = onDisponibleChange,
                    enabled = habilitado
                )
            }

            Text(
                if (editando) "Fecha de registro: ${estado.fechaOriginal} (no se modifica)"
                else "La fecha de registro se asigna automáticamente al guardar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            estado.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            estado.mensaje?.let { mensaje ->
                Text(mensaje, color = MaterialTheme.colorScheme.primary)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onGuardar, enabled = habilitado) {
                    Text(if (editando) "Guardar cambios" else "Registrar")
                }
                if (editando) {
                    OutlinedButton(onClick = onCancelar, enabled = habilitado) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaProducto(
    producto: Producto,
    habilitado: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val formato = remember {
        NumberFormat.getNumberInstance(
            Locale.forLanguageTag("es-CL")
        ).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
    }

    // Formato del largo: hasta 2 decimales, con coma decimal.
    val formatoLargo = remember {
        NumberFormat.getNumberInstance(
            Locale.forLanguageTag("es-CL")
        ).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(producto.nombre, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Código: ${producto.codigo}")
            Text("Precio: $${formato.format(producto.precio)}")
            Text("Largo: ${formatoLargo.format(producto.largo)} cm")
            Text("Fecha de registro: ${producto.fechaRegistro}")
            Text(
                if (producto.disponible) "Estado: Disponible" else "Estado: No disponible",
                color = if (producto.disponible) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEditar, enabled = habilitado) {
                    Text("Editar")
                }
                TextButton(onClick = onEliminar, enabled = habilitado) {
                    Text("Eliminar")
                }
            }
        }
    }
}
