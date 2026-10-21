package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** SQLite local como UsuarioDbHelper del profesor, adaptado a siniestros. */
class SiniestroDbHelper(context: Context) :
    SQLiteOpenHelper(context, "seguros_bpo.db", null, 1) {

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // No hay versiones anteriores de esta base de datos en el MVP.
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

    fun listar(asegurado: Asegurado): List<Siniestro> {
        val resultado = mutableListOf<Siniestro>()
        readableDatabase.query(
            "siniestros", null, "asegurado = ?", arrayOf(asegurado.name),
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
}
