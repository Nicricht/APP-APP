package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.RolCuenta
import cl.duocuc.segurosbpo.model.ValidadorCuenta
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class CuentaDbHelper(
    context: Context,
    nombreBase: String = "cuentas_demo.db"
) : SQLiteOpenHelper(context, nombreBase, null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE cuentas (
                id TEXT PRIMARY KEY NOT NULL,
                nombre TEXT NOT NULL,
                rut TEXT NOT NULL UNIQUE COLLATE NOCASE,
                correo TEXT NOT NULL UNIQUE COLLATE NOCASE,
                telefono TEXT NOT NULL,
                rol TEXT NOT NULL,
                equipo TEXT,
                salt BLOB NOT NULL,
                clave_hash BLOB NOT NULL,
                foto BLOB
            )
        """.trimIndent())
        insertarLiquidadorDemo(db, "LIQ_VEHICULOS", "María P.", "VEHICULOS",
            "vehiculos@segurosbpo.demo", "DEMO-VEHICULOS")
        insertarLiquidadorDemo(db, "LIQ_HOGAR", "Pedro C.", "HOGAR",
            "hogar@segurosbpo.demo", "DEMO-HOGAR")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("ALTER TABLE cuentas ADD COLUMN foto BLOB")
        if (oldVersion < 3) {
            insertarLiquidadorDemo(db, "LIQ_VEHICULOS", "María P.", "VEHICULOS",
                "vehiculos@segurosbpo.demo", "DEMO-VEHICULOS")
            insertarLiquidadorDemo(db, "LIQ_HOGAR", "Pedro C.", "HOGAR",
                "hogar@segurosbpo.demo", "DEMO-HOGAR")
        }
    }

    private fun insertarLiquidadorDemo(
        db: SQLiteDatabase,
        id: String,
        nombre: String,
        equipo: String,
        correo: String,
        rut: String
    ) {
        val sal = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivar("Demo2026!", sal)
        db.insertWithOnConflict("cuentas", null, ContentValues().apply {
            put("id", id)
            put("nombre", nombre)
            put("rut", rut)
            put("correo", correo)
            put("telefono", "900000000")
            put("rol", RolCuenta.LIQUIDADOR.name)
            put("equipo", equipo)
            put("salt", sal)
            put("clave_hash", hash)
        }, SQLiteDatabase.CONFLICT_IGNORE)
    }

    private fun derivar(clave: String, salt: ByteArray): ByteArray {
        val parametros = PBEKeySpec(clave.toCharArray(), salt, 60000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(parametros).encoded
        } finally {
            parametros.clearPassword()
        }
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

    fun registrarAsegurado(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        clave: String,
        confirmacion: String
    ): CuentaLocal {
        ValidadorCuenta.errorRegistro(nombre, rut, correo, telefono, clave, confirmacion)
            ?.let { throw IllegalArgumentException(it) }
        val id = "USR_" + UUID.randomUUID().toString()
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = derivar(clave, salt)
        try {
            writableDatabase.insertOrThrow("cuentas", null, ContentValues().apply {
                put("id", id)
                put("nombre", nombre.trim())
                put("rut", rut.trim().uppercase().replace(".", ""))
                put("correo", ValidadorCuenta.normalizarCorreo(correo))
                put("telefono", telefono.trim())
                put("rol", RolCuenta.ASEGURADO.name)
                putNull("equipo")
                put("salt", salt)
                put("clave_hash", hash)
            })
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            throw IllegalArgumentException("Ya existe una cuenta con ese correo o RUT", e)
        }
        return requireNotNull(obtener(id))
    }

    fun actualizarTelefono(id: String, telefono: String): CuentaLocal {
        require(telefono.trim().length >= 8) { "El teléfono debe tener al menos 8 caracteres" }
        val filas = writableDatabase.update(
            "cuentas", ContentValues().apply { put("telefono", telefono.trim()) },
            "id = ?", arrayOf(id)
        )
        require(filas == 1) { "No se encontró la cuenta" }
        return requireNotNull(obtener(id))
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
        return requireNotNull(obtener(id))
    }

    fun iniciarSesion(correo: String, clave: String): CuentaLocal? {
        if (correo.isBlank() || clave.isBlank()) return null
        return readableDatabase.query(
            "cuentas", null, "correo = ?",
            arrayOf(ValidadorCuenta.normalizarCorreo(correo)),
            null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val sal = cursor.getBlob(cursor.getColumnIndexOrThrow("salt"))
            val hash = cursor.getBlob(cursor.getColumnIndexOrThrow("clave_hash"))
            if (MessageDigest.isEqual(hash, derivar(clave, sal))) convertir(cursor) else null
        }
    }
}
