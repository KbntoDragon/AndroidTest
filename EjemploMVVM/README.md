# CRUD de Productos — MVVM + Jetpack Compose + SQLite

Este proyecto es una actualización directa de **EjemploMVVM.zip**. Se respetó la estructura y las versiones del proyecto original, y se reemplazó la pantalla inicial por un mantenedor funcional de productos.

## Configuración preservada

- `namespace` / `applicationId`: `com.fabiancg.ejemplomvvm`.
- Android Gradle Plugin **9.2.1**.
- Kotlin **2.2.10** y plugin Kotlin Compose existente.
- Gradle Wrapper **9.4.1**.
- `compileSdk` **37.1** (API 37 con `minorApiLevel = 1`), `targetSdk` **36**, `minSdk` **33**.
- Jetpack Compose Material 3, BOM **2026.02.01**.
- Java **11** para el código y toolchain del daemon de Gradle en **JDK 21**.
- Se agregaron **solo dos dependencias AndroidX Lifecycle**, usando la versión **2.11.0 que ya tenía el proyecto**: `lifecycle-viewmodel-ktx` y `lifecycle-runtime-compose`. No se agregó Room, KSP ni bibliotecas de inyección de dependencias.

## Abrir en Android Studio

1. Descomprime el archivo ZIP.
2. En Android Studio elige **Open** y selecciona la carpeta **EjemploMVVM** (la que contiene `settings.gradle.kts`).
3. Permite que Gradle sincronice. Usa un JDK 21 para Gradle; el proyecto original incluye configuración de Gradle Daemon JVM con toolchain 21.
4. Verifica que Android Studio tenga instalado el SDK solicitado por el proyecto base (compileSdk 37.1) y ejecuta la aplicación en un emulador/dispositivo con Android **13 / API 33 o superior**.
5. No se necesitan credenciales, Internet, servidores ni configuración adicional para SQLite.

> Este paquete conserva el Gradle Wrapper del original. En el entorno donde se generó no estaba disponible Android SDK, por lo que **no se pudo compilar ni ejecutar en emulador**. Se comprobaron estructuralmente los archivos y las reglas SQL por separado.

## Requisitos del producto

| Campo | Kotlin | Columna SQLite | Regla |
| --- | --- | --- | --- |
| Código | `String` | `codigo TEXT` | Clave primaria (`COLLATE NOCASE`); obligatorio, sin duplicados, aun si cambian mayúsculas/minúsculas |
| Nombre | `String` | `nombre TEXT` | Obligatorio |
| Precio | `Double` | `precio REAL` | Mayor que cero |
| Largo | `Double` | `largo REAL` | En centímetros; mayor que cero (acepta decimales con coma o punto) |
| Fecha de registro | `String` | `fecha_registro TEXT` | Fecha actual en formato ISO `YYYY-MM-DD` al crear; no cambia al editar |
| Disponible | `Boolean` | `disponible INTEGER` | `1` = disponible; `0` = no disponible |

Los códigos se almacenan en **mayúsculas y sin espacios alrededor** (`p001` se guarda como `P001`). La base de datos también comprueba la unicidad independientemente de la validación en Kotlin.

## Archivos y capas MVVM

```text
EjemploMVVM/
└── app/src/main/java/com/fabiancg/ejemplomvvm/
    ├── MainActivity.kt                  # Ensambla Repository, ViewModel y Vista
    ├── model/
    │   └── Producto.kt                  # Model: entidad de negocio
    ├── data/
    │   ├── ProductoDatabaseHelper.kt    # SQLiteOpenHelper, CREATE TABLE
    │   └── ProductoRepository.kt        # CRUD sobre SQLite y Dispatchers.IO
    ├── viewmodel/
    │   ├── ProductoUiState.kt           # Estado observable del formulario y lista
    │   └── ProductoViewModel.kt         # Validación, selección y acciones CRUD
    └── ui/
        ├── ProductosScreen.kt           # View: Jetpack Compose
        └── theme/                       # Tema original
```

```text
Usuario pulsa Registrar / Editar / Eliminar
                 ↓
Vista (ProductosScreen, Compose)
                 ↓ envía evento
ViewModel (ProductoViewModel, StateFlow)
                 ↓ llama a
Repository (ProductoRepository)
                 ↓ SQL en Dispatchers.IO
SQLiteOpenHelper → productos.db
                 ↓
Repository devuelve lista actualizada
                 ↓
ViewModel publica nuevo ProductoUiState
                 ↓
Compose actualiza la pantalla automáticamente
```

## Operaciones CRUD

- **Crear:** valida código, nombre, precio y largo (cm). Agrega `LocalDate.now().toString()` y estado disponible/no disponible. Si el código existe, muestra un mensaje sin modificar la base.
- **Leer:** al iniciar, ejecuta `SELECT` y muestra la lista ordenada por nombre. Después de cada operación se vuelve a leer la tabla para reflejar los cambios.
- **Actualizar:** al tocar **Editar**, rellena el formulario y bloquea el código. Permite modificar nombre, precio, largo y estado; se conserva fecha original.
- **Eliminar:** al tocar **Eliminar**, aparece un diálogo de confirmación. Al aceptar, se borra el registro seleccionado.
- **Cancelar edición:** vuelve al modo Registrar con el formulario limpio.
- **Ordenar:** junto al botón Registrar hay dos botones, **Nombre** (A a Z) y **Precio** (menor a mayor). El orden activo se muestra relleno y se mantiene al registrar, editar o eliminar.

## Cómo probar

1. Registra `P001`, nombre `Mouse`, precio `12990`, disponible.
2. Registra `P002`, nombre `Teclado`, precio `25000`, no disponible.
3. Intenta registrar `p001`: debe indicar que ya existe (sin duplicarlo).
4. Prueba guardar un precio `0`, `-3` o texto no numérico: debe mostrar validación.
5. Edita `P001`: cambia el nombre a `Mouse inalámbrico`, el precio y el estado. El código y la fecha no deben cambiar.
6. Cierra y abre la app: los productos deben mantenerse en SQLite.
7. Elimina `P002` con confirmación. Comprueba que desaparece del listado.

## Sobre SQLite y Room

Esta versión usa **SQLite nativo** mediante `android.database.sqlite.SQLiteOpenHelper`. No utiliza Room: no requiere anotaciones `@Entity` / `@Dao`, KSP ni generación de código. Room también se apoya en SQLite internamente, pero aquí se eligió la API nativa para mantener el ejemplo sencillo y las dependencias mínimas.

La base se guarda en el almacenamiento privado de la aplicación (`productos.db`). No requiere permiso de almacenamiento. Se crea cuando se realiza la primera consulta a la base. La versión inicial es 1; si en el futuro cambia el esquema, hay que implementar las migraciones correspondientes en `onUpgrade`, sin eliminar los datos.

**Nota técnica:** Se emplea `Double` / `REAL` para mantener el ejemplo introductorio. Para operaciones financieras con precisión decimal estricta, conviene representar importes en unidades enteras mínimas o una solución decimal adecuada.
