package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SiniestroDbHelper(
    context: Context,
    databaseName: String = "seguros_bpo.db"
) : SQLiteOpenHelper(context, databaseName, null, 4) {

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
                liquidador TEXT,
                resultado_cierre TEXT,
                mensaje_cierre TEXT,
                ultima_actualizacion TEXT NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_siniestros_asegurado ON siniestros(asegurado)")
        crearTablaEvidencias(db)
        crearTablaGestiones(db)
        cargarSiniestrosEjemplo(db)
    }

    
    private fun cargarSiniestrosEjemplo(db: SQLiteDatabase) {
        data class Ejemplo(
            val asegurado: Asegurado,
            val compania: Compania,
            val tipo: TipoSiniestro,
            val comuna: String,
            val descripcion: String,
            val etapa: EtapaSiniestro
        )

        val ejemplos = listOf(
            Ejemplo(Asegurado.CAMILA, Compania.CORDILLERA, TipoSiniestro.CHOQUE,
                "Quilicura", "Colisión leve en estacionamiento.", EtapaSiniestro.RECIBIDO),
            Ejemplo(Asegurado.JORGE, Compania.LITORAL, TipoSiniestro.DANO_AGUA,
                "Maipú", "Filtración de agua en la cocina.", EtapaSiniestro.EVALUACION),
            Ejemplo(Asegurado.VALENTINA, Compania.PATAGONIA, TipoSiniestro.CRISTALES,
                "Santiago", "Rotura de cristal lateral del vehículo.", EtapaSiniestro.LIQUIDACION),
            Ejemplo(Asegurado.HECTOR, Compania.VALLE_CENTRAL, TipoSiniestro.ROBO_HOGAR,
                "Conchalí", "Robo de objetos del hogar.", EtapaSiniestro.CERRADO),
            Ejemplo(Asegurado.DANIELA, Compania.PROTEGE, TipoSiniestro.INCENDIO,
                "Independencia", "Daños por incendio en vivienda.", EtapaSiniestro.EVALUACION)
        )
        val fechaOcurrencia = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT)
            .format(Date(System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000))
        val ahora = fechaActual()
        val anio = SimpleDateFormat("yy", Locale.ROOT).format(Date())
        ejemplos.forEach { ejemplo ->
            val valores = ContentValues().apply {
                put("numero", "TEMP-EJEMPLO-${ejemplo.asegurado.name}")
                put("asegurado", ejemplo.asegurado.name)
                put("compania", ejemplo.compania.name)
                put("tipo", ejemplo.tipo.name)
                put("fecha_ocurrencia", fechaOcurrencia)
                put("fecha_reporte", ahora.take(10))
                put("comuna", ejemplo.comuna)
                put("descripcion", ejemplo.descripcion)
                put("hubo_terceros", 0)
                put("etapa", ejemplo.etapa.name)
                if (ejemplo.etapa != EtapaSiniestro.RECIBIDO) {
                    put("liquidador", if (ejemplo.tipo.esVehiculo) Liquidador.VEHICULOS.name
                        else Liquidador.HOGAR.name)
                }
                put("ultima_actualizacion", ahora)
            }
            val id = db.insertOrThrow("siniestros", null, valores)
            val numero = "SIN-$anio-${(id + 4860).toString().padStart(6, '0')}"
            db.execSQL("UPDATE siniestros SET numero = ? WHERE id = ?", arrayOf(numero, id))
            insertarGestion(db, id, "Ejemplo académico", "Caso ficticio disponible en etapa: " + ejemplo.etapa.etiqueta, true)
        }
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

    private fun crearTablaGestiones(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS gestiones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                siniestro_id INTEGER NOT NULL,
                fecha TEXT NOT NULL,
                responsable TEXT NOT NULL,
                descripcion TEXT NOT NULL,
                visible_asegurado INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY(siniestro_id) REFERENCES siniestros(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_gestiones_caso ON gestiones(siniestro_id, id)")
    }

    private fun insertarGestion(db: SQLiteDatabase, siniestroId: Long,
        responsable: String, descripcion: String, visible: Boolean
    ) {
        val valores = ContentValues().apply {
            put("siniestro_id", siniestroId)
            put("fecha", fechaActual())
            put("responsable", responsable)
            put("descripcion", descripcion)
            put("visible_asegurado", if (visible) 1 else 0)
        }
        db.insertOrThrow("gestiones", null, valores)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        if (oldVersion < 2) crearTablaEvidencias(db)
        if (oldVersion < 3) {
            crearTablaGestiones(db)

            db.rawQuery("SELECT id FROM siniestros", null).use { cursor ->
                while (cursor.moveToNext()) {
                    insertarGestion(db, cursor.getLong(0), "Sistema",
                        "Caso incorporado al historial local. No hay gestiones anteriores disponibles.", true)
                }
            }
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE siniestros ADD COLUMN liquidador TEXT")
            db.execSQL("ALTER TABLE siniestros ADD COLUMN resultado_cierre TEXT")
            db.execSQL("ALTER TABLE siniestros ADD COLUMN mensaje_cierre TEXT")

            db.execSQL("""
                UPDATE siniestros SET liquidador = CASE
                    WHEN tipo IN ('CHOQUE', 'ROBO_VEHICULO', 'CRISTALES') THEN 'VEHICULOS'
                    ELSE 'HOGAR' END
                WHERE etapa <> 'RECIBIDO'
            """.trimIndent())
        }
    }

    private fun fechaActual(): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

    
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
                putNull("liquidador")
                put("ultima_actualizacion", ahora)
            }
            val id = db.insertOrThrow("siniestros", null, valores)
            val anio = SimpleDateFormat("yy", Locale.ROOT).format(Date())
            val numero = "SIN-$anio-${(id + 4860).toString().padStart(6, '0')}"
            db.execSQL("UPDATE siniestros SET numero = ? WHERE id = ?", arrayOf(numero, id))
            insertarGestion(db, id, "Asegurado", "Reporte registrado con número " + numero, true)
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

    private fun convertir(cursor: Cursor): Siniestro {
        fun texto(col: String) = cursor.getString(cursor.getColumnIndexOrThrow(col))
        fun opcional(col: String): String? {
            val indice = cursor.getColumnIndexOrThrow(col)
            return if (cursor.isNull(indice)) null else cursor.getString(indice)
        }
        return Siniestro(
            cursor.getLong(cursor.getColumnIndexOrThrow("id")),
            texto("numero"), Asegurado.valueOf(texto("asegurado")),
            Compania.valueOf(texto("compania")), TipoSiniestro.valueOf(texto("tipo")),
            texto("fecha_ocurrencia"), texto("fecha_reporte"), texto("comuna"),
            texto("descripcion"), cursor.getInt(cursor.getColumnIndexOrThrow("hubo_terceros")) == 1,
            EtapaSiniestro.valueOf(texto("etapa")), texto("ultima_actualizacion"),
            opcional("liquidador")?.let { Liquidador.valueOf(it) },
            opcional("resultado_cierre"), opcional("mensaje_cierre")
        )
    }

    fun listarAsignados(liquidador: Liquidador): List<Siniestro> {
        val casos = mutableListOf<Siniestro>()
        readableDatabase.query(
            "siniestros", null, "liquidador = ?", arrayOf(liquidador.name),
            null, null, "id DESC"
        ).use { c -> while (c.moveToNext()) casos.add(convertir(c)) }
        return casos
    }

    fun obtenerAsignado(id: Long, liquidador: Liquidador): Siniestro? =
        readableDatabase.query(
            "siniestros", null, "id = ? AND liquidador = ?",
            arrayOf(id.toString(), liquidador.name), null, null, null
        ).use { c -> if (c.moveToFirst()) convertir(c) else null }

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
                resultado.add(convertir(cursor))
            }
        }
        return resultado
    }

    fun obtener(id: Long, asegurado: Asegurado): Siniestro? =
        listar(asegurado).firstOrNull { it.id == id }

    
    fun actualizarDescripcion(id: Long, asegurado: Asegurado, descripcion: String): Boolean {
        if (descripcion.isBlank()) return false
        val db = writableDatabase
        db.beginTransaction()
        try {
            val valores = ContentValues().apply {
                put("descripcion", descripcion.trim())
                put("ultima_actualizacion", fechaActual())
            }
            val ok = db.update("siniestros", valores,
                "id = ? AND asegurado = ? AND etapa = ?",
                arrayOf(id.toString(), asegurado.name, EtapaSiniestro.RECIBIDO.name)) > 0
            if (ok) insertarGestion(db, id, "Asegurado", "Descripción corregida", true)
            db.setTransactionSuccessful()
            return ok
        } finally {
            db.endTransaction()
        }
    }

    
    fun obtenerImagen(evidenciaId: Long, siniestroId: Long, asegurado: Asegurado): Bitmap? {
        val contenido = readableDatabase.rawQuery("""
            SELECT e.contenido FROM evidencias e
            JOIN siniestros s ON s.id = e.siniestro_id
            WHERE e.id = ? AND s.id = ? AND s.asegurado = ? AND e.mime LIKE 'image/%'
        """.trimIndent(), arrayOf(evidenciaId.toString(), siniestroId.toString(), asegurado.name))
            .use { c -> if (c.moveToFirst()) c.getBlob(0) else null } ?: return null
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(contenido, 0, contenido.size, opciones)
        val reducir = BitmapFactory.Options()
        var escala = 1
        while (opciones.outWidth / escala > 900 || opciones.outHeight / escala > 900) {
            escala *= 2
        }
        reducir.inSampleSize = escala
        return BitmapFactory.decodeByteArray(contenido, 0, contenido.size, reducir)
    }

    
    fun listarGestiones(siniestroId: Long, asegurado: Asegurado): List<Gestion> {
        val resultado = mutableListOf<Gestion>()
        readableDatabase.rawQuery("""
            SELECT g.id, g.siniestro_id, g.fecha, g.responsable,
                   g.descripcion, g.visible_asegurado
            FROM gestiones g JOIN siniestros s ON s.id = g.siniestro_id
            WHERE s.id = ? AND s.asegurado = ? AND g.visible_asegurado = 1
            ORDER BY g.id DESC
        """.trimIndent(), arrayOf(siniestroId.toString(), asegurado.name)).use { c ->
            while (c.moveToNext()) {
                resultado.add(Gestion(c.getLong(0), c.getLong(1),
                    c.getString(2), c.getString(3), c.getString(4),
                    c.getInt(5) == 1))
            }
        }
        return resultado
    }

    
    fun listarGestionesAsignadas(siniestroId: Long, liquidador: Liquidador): List<Gestion> {
        val resultado = mutableListOf<Gestion>()
        readableDatabase.rawQuery("""
            SELECT g.id, g.siniestro_id, g.fecha, g.responsable,
                   g.descripcion, g.visible_asegurado
            FROM gestiones g JOIN siniestros s ON s.id = g.siniestro_id
            WHERE s.id = ? AND s.liquidador = ?
            ORDER BY g.id DESC
        """.trimIndent(), arrayOf(siniestroId.toString(), liquidador.name)).use { c ->
            while (c.moveToNext()) {
                resultado.add(Gestion(c.getLong(0), c.getLong(1),
                    c.getString(2), c.getString(3), c.getString(4),
                    c.getInt(5) == 1))
            }
        }
        return resultado
    }

    fun registrarGestionAsignada(siniestroId: Long, liquidador: Liquidador,
        descripcion: String, visible: Boolean
    ) {
        val error = ReglasEtapa.validarGestion(descripcion)
        require(error == null) { error ?: "Descripción inválida" }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val caso = db.rawQuery(
                "SELECT etapa FROM siniestros WHERE id = ? AND liquidador = ?",
                arrayOf(siniestroId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
            require(caso != null) { "El caso no está asignado a este liquidador" }
            require(caso != EtapaSiniestro.CERRADO.name) { "No se modifica un caso cerrado" }
            insertarGestion(db, siniestroId, liquidador.nombre, descripcion.trim(), visible)
            val valores = ContentValues().apply { put("ultima_actualizacion", fechaActual()) }
            db.update("siniestros", valores, "id = ? AND liquidador = ?",
                arrayOf(siniestroId.toString(), liquidador.name))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun avanzarAsignado(siniestroId: Long, liquidador: Liquidador,
        resultado: String, mensajeCierre: String
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val actual = obtenerAsignado(siniestroId, liquidador)
                ?: throw IllegalArgumentException("El caso no está asignado a este liquidador")


            val siguiente = ReglasEtapa.siguiente(actual.etapa)
                ?: throw IllegalArgumentException("Un siniestro cerrado no puede reabrirse")
            val error = ReglasEtapa.validar(actual.etapa, siguiente, 0, resultado, mensajeCierre)
            require(error == null) { error ?: "Transición inválida" }
            val valores = ContentValues().apply {
                put("etapa", siguiente.name)
                put("ultima_actualizacion", fechaActual())
                if (siguiente == EtapaSiniestro.CERRADO) {
                    put("resultado_cierre", resultado)
                    put("mensaje_cierre", mensajeCierre.trim())
                }
            }
            val filas = db.update("siniestros", valores,
                "id = ? AND liquidador = ? AND etapa = ?",
                arrayOf(siniestroId.toString(), liquidador.name, actual.etapa.name))
            require(filas == 1) { "El estado del siniestro cambió. Actualiza la pantalla" }
            val nota = if (siguiente == EtapaSiniestro.CERRADO)
                "Caso cerrado: " + resultado + ". " + mensajeCierre.trim()
                else "Etapa actualizada a " + siguiente.etiqueta
            insertarGestion(db, siniestroId, liquidador.nombre, nota, true)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    
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
            insertarGestion(db, siniestroId, "Asegurado",
                "Documento adjuntado: " + tipoDocumento.trim(), true)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

}
