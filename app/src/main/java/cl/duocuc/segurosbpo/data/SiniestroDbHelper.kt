package cl.duocuc.segurosbpo.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.AreasSiniestro
import cl.duocuc.segurosbpo.model.ControlPlazos
import cl.duocuc.segurosbpo.model.AvisoSiniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.Compania
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.DocumentosLiquidador
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.ReglasEtapa
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import cl.duocuc.segurosbpo.model.Consulta
import cl.duocuc.segurosbpo.model.EvaluacionCierre
import cl.duocuc.segurosbpo.model.ValidadorConsulta
import cl.duocuc.segurosbpo.model.ValidadorDesistimiento
import cl.duocuc.segurosbpo.model.ValidadorEvaluacion
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.ValidadorSiniestro
import cl.duocuc.segurosbpo.model.Siniestro
import cl.duocuc.segurosbpo.model.TipoSiniestro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SiniestroDbHelper(
    context: Context,
    databaseName: String = "seguros_bpo.db"
) : SQLiteOpenHelper(context, databaseName, null, 9) {

    private val TROZO = 512 * 1024

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
                ultima_actualizacion TEXT NOT NULL,
                archivado INTEGER NOT NULL DEFAULT 0,
                liquidador_id TEXT,
                liquidador_nombre TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_siniestros_asegurado ON siniestros(asegurado)")
        crearTablaEvidencias(db)
        crearTablaGestiones(db)
        crearTablaSolicitudes(db)
        crearTablaAvisos(db)
        crearTablaConsultas(db)
        crearTablaEvaluaciones(db)
        crearTablaCatalogos(db)
        sembrarCatalogos(db)
    }

    private fun crearTablaCatalogos(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS companias (
                codigo TEXT PRIMARY KEY NOT NULL,
                etiqueta TEXT NOT NULL,
                activa INTEGER NOT NULL DEFAULT 1,
                plazo_dias_habiles INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS tipos_siniestro (
                codigo TEXT PRIMARY KEY NOT NULL,
                etiqueta TEXT NOT NULL,
                es_vehiculo INTEGER NOT NULL,
                documentos TEXT NOT NULL
            )
        """.trimIndent())
    }

    private fun sembrarCatalogos(db: SQLiteDatabase) {
        Compania.semilla.forEach { compania ->
            db.insertWithOnConflict("companias", null, ContentValues().apply {
                put("codigo", compania.name)
                put("etiqueta", compania.etiqueta)
                put("activa", if (compania.activa) 1 else 0)
                put("plazo_dias_habiles", compania.plazoDiasHabiles)
            }, SQLiteDatabase.CONFLICT_IGNORE)
        }
        TipoSiniestro.semilla.forEach { tipo ->
            db.insertWithOnConflict("tipos_siniestro", null, ContentValues().apply {
                put("codigo", tipo.name)
                put("etiqueta", tipo.etiqueta)
                put("es_vehiculo", if (tipo.esVehiculo) 1 else 0)
                put("documentos", tipo.documentosHabituales.joinToString("|"))
            }, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) cargarCatalogos(db)
    }

    private fun cargarCatalogos(db: SQLiteDatabase) {
        runCatching {
            val companias = mutableListOf<Compania>()
            db.query("companias", null, null, null, null, null, "etiqueta ASC").use { c ->
                while (c.moveToNext()) {
                    companias.add(Compania(
                        c.getString(0), c.getString(1), c.getInt(2) == 1, c.getInt(3)
                    ))
                }
            }
            Compania.reemplazar(companias)
            val tipos = mutableListOf<TipoSiniestro>()
            db.query("tipos_siniestro", null, null, null, null, null, "etiqueta ASC").use { c ->
                while (c.moveToNext()) {
                    tipos.add(TipoSiniestro(
                        c.getString(0), c.getString(1), c.getInt(2) == 1,
                        c.getString(3).split("|").filter { it.isNotBlank() }
                    ))
                }
            }
            TipoSiniestro.reemplazar(tipos)
        }
    }

    fun listarCompanias(): List<Compania> = Compania.entries

    fun listarTipos(): List<TipoSiniestro> = TipoSiniestro.entries

    fun crearCompania(etiqueta: String, plazoDiasHabiles: Int) {
        val nombre = etiqueta.trim()
        require(nombre.length in 3..60) { "El nombre debe tener entre 3 y 60 caracteres" }
        require(plazoDiasHabiles in 1..60) { "El plazo debe estar entre 1 y 60 días hábiles" }
        val codigo = codigoDesde(nombre)
        val db = writableDatabase
        require(Compania.entries.none { it.name == codigo || it.etiqueta.equals(nombre, true) }) {
            "Ya existe una compañía con ese nombre"
        }
        db.insertOrThrow("companias", null, ContentValues().apply {
            put("codigo", codigo)
            put("etiqueta", nombre)
            put("activa", 1)
            put("plazo_dias_habiles", plazoDiasHabiles)
        })
        cargarCatalogos(db)
    }

    fun crearTipoSiniestro(etiqueta: String, esVehiculo: Boolean, documentos: List<String>) {
        val nombre = etiqueta.trim()
        require(nombre.length in 3..60) { "El nombre debe tener entre 3 y 60 caracteres" }
        val limpios = documentos.map { it.trim() }.filter { it.isNotBlank() }
        require(limpios.none { it.contains("|") }) { "Los documentos no pueden incluir el signo |" }
        val codigo = codigoDesde(nombre)
        val db = writableDatabase
        require(
            TipoSiniestro.entries.none { it.name == codigo || it.etiqueta.equals(nombre, true) }
        ) { "Ya existe un tipo de siniestro con ese nombre" }
        db.insertOrThrow("tipos_siniestro", null, ContentValues().apply {
            put("codigo", codigo)
            put("etiqueta", nombre)
            put("es_vehiculo", if (esVehiculo) 1 else 0)
            put("documentos", limpios.joinToString("|"))
        })
        cargarCatalogos(db)
    }

    private fun codigoDesde(nombre: String): String = nombre.uppercase()
        .replace("Á", "A").replace("É", "E").replace("Í", "I")
        .replace("Ó", "O").replace("Ú", "U").replace("Ñ", "N")
        .replace(Regex("[^A-Z0-9]+"), "_")
        .trim('_')
        .take(40)

    private fun crearTablaConsultas(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS consultas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                siniestro_id INTEGER NOT NULL,
                pregunta TEXT NOT NULL,
                respuesta TEXT,
                fecha_pregunta TEXT NOT NULL,
                fecha_respuesta TEXT,
                FOREIGN KEY (siniestro_id) REFERENCES siniestros (id)
            )
        """.trimIndent())
    }

    private fun crearTablaEvaluaciones(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS evaluaciones (
                siniestro_id INTEGER PRIMARY KEY NOT NULL,
                nota INTEGER NOT NULL,
                comentario TEXT,
                fecha TEXT NOT NULL,
                FOREIGN KEY (siniestro_id) REFERENCES siniestros (id)
            )
        """.trimIndent())
    }

    
    private fun borrarCasosDeEjemplo(db: SQLiteDatabase) {
        val filtro = """
            siniestro_id IN (
                SELECT siniestro_id FROM gestiones
                WHERE responsable IN ('Registro del caso', 'Ejemplo académico')
            )
        """.trimIndent()
        listOf("evidencias", "solicitudes_documentos", "avisos", "consultas", "evaluaciones")
            .forEach { tabla -> db.delete(tabla, filtro, null) }
        db.delete("siniestros", """
            id IN (
                SELECT siniestro_id FROM gestiones
                WHERE responsable IN ('Registro del caso', 'Ejemplo académico')
            )
        """.trimIndent(), null)
        db.delete("gestiones", filtro, null)
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

    private fun crearTablaSolicitudes(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS solicitudes_documentos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                siniestro_id INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                comentario TEXT NOT NULL,
                fecha_solicitud TEXT NOT NULL,
                fecha_entrega TEXT,
                evidencia_id INTEGER,
                FOREIGN KEY(siniestro_id) REFERENCES siniestros(id) ON DELETE CASCADE,
                FOREIGN KEY(evidencia_id) REFERENCES evidencias(id)
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_solicitudes_caso ON solicitudes_documentos(siniestro_id, fecha_entrega)")
    }

    private fun crearTablaAvisos(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS avisos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                siniestro_id INTEGER NOT NULL,
                mensaje TEXT NOT NULL,
                fecha TEXT NOT NULL,
                leido INTEGER NOT NULL DEFAULT 0,
                solicitud_id INTEGER,
                FOREIGN KEY(siniestro_id) REFERENCES siniestros(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_avisos_caso ON avisos(siniestro_id, id)")
    }

    private fun insertarAviso(
        db: SQLiteDatabase, siniestroId: Long, mensaje: String, solicitudId: Long? = null
    ) {
        db.insertOrThrow("avisos", null, ContentValues().apply {
            put("siniestro_id", siniestroId)
            put("mensaje", mensaje)
            put("fecha", fechaActual())
            put("leido", 0)
            if (solicitudId == null) putNull("solicitud_id") else put("solicitud_id", solicitudId)
        })
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
        if (oldVersion < 5) {
            crearTablaSolicitudes(db)
            crearTablaAvisos(db)
        }
        if (oldVersion < 6) {
            crearTablaConsultas(db)
            crearTablaEvaluaciones(db)
        }
        if (oldVersion < 7) {
            db.execSQL(
                "ALTER TABLE siniestros ADD COLUMN archivado INTEGER NOT NULL DEFAULT 0"
            )
            borrarCasosDeEjemplo(db)
        }
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE avisos ADD COLUMN solicitud_id INTEGER")
        }
        if (oldVersion < 9) {
            crearTablaCatalogos(db)
            sembrarCatalogos(db)
            db.execSQL("ALTER TABLE siniestros ADD COLUMN liquidador_id TEXT")
            db.execSQL("ALTER TABLE siniestros ADD COLUMN liquidador_nombre TEXT")
        }
    }

    private fun fechaActual(): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())

    
    fun buscarPosibleDuplicado(formulario: FormularioUiState): String? =
        readableDatabase.query(
            "siniestros", arrayOf("numero"),
            "asegurado = ? AND compania = ? AND tipo = ? AND fecha_ocurrencia = ? AND etapa <> ?",
            arrayOf(
                formulario.asegurado.name, formulario.compania.name, formulario.tipoSiniestro.name,
                formulario.fechaOcurrencia, EtapaSiniestro.CERRADO.name
            ),
            null, null, "id DESC", "1"
        ).use { if (it.moveToFirst()) it.getString(0) else null }

    fun registrar(formulario: FormularioUiState): Siniestro {
        val error = listOfNotNull(
            ValidadorSiniestro.errorFecha(formulario.fechaOcurrencia),
            ValidadorSiniestro.errorComuna(formulario.comuna),
            ValidadorSiniestro.errorDescripcion(formulario.descripcion),
            ValidadorSiniestro.errorCompania(formulario.compania),
            if (!formulario.datosCorrectos) "Debes confirmar que los datos son correctos" else null
        ).firstOrNull()
        error?.let { throw IllegalArgumentException(it) }
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
            opcional("resultado_cierre"), opcional("mensaje_cierre"),
            opcional("liquidador_id"), opcional("liquidador_nombre")
        )
    }

    fun listarDisponibles(liquidador: Liquidador): List<Siniestro> {
        val resultado = mutableListOf<Siniestro>()
        val tipos = AreasSiniestro.paraLiquidador(liquidador).tipos
        val marcadores = tipos.joinToString(",") { "?" }
        readableDatabase.query(
            "siniestros", null,
            "liquidador IS NULL AND etapa = ? AND tipo IN ($marcadores)",
            arrayOf(EtapaSiniestro.RECIBIDO.name, *tipos.map { it.name }.toTypedArray()),
            null, null, "id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) resultado.add(convertir(cursor))
        }
        return resultado
    }

    fun tomarCaso(
        siniestroId: Long,
        liquidador: Liquidador,
        personaId: String,
        personaNombre: String
    ): Boolean {
        val tipos = AreasSiniestro.paraLiquidador(liquidador).tipos
        val db = writableDatabase
        db.beginTransaction()
        try {
            val marcadores = tipos.joinToString(",") { "?" }
            val filas = db.update(
                "siniestros",
                ContentValues().apply {
                    put("liquidador", liquidador.name)
                    put("liquidador_id", personaId)
                    put("liquidador_nombre", personaNombre)
                    put("etapa", EtapaSiniestro.EVALUACION.name)
                    put("ultima_actualizacion", fechaActual())
                },
                "id = ? AND liquidador IS NULL AND etapa = ? AND tipo IN ($marcadores)",
                arrayOf(
                    siniestroId.toString(),
                    EtapaSiniestro.RECIBIDO.name,
                    *tipos.map { it.name }.toTypedArray()
                )
            )
            if (filas == 1) {
                insertarGestion(
                    db, siniestroId, personaNombre,
                    "Caso tomado por " + personaNombre + ", del equipo " + liquidador.equipo, true
                )
                insertarAviso(db, siniestroId, "Tu caso está en evaluación")
            }
            db.setTransactionSuccessful()
            return filas == 1
        } finally {
            db.endTransaction()
        }
    }

    fun listarSeguimientosVencidos(
        liquidador: Liquidador,
        fechaConsulta: Date = Date()
    ): Set<Long> = listarAsignados(liquidador)
        .asSequence()
        .filter { it.etapa != EtapaSiniestro.CERRADO }
        .filter { caso ->
            val ultimaVisible = listarGestionesAsignadas(caso.id, liquidador)
                .lastOrNull { it.visibleAsegurado }?.fecha
                ?: caso.fechaReporte
            ControlPlazos.diasCorridos(ultimaVisible, fechaConsulta) >= 7
        }
        .map { it.id }
        .toSet()

    fun listarAsignados(liquidador: Liquidador, personaId: String? = null): List<Siniestro> =
        asignadosPor(liquidador, archivado = 0, personaId = personaId)

    fun listarArchivados(liquidador: Liquidador, personaId: String? = null): List<Siniestro> =
        asignadosPor(liquidador, archivado = 1, personaId = personaId)

    private fun asignadosPor(
        liquidador: Liquidador, archivado: Int, personaId: String?
    ): List<Siniestro> {
        val casos = mutableListOf<Siniestro>()
        val filtro = StringBuilder("liquidador = ? AND archivado = ?")
        val argumentos = mutableListOf(liquidador.name, archivado.toString())
        if (personaId != null) {
            filtro.append(" AND liquidador_id = ?")
            argumentos.add(personaId)
        }
        readableDatabase.query(
            "siniestros", null, filtro.toString(), argumentos.toTypedArray(),
            null, null, "id DESC"
        ).use { c -> while (c.moveToNext()) casos.add(convertir(c)) }
        return casos
    }

    fun archivarCaso(
        siniestroId: Long, liquidador: Liquidador, responsable: String = liquidador.equipo
    ) {
        val caso = obtenerAsignado(siniestroId, liquidador)
            ?: throw IllegalArgumentException("El caso no está asignado a tu equipo")
        require(caso.etapa == EtapaSiniestro.CERRADO) {
            "Solo puedes archivar casos cerrados"
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.update("siniestros", ContentValues().apply { put("archivado", 1) },
                "id = ?", arrayOf(siniestroId.toString()))
            insertarGestion(
                db, siniestroId, responsable,
                "El caso se archivó en la bandeja del equipo.", false
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun restaurarCaso(siniestroId: Long, liquidador: Liquidador) {
        val db = writableDatabase
        val filas = db.update(
            "siniestros", ContentValues().apply { put("archivado", 0) },
            "id = ? AND liquidador = ?", arrayOf(siniestroId.toString(), liquidador.name)
        )
        require(filas == 1) { "El caso no está asignado a tu equipo" }
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
            null, null, "CASE WHEN etapa = 'CERRADO' THEN 1 ELSE 0 END, id DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                resultado.add(convertir(cursor).copy(asegurado = asegurado))
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

    
    fun obtenerImagen(
        evidenciaId: Long, siniestroId: Long, asegurado: Asegurado, maxLado: Int = 900
    ): Bitmap? {
        val autorizado = readableDatabase.rawQuery("""
            SELECT 1 FROM evidencias e
            JOIN siniestros s ON s.id = e.siniestro_id
            WHERE e.id = ? AND s.id = ? AND s.asegurado = ? AND e.mime LIKE 'image/%'
        """.trimIndent(), arrayOf(evidenciaId.toString(), siniestroId.toString(), asegurado.name))
            .use { it.moveToFirst() }
        if (!autorizado) return null
        val contenido = leerContenido(evidenciaId) ?: return null
        return decodificarImagen(contenido, maxLado)
    }

    
    fun listarGestiones(siniestroId: Long, asegurado: Asegurado): List<Gestion> {
        val resultado = mutableListOf<Gestion>()
        readableDatabase.rawQuery("""
            SELECT g.id, g.siniestro_id, g.fecha, g.responsable,
                   g.descripcion
            FROM gestiones g JOIN siniestros s ON s.id = g.siniestro_id
            WHERE s.id = ? AND s.asegurado = ? AND g.visible_asegurado = 1
            ORDER BY g.id ASC
        """.trimIndent(), arrayOf(siniestroId.toString(), asegurado.name)).use { c ->
            while (c.moveToNext()) {
                resultado.add(Gestion(c.getLong(0), c.getLong(1),
                    c.getString(2), c.getString(3), c.getString(4),
                    true))
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
            ORDER BY g.id ASC
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
        descripcion: String, visible: Boolean,
        responsable: String = liquidador.equipo
    ) {
        ReglasEtapa.validarGestion(descripcion)?.let { throw IllegalArgumentException(it) }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val caso = db.rawQuery(
                "SELECT etapa FROM siniestros WHERE id = ? AND liquidador = ?",
                arrayOf(siniestroId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
            require(caso != null) { "El caso no está asignado a este liquidador" }
            require(caso != EtapaSiniestro.CERRADO.name) { "No se modifica un caso cerrado" }
            insertarGestion(db, siniestroId, responsable, descripcion.trim(), visible)
            val valores = ContentValues().apply { put("ultima_actualizacion", fechaActual()) }
            db.update("siniestros", valores, "id = ? AND liquidador = ?",
                arrayOf(siniestroId.toString(), liquidador.name))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun avanzarAsignado(siniestroId: Long, liquidador: Liquidador,
        resultado: String, mensajeCierre: String,
        destino: EtapaSiniestro? = null,
        responsable: String = liquidador.equipo
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val actual = obtenerAsignado(siniestroId, liquidador)
                ?: throw IllegalArgumentException("El caso no está asignado a este liquidador")


            val siguiente = destino ?: ReglasEtapa.siguiente(actual.etapa)
                ?: throw IllegalArgumentException("Un siniestro cerrado no puede reabrirse")
            val pendientes = db.rawQuery(
                "SELECT COUNT(*) FROM solicitudes_documentos WHERE siniestro_id = ? AND fecha_entrega IS NULL",
                arrayOf(siniestroId.toString())
            ).use { c -> c.moveToFirst(); c.getInt(0) }
            ReglasEtapa.validar(actual.etapa, siguiente, pendientes, resultado, mensajeCierre)
                ?.let { throw IllegalArgumentException(it) }
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
                else "Etapa actualizada a " + siguiente.paso
            insertarGestion(db, siniestroId, responsable, nota, true)
            insertarAviso(db, siniestroId, nota)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    
    fun listarSolicitudes(siniestroId: Long, asegurado: Asegurado): List<SolicitudDocumento> {
        val resultado = mutableListOf<SolicitudDocumento>()
        readableDatabase.rawQuery("""
            SELECT d.id, d.siniestro_id, d.tipo, d.comentario, d.fecha_solicitud, d.fecha_entrega
            FROM solicitudes_documentos d
            JOIN siniestros s ON s.id = d.siniestro_id
            WHERE s.id = ? AND s.asegurado = ?
            ORDER BY d.id DESC
        """.trimIndent(), arrayOf(siniestroId.toString(), asegurado.name)).use { c ->
            while (c.moveToNext()) {
                resultado.add(SolicitudDocumento(
                    c.getLong(0), c.getLong(1), c.getString(2),
                    c.getString(3), c.getString(4),
                    if (c.isNull(5)) null else c.getString(5)
                ))
            }
        }
        return resultado
    }

    fun listarSolicitudesAsignadas(siniestroId: Long, liquidador: Liquidador): List<SolicitudDocumento> {
        val resultado = mutableListOf<SolicitudDocumento>()
        readableDatabase.rawQuery("""
            SELECT d.id, d.siniestro_id, d.tipo, d.comentario, d.fecha_solicitud, d.fecha_entrega
            FROM solicitudes_documentos d
            JOIN siniestros s ON s.id = d.siniestro_id
            WHERE s.id = ? AND s.liquidador = ?
            ORDER BY d.id DESC
        """.trimIndent(), arrayOf(siniestroId.toString(), liquidador.name)).use { c ->
            while (c.moveToNext()) {
                resultado.add(SolicitudDocumento(
                    c.getLong(0), c.getLong(1), c.getString(2),
                    c.getString(3), c.getString(4),
                    if (c.isNull(5)) null else c.getString(5)
                ))
            }
        }
        return resultado
    }

    fun solicitarDocumento(
        siniestroId: Long, liquidador: Liquidador, tipo: String, comentario: String,
        responsable: String = liquidador.equipo
    ) {
        val nombre = tipo.trim()
        require(nombre.isNotEmpty() && nombre.length <= 100) { "Indica un documento de hasta 100 caracteres" }
        require(comentario.length <= 500) { "El comentario no puede superar 500 caracteres" }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val caso = obtenerAsignado(siniestroId, liquidador)
                ?: throw IllegalArgumentException("El caso no está asignado a este liquidador")
            require(caso.etapa != EtapaSiniestro.CERRADO) { "El caso está cerrado" }
            val duplicados = db.rawQuery("""
                SELECT COUNT(*) FROM solicitudes_documentos
                WHERE siniestro_id = ? AND tipo = ? COLLATE NOCASE AND fecha_entrega IS NULL
            """.trimIndent(), arrayOf(siniestroId.toString(), nombre)).use { c ->
                c.moveToFirst()
                c.getInt(0)
            }
            require(duplicados == 0) { "Ese documento ya está pendiente" }
            val solicitudId = db.insertOrThrow("solicitudes_documentos", null, ContentValues().apply {
                put("siniestro_id", siniestroId)
                put("tipo", nombre)
                put("comentario", comentario.trim())
                put("fecha_solicitud", fechaActual())
            })
            val nota = "Documento solicitado: " + nombre
            insertarGestion(db, siniestroId, responsable, nota, true)
            insertarAviso(db, siniestroId, "Necesitamos el documento: " + nombre, solicitudId)
            db.update("siniestros", ContentValues().apply {
                put("ultima_actualizacion", fechaActual())
            }, "id = ?", arrayOf(siniestroId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun marcarSolicitudRecibida(
        solicitudId: Long, liquidador: Liquidador, responsable: String = liquidador.equipo
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val datos = db.rawQuery("""
                SELECT d.siniestro_id, d.tipo FROM solicitudes_documentos d
                JOIN siniestros s ON s.id = d.siniestro_id
                WHERE d.id = ? AND s.liquidador = ? AND d.fecha_entrega IS NULL
            """.trimIndent(),
                arrayOf(solicitudId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getLong(0) to c.getString(1) else null }
            require(datos != null) { "La solicitud no está pendiente en un caso de tu equipo" }
            val ahora = fechaActual()
            db.update("solicitudes_documentos", ContentValues().apply {
                put("fecha_entrega", ahora)
            }, "id = ?", arrayOf(solicitudId.toString()))
            insertarGestion(
                db, datos.first, responsable,
                "Se dio por recibido el documento: " + datos.second, true
            )
            insertarAviso(
                db, datos.first,
                "El liquidador confirmó la recepción de: " + datos.second
            )
            db.update("siniestros", ContentValues().apply {
                put("ultima_actualizacion", ahora)
            }, "id = ?", arrayOf(datos.first.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun anularSolicitud(
        solicitudId: Long, liquidador: Liquidador, responsable: String = liquidador.equipo
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val datos = db.rawQuery("""
                SELECT d.siniestro_id, d.tipo FROM solicitudes_documentos d
                JOIN siniestros s ON s.id = d.siniestro_id
                WHERE d.id = ? AND s.liquidador = ? AND d.fecha_entrega IS NULL
            """.trimIndent(),
                arrayOf(solicitudId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getLong(0) to c.getString(1) else null }
            require(datos != null) { "La solicitud no está pendiente en un caso de tu equipo" }
            db.delete("solicitudes_documentos", "id = ?", arrayOf(solicitudId.toString()))
            insertarGestion(
                db, datos.first, responsable,
                "Ya no se requiere el documento: " + datos.second, true
            )
            insertarAviso(
                db, datos.first,
                "Ya no necesitamos el documento: " + datos.second
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun generarRecordatorios(fechaConsulta: Date = Date()): Int {
        val db = writableDatabase
        var generados = 0
        db.beginTransaction()
        try {
            data class Pendiente(val id: Long, val siniestroId: Long, val tipo: String, val fecha: String)
            val pendientes = mutableListOf<Pendiente>()
            db.rawQuery("""
                SELECT d.id, d.siniestro_id, d.tipo, d.fecha_solicitud
                FROM solicitudes_documentos d
                JOIN siniestros s ON s.id = d.siniestro_id
                WHERE d.fecha_entrega IS NULL AND s.etapa <> ?
            """.trimIndent(), arrayOf(EtapaSiniestro.CERRADO.name)).use { c ->
                while (c.moveToNext()) {
                    pendientes.add(Pendiente(c.getLong(0), c.getLong(1), c.getString(2), c.getString(3)))
                }
            }
            pendientes.forEach { pendiente ->
                if (ControlPlazos.diasCorridos(pendiente.fecha, fechaConsulta) >= 15) {
                    val aviso = "Recordatorio #${pendiente.id}: entrega pendiente de ${pendiente.tipo}"
                    val existe = db.rawQuery(
                        "SELECT COUNT(*) FROM avisos WHERE siniestro_id = ? AND mensaje = ?",
                        arrayOf(pendiente.siniestroId.toString(), aviso)
                    ).use { c -> c.moveToFirst(); c.getInt(0) > 0 }
                    if (!existe) {
                        insertarAviso(db, pendiente.siniestroId, aviso, pendiente.id)
                        insertarGestion(db, pendiente.siniestroId, "Sistema", aviso, true)
                        generados++
                    }
                }
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        return generados
    }

    fun listarAvisos(asegurado: Asegurado): List<AvisoSiniestro> {
        val resultado = mutableListOf<AvisoSiniestro>()
        readableDatabase.rawQuery("""
            SELECT a.id, a.siniestro_id, s.numero, a.mensaje, a.fecha, a.leido,
                   a.solicitud_id, d.tipo, d.fecha_entrega
            FROM avisos a JOIN siniestros s ON s.id = a.siniestro_id
            LEFT JOIN solicitudes_documentos d ON d.id = a.solicitud_id
            WHERE s.asegurado = ?
            ORDER BY a.id DESC
        """.trimIndent(), arrayOf(asegurado.name)).use { c ->
            while (c.moveToNext()) {
                val pendiente = !c.isNull(6) && !c.isNull(7) && c.isNull(8)
                resultado.add(AvisoSiniestro(
                    c.getLong(0), c.getLong(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getInt(5) == 1,
                    if (pendiente) c.getLong(6) else null,
                    if (pendiente) c.getString(7) else null
                ))
            }
        }
        return resultado
    }

    fun marcarAvisoLeido(id: Long, asegurado: Asegurado): Boolean {
        val cambios = ContentValues().apply { put("leido", 1) }
        return writableDatabase.update(
            "avisos",
            cambios,
            "id = ? AND siniestro_id IN (SELECT id FROM siniestros WHERE asegurado = ?)",
            arrayOf(id.toString(), asegurado.name)
        ) == 1
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

    
    fun desistir(siniestroId: Long, asegurado: Asegurado, motivo: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val etapaActual = db.rawQuery(
                "SELECT etapa FROM siniestros WHERE id = ? AND asegurado = ?",
                arrayOf(siniestroId.toString(), asegurado.name)
            ).use { c -> if (c.moveToFirst()) EtapaSiniestro.valueOf(c.getString(0)) else null }
            require(etapaActual != null) { "No existe ese siniestro del asegurado" }
            ValidadorDesistimiento.error(etapaActual, motivo)
                ?.let { throw IllegalArgumentException(it) }
            val ahora = fechaActual()
            db.update("siniestros", ContentValues().apply {
                put("etapa", EtapaSiniestro.CERRADO.name)
                put("resultado_cierre", "Desistido")
                put("mensaje_cierre", motivo.trim())
                put("ultima_actualizacion", ahora)
            }, "id = ?", arrayOf(siniestroId.toString()))
            insertarGestion(
                db, siniestroId, asegurado.etiqueta,
                "La persona asegurada desistió del siniestro. Motivo: " + motivo.trim(), true
            )
            insertarAviso(db, siniestroId, "Tu siniestro quedó cerrado por desistimiento.")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listarConsultas(siniestroId: Long, asegurado: Asegurado): List<Consulta> =
        consultasPor(
            "WHERE s.id = ? AND s.asegurado = ?",
            arrayOf(siniestroId.toString(), asegurado.name)
        )

    fun listarConsultasAsignadas(siniestroId: Long, liquidador: Liquidador): List<Consulta> =
        consultasPor(
            "WHERE s.id = ? AND s.liquidador = ?",
            arrayOf(siniestroId.toString(), liquidador.name)
        )

    private fun consultasPor(filtro: String, argumentos: Array<String>): List<Consulta> {
        val resultado = mutableListOf<Consulta>()
        readableDatabase.rawQuery("""
            SELECT c.id, c.siniestro_id, c.pregunta, c.respuesta,
                   c.fecha_pregunta, c.fecha_respuesta
            FROM consultas c JOIN siniestros s ON s.id = c.siniestro_id
            $filtro
            ORDER BY c.id DESC
        """.trimIndent(), argumentos).use { c ->
            while (c.moveToNext()) {
                resultado.add(Consulta(
                    c.getLong(0), c.getLong(1), c.getString(2),
                    if (c.isNull(3)) null else c.getString(3),
                    c.getString(4),
                    if (c.isNull(5)) null else c.getString(5)
                ))
            }
        }
        return resultado
    }

    fun registrarConsulta(siniestroId: Long, asegurado: Asegurado, pregunta: String) {
        ValidadorConsulta.errorPregunta(pregunta)?.let { throw IllegalArgumentException(it) }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val existe = db.rawQuery(
                "SELECT 1 FROM siniestros WHERE id = ? AND asegurado = ?",
                arrayOf(siniestroId.toString(), asegurado.name)
            ).use { it.moveToFirst() }
            require(existe) { "No existe ese siniestro del asegurado" }
            db.insertOrThrow("consultas", null, ContentValues().apply {
                put("siniestro_id", siniestroId)
                put("pregunta", pregunta.trim())
                put("fecha_pregunta", fechaActual())
            })
            insertarGestion(
                db, siniestroId, asegurado.etiqueta,
                "La persona asegurada envió una consulta sobre su caso.", true
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun responderConsulta(
        consultaId: Long, liquidador: Liquidador, respuesta: String,
        responsable: String = liquidador.equipo
    ) {
        ValidadorConsulta.errorRespuesta(respuesta)?.let { throw IllegalArgumentException(it) }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val siniestroId = db.rawQuery("""
                SELECT c.siniestro_id FROM consultas c
                JOIN siniestros s ON s.id = c.siniestro_id
                WHERE c.id = ? AND s.liquidador = ?
            """.trimIndent(),
                arrayOf(consultaId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getLong(0) else null }
            require(siniestroId != null) { "La consulta no pertenece a un caso de tu equipo" }
            db.update("consultas", ContentValues().apply {
                put("respuesta", respuesta.trim())
                put("fecha_respuesta", fechaActual())
            }, "id = ?", arrayOf(consultaId.toString()))
            insertarGestion(
                db, siniestroId, responsable,
                "Se respondió la consulta de la persona asegurada.", true
            )
            insertarAviso(db, siniestroId, "Respondimos tu consulta. Revisa el detalle del caso.")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun obtenerEvaluacion(siniestroId: Long, asegurado: Asegurado): EvaluacionCierre? =
        readableDatabase.rawQuery("""
            SELECT e.siniestro_id, e.nota, e.comentario, e.fecha
            FROM evaluaciones e JOIN siniestros s ON s.id = e.siniestro_id
            WHERE s.id = ? AND s.asegurado = ?
        """.trimIndent(), arrayOf(siniestroId.toString(), asegurado.name)).use { c ->
            if (!c.moveToFirst()) return@use null
            EvaluacionCierre(
                c.getLong(0), c.getInt(1),
                if (c.isNull(2)) null else c.getString(2), c.getString(3)
            )
        }

    fun guardarEvaluacion(
        siniestroId: Long, asegurado: Asegurado, nota: Int, comentario: String
    ) {
        ValidadorEvaluacion.error(nota, comentario)?.let { throw IllegalArgumentException(it) }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val etapaActual = db.rawQuery(
                "SELECT etapa FROM siniestros WHERE id = ? AND asegurado = ?",
                arrayOf(siniestroId.toString(), asegurado.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
            require(etapaActual != null) { "No existe ese siniestro del asegurado" }
            require(etapaActual == EtapaSiniestro.CERRADO.name) {
                "Solo puedes evaluar un caso cerrado"
            }
            db.insertWithOnConflict("evaluaciones", null, ContentValues().apply {
                put("siniestro_id", siniestroId)
                put("nota", nota)
                put("comentario", comentario.trim())
                put("fecha", fechaActual())
            }, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listarSinAsignar(): List<Siniestro> {
        val resultado = mutableListOf<Siniestro>()
        readableDatabase.query(
            "siniestros", null,
            "liquidador IS NULL AND etapa = ?",
            arrayOf(EtapaSiniestro.RECIBIDO.name),
            null, null, "id ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) resultado.add(convertir(cursor))
        }
        return resultado
    }

    fun cargaPorPersona(): Map<String, Int> {
        val carga = mutableMapOf<String, Int>()
        readableDatabase.rawQuery("""
            SELECT liquidador_id, COUNT(*) FROM siniestros
            WHERE liquidador_id IS NOT NULL AND etapa <> ? AND archivado = 0
            GROUP BY liquidador_id
        """.trimIndent(), arrayOf(EtapaSiniestro.CERRADO.name)).use { c ->
            while (c.moveToNext()) carga[c.getString(0)] = c.getInt(1)
        }
        return carga
    }

    fun cargaPorLiquidador(): Map<Liquidador, Int> {
        val carga = Liquidador.entries.associateWith { 0 }.toMutableMap()
        readableDatabase.rawQuery("""
            SELECT liquidador, COUNT(*) FROM siniestros
            WHERE liquidador IS NOT NULL AND etapa <> ?
            GROUP BY liquidador
        """.trimIndent(), arrayOf(EtapaSiniestro.CERRADO.name)).use { c ->
            while (c.moveToNext()) {
                runCatching { Liquidador.valueOf(c.getString(0)) }.getOrNull()?.let { equipo ->
                    carga[equipo] = c.getInt(1)
                }
            }
        }
        return carga
    }

    fun asignarLiquidador(
        siniestroId: Long,
        liquidador: Liquidador,
        personaId: String,
        personaNombre: String
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val caso = db.rawQuery(
                "SELECT tipo, etapa FROM siniestros WHERE id = ?",
                arrayOf(siniestroId.toString())
            ).use { c ->
                if (c.moveToFirst()) TipoSiniestro.valueOf(c.getString(0)) to c.getString(1)
                else null
            }
            require(caso != null) { "No existe ese siniestro" }
            require(caso.second != EtapaSiniestro.CERRADO.name) {
                "No se puede reasignar un caso cerrado"
            }
            require(caso.first.esVehiculo == liquidador.esVehiculo) {
                "Un siniestro de " + (if (caso.first.esVehiculo) "vehículo" else "hogar") +
                    " solo puede asignarse al equipo correspondiente"
            }
            val ahora = fechaActual()
            val nuevaEtapa = if (caso.second == EtapaSiniestro.RECIBIDO.name)
                EtapaSiniestro.EVALUACION.name else caso.second
            db.update("siniestros", ContentValues().apply {
                put("liquidador", liquidador.name)
                put("liquidador_id", personaId)
                put("liquidador_nombre", personaNombre)
                put("etapa", nuevaEtapa)
                put("ultima_actualizacion", ahora)
            }, "id = ?", arrayOf(siniestroId.toString()))
            insertarGestion(
                db, siniestroId, "Analista de administración",
                "Caso asignado a " + personaNombre + ", del equipo " + liquidador.equipo, true
            )
            insertarAviso(
                db, siniestroId,
                "Tu caso quedó a cargo de " + personaNombre + "."
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listarEvidenciasAsignadas(siniestroId: Long, liquidador: Liquidador): List<Evidencia> {
        val resultado = mutableListOf<Evidencia>()
        readableDatabase.rawQuery("""
            SELECT e.id, e.siniestro_id, e.tipo_documento, e.nombre_archivo,
                   e.mime, e.tamano_bytes, e.fecha_registro
            FROM evidencias e JOIN siniestros s ON s.id = e.siniestro_id
            WHERE s.id = ? AND s.liquidador = ?
            ORDER BY e.id DESC
        """.trimIndent(), arrayOf(siniestroId.toString(), liquidador.name)).use { cursor ->
            while (cursor.moveToNext()) {
                resultado.add(Evidencia(
                    cursor.getLong(0), cursor.getLong(1), cursor.getString(2),
                    cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getString(6)
                ))
            }
        }
        return resultado
    }

    fun obtenerImagenAsignada(
        evidenciaId: Long, siniestroId: Long, liquidador: Liquidador, maxLado: Int = 900
    ): Bitmap? {
        val mime = readableDatabase.rawQuery("""
            SELECT e.mime
            FROM evidencias e JOIN siniestros s ON s.id = e.siniestro_id
            WHERE e.id = ? AND s.id = ? AND s.liquidador = ?
        """.trimIndent(),
            arrayOf(evidenciaId.toString(), siniestroId.toString(), liquidador.name)
        ).use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: return null
        if (!mime.startsWith("image/")) return null
        val contenido = leerContenido(evidenciaId) ?: return null
        return decodificarImagen(contenido, maxLado)
    }

    fun obtenerArchivoAsignado(
        evidenciaId: Long, siniestroId: Long, liquidador: Liquidador
    ): ArchivoEvidencia? {
        val datos = readableDatabase.rawQuery("""
            SELECT e.nombre_archivo, e.mime
            FROM evidencias e JOIN siniestros s ON s.id = e.siniestro_id
            WHERE e.id = ? AND s.id = ? AND s.liquidador = ?
        """.trimIndent(),
            arrayOf(evidenciaId.toString(), siniestroId.toString(), liquidador.name)
        ).use { c -> if (c.moveToFirst()) c.getString(0) to c.getString(1) else null }
            ?: return null
        val contenido = leerContenido(evidenciaId) ?: return null
        return ArchivoEvidencia(datos.first, datos.second, contenido)
    }

    private fun leerContenido(evidenciaId: Long): ByteArray? {
        val db = readableDatabase
        val total = db.rawQuery(
            "SELECT length(contenido) FROM evidencias WHERE id = ?",
            arrayOf(evidenciaId.toString())
        ).use { c -> if (c.moveToFirst()) c.getLong(0) else null } ?: return null
        if (total <= 0L) return null
        val salida = java.io.ByteArrayOutputStream(total.toInt())
        var desde = 1L
        while (desde <= total) {
            val trozo = db.rawQuery(
                "SELECT substr(contenido, ?, ?) FROM evidencias WHERE id = ?",
                arrayOf(desde.toString(), TROZO.toString(), evidenciaId.toString())
            ).use { c -> if (c.moveToFirst()) c.getBlob(0) else null } ?: return null
            if (trozo.isEmpty()) break
            salida.write(trozo)
            desde += trozo.size
        }
        return salida.toByteArray().takeIf { it.isNotEmpty() }
    }

    private fun decodificarImagen(contenido: ByteArray, maxLado: Int): Bitmap? {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(contenido, 0, contenido.size, medidas)
        var escala = 1
        while (medidas.outWidth / escala > maxLado || medidas.outHeight / escala > maxLado) {
            escala *= 2
        }
        val reducir = BitmapFactory.Options().apply {
            inSampleSize = escala
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeByteArray(contenido, 0, contenido.size, reducir)
    }

    fun guardarEvidenciaLiquidador(
        siniestroId: Long, liquidador: Liquidador, tipoDocumento: String,
        archivo: ArchivoEvidencia
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val caso = db.rawQuery(
                "SELECT etapa FROM siniestros WHERE id = ? AND liquidador = ?",
                arrayOf(siniestroId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) else null }
            require(caso != null) { "El caso no está asignado a tu equipo" }
            require(caso != EtapaSiniestro.CERRADO.name) {
                "No puedes adjuntar documentos a un caso cerrado"
            }
            val cantidad = db.rawQuery(
                "SELECT COUNT(*) FROM evidencias WHERE siniestro_id = ?",
                arrayOf(siniestroId.toString())
            ).use { c -> c.moveToFirst(); c.getInt(0) }
            ValidadorEvidencia.error(tipoDocumento, archivo.mime, archivo.bytes.size, cantidad)
                ?.let { throw IllegalArgumentException(it) }
            db.insertOrThrow("evidencias", null, ContentValues().apply {
                put("siniestro_id", siniestroId)
                put("tipo_documento", tipoDocumento.trim())
                put("nombre_archivo", archivo.nombre)
                put("mime", archivo.mime)
                put("tamano_bytes", archivo.bytes.size)
                put("contenido", archivo.bytes)
                put("fecha_registro", fechaActual())
            })
            insertarGestion(
                db, siniestroId, "Documento del liquidador",
                "Se adjuntó " + tipoDocumento.trim(), true
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
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
            ValidadorEvidencia.error(tipoDocumento, archivo.mime, archivo.bytes.size, cantidad)
                ?.let { throw IllegalArgumentException(it) }
            val valores = ContentValues().apply {
                put("siniestro_id", siniestroId)
                put("tipo_documento", tipoDocumento.trim())
                put("nombre_archivo", archivo.nombre)
                put("mime", archivo.mime)
                put("tamano_bytes", archivo.bytes.size)
                put("contenido", archivo.bytes)
                put("fecha_registro", fechaActual())
            }
            val evidenciaId = db.insertOrThrow("evidencias", null, valores)
            val solicitudId = db.rawQuery("""
                SELECT id FROM solicitudes_documentos
                WHERE siniestro_id = ? AND tipo = ? COLLATE NOCASE AND fecha_entrega IS NULL
                ORDER BY id LIMIT 1
            """.trimIndent(), arrayOf(siniestroId.toString(), tipoDocumento.trim())).use { c ->
                if (c.moveToFirst()) c.getLong(0) else null
            }
            if (solicitudId != null) {
                db.update("solicitudes_documentos", ContentValues().apply {
                    put("fecha_entrega", fechaActual())
                    put("evidencia_id", evidenciaId)
                }, "id = ?", arrayOf(solicitudId.toString()))
                insertarGestion(db, siniestroId, "Asegurado",
                    "Documento solicitado entregado: " + tipoDocumento.trim(), true)
            } else {
                insertarGestion(db, siniestroId, "Asegurado",
                    "Documento adjuntado: " + tipoDocumento.trim(), true)
            }
            db.update("siniestros", ContentValues().apply {
                put("ultima_actualizacion", fechaActual())
            }, "id = ?", arrayOf(siniestroId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun eliminarEvidencia(evidenciaId: Long, siniestroId: Long, asegurado: Asegurado) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val datos = db.rawQuery("""
                SELECT e.tipo_documento, s.etapa FROM evidencias e
                JOIN siniestros s ON s.id = e.siniestro_id
                WHERE e.id = ? AND s.id = ? AND s.asegurado = ?
            """.trimIndent(),
                arrayOf(evidenciaId.toString(), siniestroId.toString(), asegurado.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) to c.getString(1) else null }
            require(datos != null) { "El documento no pertenece a este siniestro" }
            require(datos.second != EtapaSiniestro.CERRADO.name) {
                "No puedes eliminar documentos de un caso cerrado"
            }
            require(datos.first !in DocumentosLiquidador.tipos) {
                "Ese documento lo cargó el equipo liquidador y no puedes eliminarlo"
            }
            val ahora = fechaActual()
            db.update("solicitudes_documentos", ContentValues().apply {
                putNull("fecha_entrega")
                putNull("evidencia_id")
            }, "evidencia_id = ?", arrayOf(evidenciaId.toString()))
            db.delete("evidencias", "id = ?", arrayOf(evidenciaId.toString()))
            insertarGestion(
                db, siniestroId, asegurado.etiqueta,
                "Se eliminó el documento adjuntado: " + datos.first, true
            )
            db.update("siniestros", ContentValues().apply {
                put("ultima_actualizacion", ahora)
            }, "id = ?", arrayOf(siniestroId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun eliminarEvidenciaLiquidador(
        evidenciaId: Long, siniestroId: Long, liquidador: Liquidador,
        responsable: String = liquidador.equipo
    ) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val datos = db.rawQuery("""
                SELECT e.tipo_documento, s.etapa FROM evidencias e
                JOIN siniestros s ON s.id = e.siniestro_id
                WHERE e.id = ? AND s.id = ? AND s.liquidador = ?
            """.trimIndent(),
                arrayOf(evidenciaId.toString(), siniestroId.toString(), liquidador.name)
            ).use { c -> if (c.moveToFirst()) c.getString(0) to c.getString(1) else null }
            require(datos != null) { "El documento no pertenece a un caso de tu equipo" }
            require(datos.second != EtapaSiniestro.CERRADO.name) {
                "No puedes eliminar documentos de un caso cerrado"
            }
            require(datos.first in DocumentosLiquidador.tipos) {
                "Solo puedes eliminar los documentos cargados por el equipo liquidador"
            }
            db.delete("evidencias", "id = ?", arrayOf(evidenciaId.toString()))
            insertarGestion(
                db, siniestroId, responsable,
                "Se eliminó el documento del caso: " + datos.first, false
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
