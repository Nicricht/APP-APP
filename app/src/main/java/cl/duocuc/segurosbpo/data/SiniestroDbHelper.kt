package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** SQLite local como UsuarioDbHelper del profesor, adaptado a siniestros. */
class SiniestroDbHelper(context: Context) :
    SQLiteOpenHelper(context, "seguros_bpo.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE siniestros (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero TEXT NOT NULL UNIQUE,
                asegurado TEXT NOT NULL,
                compania TEXT NOT NULL,
                tipo TEXT NOT NULL,
                fecha_ocurrencia TEXT NOT NULL,
                fecha_reporte TEXT NOT NULL,
                comuna TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                hubo_terceros INTEGER NOT NULL,
                etapa TEXT NOT NULL,
                ultima_actualizacion TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_siniestros_asegurado ON siniestros(asegurado)")
        crearTablaEvidencias(db)
    }

    private fun crearTablaEvidencias(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS evidencias (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                siniestro_id INTEGER NOT NULL,
                tipo_documento TEXT NOT NULL,
                nombre_archivo TEXT NOT NULL,
                mime TEXT NOT NULL,
                tamano_bytes INTEGER NOT NULL,
                contenido BLOB NOT NULL,
                fecha_registro TEXT NOT NULL,
                FOREIGN KEY(siniestro_id) REFERENCES siniestros(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_evidencia_caso ON evidencias(siniestro_id)")
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versión 1 -> 2: conservar los siniestros existentes y agregar documentos.
        if (oldVersion < 2) crearTablaEvidencias(db)
    }

    private fun fechaActual(): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

    /** El correlativo nace del ID que SQLite ya guardó y no se reinicia al limpiar. */
    fun registrar(formulario: FormularioUiState): Siniestro {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val ahora = fechaActual()
            val valores = ContentValues().apply {
                put("numero", "TEMP-${System.nanoTime()}")
                put("asegurado", formulario.asegurado.name)
                put("compania", formulario.compania.name)
                put("tipo", formulario.tipoSiniestro.name)
                put("fecha_ocurrencia", formulario.fechaOcurrencia)
                put("fecha_reporte", ahora.substring(0, 10))
                put("comuna", formulario.comuna.trim())
                put("descripcion", formulario.descripcion.trim())
                put("hubo_terceros", if (formulario.huboTerceros) 1 else 0)
                put("etapa", EtapaSiniestro.RECIBIDO.name)
                put("ultima_actualizacion", ahora)
            }
            val id = db.insertOrThrow("siniestros", null, valores)
            val anio = SimpleDateFormat("yy", Locale.ROOT).format(Date())
            val numero = "SIN-$anio-${(id + 4860).toString().padStart(6, '0')}"
            db.execSQL("UPDATE siniestros SET numero = ? WHERE id = ?", arrayOf(numero, id))
            db.setTransactionSuccessful()
            return Siniestro(
                id, numero, formulario.asegurado, formulario.compania,
                formulario.tipoSiniestro, formulario.fechaOcurrencia, ahora.substring(0, 10),
                formulario.comuna.trim(), formulario.descripcion.trim(), formulario.huboTerceros,
                EtapaSiniestro.RECIBIDO, ahora
            )
        } finally {
            db.endTransaction()
        }
    }

    fun listar(asegurado: Asegurado, busqueda: String = ""): List<Siniestro> {
        val resultado = mutableListOf<Siniestro>()
        val textoBuscado = busqueda.trim()
        val patron = "%${textoBuscado.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")}%"
        val columnas = listOf("numero", "compania", "tipo", "comuna", "etapa")
        val condicion = if (textoBuscado.isBlank()) "asegurado = ?" else
            "asegurado = ? AND (" + columnas.joinToString(" OR ") {
                "$it LIKE ? ESCAPE '\\'"
            } + ")"
        val argumentos = if (textoBuscado.isBlank()) arrayOf(asegurado.name)
            else arrayOf(asegurado.name, *Array(columnas.size) { patron })
        readableDatabase.query(
            "siniestros", null, condicion, argumentos,
            null, null, "id DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                fun texto(col: String) = cursor.getString(cursor.getColumnIndexOrThrow(col))
                resultado.add(
                    Siniestro(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        texto("numero"), Asegurado.valueOf(texto("asegurado")),
                        Compania.valueOf(texto("compania")),
                        TipoSiniestro.valueOf(texto("tipo")),
                        texto("fecha_ocurrencia"), texto("fecha_reporte"),
                        texto("comuna"), texto("descripcion"),
                        cursor.getInt(cursor.getColumnIndexOrThrow("hubo_terceros")) == 1,
                        EtapaSiniestro.valueOf(texto("etapa")), texto("ultima_actualizacion")
                    )
                )
            }
        }
        return resultado
    }

    fun obtener(id: Long, asegurado: Asegurado): Siniestro? =
        listar(asegurado).firstOrNull { it.id == id }

    /** Por ahora, la corrección se limita a la descripción de un caso recibido. */
    fun actualizarDescripcion(id: Long, asegurado: Asegurado, descripcion: String): Boolean {
        if (descripcion.isBlank()) return false
        val valores = ContentValues().apply {
            put("descripcion", descripcion.trim())
            put("ultima_actualizacion", fechaActual())
        }
        return writableDatabase.update(
            "siniestros", valores,
            "id = ? AND asegurado = ? AND etapa = ?",
            arrayOf(id.toString(), asegurado.name, EtapaSiniestro.RECIBIDO.name)
        ) > 0
    }

    /** Lista metadatos únicamente si el asegurado es dueño del caso. */
    fun listarEvidencias(siniestroId: Long, asegurado: Asegurado): List<Evidencia> {
        val resultado = mutableListOf<Evidencia>()
        readableDatabase.rawQuery("""
            SELECT e.id, e.siniestro_id, e.tipo_documento, e.nombre_archivo,
                   e.mime, e.tamano_bytes, e.fecha_registro
            FROM evidencias e JOIN siniestros s ON s.id = e.siniestro_id
            WHERE s.id = ? AND s.asegurado = ?
            ORDER BY e.id DESC
        """.trimIndent(), arrayOf(siniestroId.toString(), asegurado.name)).use { cursor ->
            while (cursor.moveToNext()) {
                resultado.add(Evidencia(
                    cursor.getLong(0), cursor.getLong(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6)
                ))
            }
        }
        return resultado
    }

    /** Comprueba propiedad, etapa y límites en la misma transacción del guardado. */
    fun guardarEvidencia(
        siniestroId: Long, asegurado: Asegurado, tipoDocumento: String,
        archivo: ArchivoEvidencia
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val caso = db.rawQuery(
                "SELECT etapa FROM siniestros WHERE id = ? AND asegurado = ?",
                arrayOf(siniestroId.toString(), asegurado.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
            require(caso != null) { "No existe ese siniestro del asegurado" }
            require(caso != EtapaSiniestro.CERRADO.name) { "No puedes adjuntar documentos a un caso cerrado" }
            val cantidad = db.rawQuery(
                "SELECT COUNT(*) FROM evidencias WHERE siniestro_id = ?",
                arrayOf(siniestroId.toString())
            ).use { c -> c.moveToFirst(); c.getInt(0) }
            val error = ValidadorEvidencia.error(
                tipoDocumento, archivo.mime, archivo.bytes.size, cantidad
            )
            require(error == null) { error ?: "Archivo inválido" }
            val valores = ContentValues().apply {
                put("siniestro_id", siniestroId)
                put("tipo_documento", tipoDocumento.trim())
                put("nombre_archivo", archivo.nombre)
                put("mime", archivo.mime)
                put("tamano_bytes", archivo.bytes.size)
                put("contenido", archivo.bytes)
                put("fecha_registro", fechaActual())
            }
            db.insertOrThrow("evidencias", null, valores)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

}
