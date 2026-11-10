
package com.fabiancg.ejemplomvvm.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.fabiancg.ejemplomvvm.model.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Clase encargada de realizar las operaciones CRUD en SQLite.
class ProductoRepository(context: Context) {

    // Crear el objeto que permite acceder a la base de datos.
    private val helper = ProductoDatabaseHelper(context)

    // CONSULTAR: Obtener todos los productos registrados.
    suspend fun listar(): List<Producto> = withContext(Dispatchers.IO) {

        // Lista donde almacenaremos los productos encontrados.
        val productos = mutableListOf<Producto>()

        // Consultar todos los registros de la tabla productos.
        // Consulta es aproximadamente equivalente a:
        /*
            SELECT *
            FROM productos
            ORDER BY nombre COLLATE NOCASE ASC, codigo ASC;

            Obtiene todos los productos, ordenados por nombre sin distinguir mayúsculas y minúsculas ASCII,
            y luego por código

        * */
        helper.readableDatabase.query(
            ProductoDatabaseHelper.TABLA_PRODUCTOS,
            null, null, null, null, null,
            "nombre COLLATE NOCASE ASC, codigo ASC"
        ).use { cursor ->

            // Obtener la posición de cada columna.
            val colCodigo = cursor.getColumnIndexOrThrow("codigo")
            val colNombre = cursor.getColumnIndexOrThrow("nombre")
            val colPrecio = cursor.getColumnIndexOrThrow("precio")
            val colLargo = cursor.getColumnIndexOrThrow("largo")//F00
            val colFecha = cursor.getColumnIndexOrThrow("fecha_registro")
            val colDisponible = cursor.getColumnIndexOrThrow("disponible")

            // Recorrer cada registro encontrado.
            while (cursor.moveToNext()) {

                // Convertir el registro SQLite en un objeto Producto.
                productos.add(
                    Producto(
                        codigo = cursor.getString(colCodigo),
                        nombre = cursor.getString(colNombre),
                        precio = cursor.getInt(colPrecio),
                        fechaRegistro = cursor.getString(colFecha),
                        disponible = cursor.getInt(colDisponible) == 1
                    )
                )
            }
        }

        // Devolver la lista de productos.
        productos
    }

    // REGISTRAR: Guardar un nuevo producto en SQLite.
    suspend fun registrar(producto: Producto): Boolean =
        withContext(Dispatchers.IO) {

            // Preparar los valores que se guardarán.
            val valores = ContentValues().apply {
                put("codigo", producto.codigo)
                put("nombre", producto.nombre)
                put("precio", producto.precio)
                put("largo", producto.largo)
                put("fecha_registro", producto.fechaRegistro)
                put("disponible", if (producto.disponible) 1 else 0)
            }

            // Insertar el producto. Ignorar conflictos de código.
            helper.writableDatabase.insertWithOnConflict(
                ProductoDatabaseHelper.TABLA_PRODUCTOS,
                null,
                valores,
                SQLiteDatabase.CONFLICT_IGNORE
            ) != -1L
        }

    // ACTUALIZAR: Modificar un producto existente.
    suspend fun actualizar(
        codigo: String,
        nombre: String,
        precio: Int,
        largo: Double,
        disponible: Boolean
    ): Boolean = withContext(Dispatchers.IO) {

        // Preparar los nuevos valores.
        val valores = ContentValues().apply {
            put("nombre", nombre)
            put("precio", precio)
            put("disponible", if (disponible) 1 else 0)
        }

        // Modificar el producto identificado por su código.
        helper.writableDatabase.update(
            ProductoDatabaseHelper.TABLA_PRODUCTOS,
            valores,
            "codigo = ?",
            arrayOf(codigo)
        ) > 0
    }

    // ELIMINAR: Borrar un producto según su código.
    suspend fun eliminar(codigo: String): Boolean =
        withContext(Dispatchers.IO) {

            helper.writableDatabase.delete(
                ProductoDatabaseHelper.TABLA_PRODUCTOS,
                "codigo = ?",
                arrayOf(codigo)
            ) > 0
        }
}
