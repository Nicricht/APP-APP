package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.ValidadorContacto

class CuentaDbHelper(
    context: Context,
    nombreBase: String = "cuentas_demo.db"
) : SQLiteOpenHelper(context, nombreBase, null, 8) {

    private val PERSONAL_LIQUIDADOR = mapOf(
        Liquidador.VEHICULOS to listOf("María P. Lagos", "Esteban R. Fuentes"),
        Liquidador.HOGAR to listOf("Pedro C. Rivas", "Carolina M. Soto")
    )

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE cuentas (
                id TEXT PRIMARY KEY NOT NULL,
                nombre TEXT NOT NULL,
                rut TEXT NOT NULL,
                correo TEXT NOT NULL,
                telefono TEXT NOT NULL,
                rol TEXT NOT NULL,
                equipo TEXT,
                foto BLOB
            )
        """.trimIndent())
        precargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 8) {
            val tieneFoto = db.rawQuery("PRAGMA table_info(cuentas)", null).use { c ->
                var existe = false
                while (c.moveToNext()) {
                    if (c.getString(c.getColumnIndexOrThrow("name")) == "foto") existe = true
                }
                existe
            }
            db.execSQL("ALTER TABLE cuentas RENAME TO cuentas_anteriores")
            onCreate(db)
            val foto = if (tieneFoto) "foto" else "NULL"
            db.execSQL("""
                INSERT OR REPLACE INTO cuentas
                    (id, nombre, rut, correo, telefono, rol, equipo, foto)
                SELECT id, nombre, rut, correo, telefono, rol, equipo, $foto
                FROM cuentas_anteriores
            """.trimIndent())
            db.execSQL("DROP TABLE cuentas_anteriores")
        }
    }

    private fun precargar(db: SQLiteDatabase) {
        Asegurado.entries.forEach { asegurado ->
            insertar(db, ContentValues().apply {
                put("id", asegurado.name)
                put("nombre", asegurado.etiqueta)
                put("rut", asegurado.rut)
                put("correo", correoPersonal(asegurado.etiqueta))
                put("telefono", "900000000")
                put("rol", RolCuenta.ASEGURADO.name)
                putNull("equipo")
            })
        }
        PERSONAL_LIQUIDADOR.forEach { (equipo, nombres) ->
            nombres.forEachIndexed { indice, nombre ->
                insertar(db, ContentValues().apply {
                    put("id", "LIQ_" + equipo.name + "_" + (indice + 1))
                    put("nombre", nombre)
                    put("rut", "")
                    put("correo", correoCorporativo(nombre))
                    put("telefono", "900000000")
                    put("rol", RolCuenta.LIQUIDADOR.name)
                    put("equipo", equipo.name)
                })
            }
        }
        insertar(db, ContentValues().apply {
            put("id", "ANALISTA_01")
            put("nombre", "Paulina G.")
            put("rut", "")
            put("correo", "asignaciones@segurosbpo.cl")
            put("telefono", "900000000")
            put("rol", RolCuenta.ANALISTA.name)
            putNull("equipo")
        })
    }

    private fun correoCorporativo(nombre: String): String {
        val partes = nombre.lowercase().replace(".", "").split(' ').filter { it.isNotBlank() }
        val inicial = partes.firstOrNull()?.take(1) ?: "l"
        val apellido = partes.getOrNull(1) ?: "liquidador"
        return inicial + apellido + "@segurosbpo.cl"
    }

    fun listarLiquidadores(equipo: Liquidador? = null): List<CuentaLocal> =
        listar().filter { cuenta ->
            cuenta.rol == RolCuenta.LIQUIDADOR &&
                (equipo == null || cuenta.equipo == equipo.name)
        }

    fun crearLiquidador(nombre: String, equipo: Liquidador, telefono: String): CuentaLocal {
        val limpio = nombre.trim()
        require(limpio.length in 3..60) { "El nombre debe tener entre 3 y 60 caracteres" }
        require(limpio.contains(' ')) { "Escribe nombre y apellido" }
        ValidadorContacto.errorTelefono(telefono)?.let { throw IllegalArgumentException(it) }
        val existentes = listarLiquidadores(equipo)
        require(existentes.none { it.nombre.equals(limpio, ignoreCase = true) }) {
            "Ya hay un liquidador con ese nombre en el equipo"
        }
        val id = "LIQ_" + equipo.name + "_" + (listarLiquidadores().size + 1)
        val valores = ContentValues().apply {
            put("id", id)
            put("nombre", limpio)
            put("rut", "")
            put("correo", correoCorporativo(limpio))
            put("telefono", ValidadorContacto.normalizarTelefono(telefono))
            put("rol", RolCuenta.LIQUIDADOR.name)
            put("equipo", equipo.name)
        }
        val fila = writableDatabase.insertWithOnConflict(
            "cuentas", null, valores, SQLiteDatabase.CONFLICT_ABORT
        )
        require(fila != -1L) { "No se pudo crear el liquidador" }
        return requireNotNull(obtener(id)) { "No se pudo crear el liquidador" }
    }

    private fun correoPersonal(nombre: String): String {
        val partes = nombre.lowercase().split(' ')
        val inicial = partes.firstOrNull()?.take(1) ?: ""
        val apellido = partes.getOrNull(1) ?: "usuario"
        return inicial + apellido + "@correo.cl"
    }

    private fun insertar(db: SQLiteDatabase, valores: ContentValues) {
        db.insertWithOnConflict("cuentas", null, valores, SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun convertir(cursor: Cursor): CuentaLocal {
        val equipo = cursor.getColumnIndexOrThrow("equipo")
        return CuentaLocal(
            cursor.getString(cursor.getColumnIndexOrThrow("id")),
            cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            cursor.getString(cursor.getColumnIndexOrThrow("rut")),
            cursor.getString(cursor.getColumnIndexOrThrow("correo")),
            cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
            RolCuenta.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("rol"))),
            if (cursor.isNull(equipo)) null else cursor.getString(equipo),
            cursor.getColumnIndexOrThrow("foto").let { i ->
                if (cursor.isNull(i)) null else cursor.getBlob(i)
            }
        )
    }

    fun obtener(id: String): CuentaLocal? = readableDatabase.query(
        "cuentas", null, "id = ?", arrayOf(id), null, null, null
    ).use { if (it.moveToFirst()) convertir(it) else null }

    fun listar(): List<CuentaLocal> = readableDatabase.query(
        "cuentas", null, null, null, null, null, "rol DESC, nombre ASC"
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(convertir(cursor))
        }
    }

    fun actualizarTelefono(id: String, telefono: String): CuentaLocal {
        ValidadorContacto.errorTelefono(telefono)?.let { throw IllegalArgumentException(it) }
        val filas = writableDatabase.update(
            "cuentas", ContentValues().apply {
                put("telefono", ValidadorContacto.normalizarTelefono(telefono))
            },
            "id = ?", arrayOf(id)
        )
        require(filas == 1) { "No se encontró la cuenta" }
        return requireNotNull(obtener(id)) { "No se encontró la cuenta" }
    }

    fun actualizarContacto(id: String, correo: String, telefono: String): CuentaLocal {
        ValidadorContacto.errorCorreo(correo)?.let { throw IllegalArgumentException(it) }
        ValidadorContacto.errorTelefono(telefono)?.let { throw IllegalArgumentException(it) }
        val correoLimpio = ValidadorContacto.normalizarCorreo(correo)
        val telefonoLimpio = ValidadorContacto.normalizarTelefono(telefono)
        val filas = writableDatabase.update(
            "cuentas", ContentValues().apply {
                put("correo", correoLimpio)
                put("telefono", telefonoLimpio)
            },
            "id = ?", arrayOf(id)
        )
        require(filas == 1) { "No se encontró la cuenta" }
        return requireNotNull(obtener(id)) { "No se encontró la cuenta" }
    }

    fun actualizarFoto(id: String, imagenJpeg: ByteArray): CuentaLocal {
        require(imagenJpeg.isNotEmpty() && imagenJpeg.size <= 1024 * 1024) {
            "La foto no puede superar 1 MB"
        }
        val filas = writableDatabase.update(
            "cuentas", ContentValues().apply { put("foto", imagenJpeg) },
            "id = ?", arrayOf(id)
        )
        require(filas == 1) { "No se encontró la cuenta" }
        return requireNotNull(obtener(id)) { "No se encontró la cuenta" }
    }
}
