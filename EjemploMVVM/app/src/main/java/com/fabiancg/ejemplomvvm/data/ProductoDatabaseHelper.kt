
package com.fabiancg.ejemplomvvm.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Clase encargada de crear y administrar la base de datos SQLite.
class ProductoDatabaseHelper(context: Context) : SQLiteOpenHelper(

    context.applicationContext, // Contexto de la aplicación.
    NOMBRE_BD,                  // Nombre de la base de datos.
    null,                       // CursorFactory (usar la predeterminada).
    VERSION_BD                  // Versión de la base de datos.
) {

    // Se ejecuta cuando la base de datos se crea por primera vez.
    override fun onCreate(db: SQLiteDatabase) {

        // Ejecutar una instrucción SQL para crear la tabla productos.
        db.execSQL(
            """
            CREATE TABLE productos (
                codigo TEXT NOT NULL PRIMARY KEY COLLATE NOCASE,
                nombre TEXT NOT NULL,
                precio INTEGER NOT NULL CHECK (precio > 0),
                largo REAL NOT NULL DEFAULT 0 CHECK (largo >= 0),
                fecha_registro TEXT NOT NULL,
                disponible INTEGER NOT NULL CHECK (disponible IN (0, 1))
            )
            """.trimIndent()
        )
    }

    // Se ejecuta cuando aumenta la versión de la base de datos.
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Versión 2: se agrega el largo del producto en centímetros.
        // Los productos que ya existían quedan con largo 0 hasta que se editen.
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE productos ADD COLUMN largo REAL NOT NULL DEFAULT 0 CHECK (largo >= 0)"
            )
        }
    }

    // Constantes que utiliza esta clase.
    companion object {

        // Nombre del archivo SQLite.
        private const val NOMBRE_BD = "productos.db"

        // Versión actual de la estructura de la base de datos.
        private const val VERSION_BD = 2

        // Nombre de la tabla de productos.
        const val TABLA_PRODUCTOS = "productos"
    }
}
