package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.PerfilesDemostracion
import cl.duocuc.segurosbpo.model.RolCuenta

/** Perfiles locales para las pruebas académicas; nunca persiste claves ni hashes. */
class CuentaDbHelper(
    context: Context,
    nombreBase: String = "cuentas_demo.db"
) : SQLiteOpenHelper(context, nombreBase, null, 4) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Al retirar la tabla heredada, borrar también su contenido en páginas libres.
        db.execSQL("PRAGMA secure_delete=ON")
    }

    override fun onCreate(db: SQLiteDatabase) {
        crearTabla(db, "cuentas")
        insertarPerfilesFicticios(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            // Conserva perfiles antiguos y sus fotos, pero retira las columnas
            // salt/clave_hash, que no están permitidas por RNF-08.
            crearTabla(db, "cuentas_sin_claves")
            val tieneFoto = db.rawQuery("PRAGMA table_info(cuentas)", null).use { cursor ->
                val indiceNombre = cursor.getColumnIndexOrThrow("name")
                var encontrada = false
                while (cursor.moveToNext()) {
                    if (cursor.getString(indiceNombre) == "foto") encontrada = true
                }
                encontrada
            }
            val foto = if (tieneFoto) "foto" else "NULL"
            db.execSQL("""
                INSERT INTO cuentas_sin_claves
                    (id, nombre, rut, correo, telefono, rol, equipo, foto)
                SELECT id, nombre, rut, correo, telefono, rol, equipo, $foto FROM cuentas
            """.trimIndent())
            db.execSQL("DROP TABLE cuentas")
            db.execSQL("ALTER TABLE cuentas_sin_claves RENAME TO cuentas")
            insertarPerfilesFicticios(db)
        }
    }

    private fun crearTabla(db: SQLiteDatabase, nombre: String) {
        db.execSQL("""
            CREATE TABLE $nombre (
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
    }

    private fun insertarPerfilesFicticios(db: SQLiteDatabase) {
        PerfilesDemostracion.todos.forEach { perfil ->
            db.insertWithOnConflict("cuentas", null, ContentValues().apply {
                put("id", perfil.id)
                put("nombre", perfil.nombre)
                put("rut", perfil.rut)
                put("correo", perfil.correo)
                put("telefono", perfil.telefono)
                put("rol", perfil.rol.name)
                if (perfil.equipo == null) putNull("equipo")
                else put("equipo", perfil.equipo)
            }, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    private fun convertir(cursor: Cursor): CuentaLocal {
        val equipo = cursor.getColumnIndexOrThrow("equipo")
        return CuentaLocal(
            id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
            rut = cursor.getString(cursor.getColumnIndexOrThrow("rut")),
            correo = cursor.getString(cursor.getColumnIndexOrThrow("correo")),
            telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
            rol = RolCuenta.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("rol"))),
            equipo = if (cursor.isNull(equipo)) null else cursor.getString(equipo),
            foto = cursor.getColumnIndexOrThrow("foto").let { indice ->
                if (cursor.isNull(indice)) null else cursor.getBlob(indice)
            }
        )
    }

    fun listarPerfiles(): List<CuentaLocal> = readableDatabase.query(
        "cuentas", null, null, null, null, null,
        "CASE WHEN id LIKE 'USR_%' THEN 1 ELSE 0 END, " +
            "CASE WHEN rol = 'ASEGURADO' THEN 0 ELSE 1 END, nombre"
    ).use { cursor ->
        buildList { while (cursor.moveToNext()) add(convertir(cursor)) }
    }

    fun obtener(id: String): CuentaLocal? = readableDatabase.query(
        "cuentas", null, "id = ?", arrayOf(id), null, null, null
    ).use { if (it.moveToFirst()) convertir(it) else null }

    fun actualizarTelefono(id: String, telefono: String): CuentaLocal {
        require(telefono.isBlank() || telefono.trim().length >= 8) {
            "El teléfono debe tener al menos 8 caracteres"
        }
        val filas = writableDatabase.update(
            "cuentas", ContentValues().apply { put("telefono", telefono.trim()) },
            "id = ?", arrayOf(id)
        )
        require(filas == 1) { "No se encontró el perfil" }
        return requireNotNull(obtener(id)) { "No se encontró el perfil" }
    }

    fun actualizarFoto(id: String, imagenJpeg: ByteArray): CuentaLocal {
        require(imagenJpeg.isNotEmpty() && imagenJpeg.size <= 1024 * 1024) {
            "La foto no puede superar 1 MB"
        }
        val filas = writableDatabase.update(
            "cuentas", ContentValues().apply { put("foto", imagenJpeg) },
            "id = ?", arrayOf(id)
        )
        require(filas == 1) { "No se encontró el perfil" }
        return requireNotNull(obtener(id)) { "No se encontró el perfil" }
    }
}
