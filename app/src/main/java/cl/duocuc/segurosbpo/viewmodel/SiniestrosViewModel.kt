package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.model.AvisoSiniestro
import cl.duocuc.segurosbpo.model.SolicitudDocumento
import cl.duocuc.segurosbpo.model.Evidencia
import cl.duocuc.segurosbpo.model.Gestion
import cl.duocuc.segurosbpo.model.Liquidador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.Consulta
import cl.duocuc.segurosbpo.model.EvaluacionCierre
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Siniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private fun marcaDeTiempo(): String =
    SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ROOT).format(Date())

fun mensajeError(error: Throwable, predeterminado: String): String =
    error.message ?: predeterminado

class SiniestrosViewModel @JvmOverloads constructor(
    application: Application,
    private val db: SiniestroDbHelper = SiniestroDbHelper(application)
) : AndroidViewModel(application) {
    private val preferencias = application.getSharedPreferences("sesion_demo", 0)
    private val inicial = runCatching {
        Asegurado.valueOf(preferencias.getString("asegurado", Asegurado.CAMILA.name)!!)
    }.getOrDefault(Asegurado.CAMILA)

    private val _asegurado = MutableStateFlow(inicial)
    val asegurado = _asegurado.asStateFlow()
    private val _siniestros = MutableStateFlow(db.listar(inicial))
    val siniestros = _siniestros.asStateFlow()
    private val _estadisticasAsegurado = MutableStateFlow(db.listar(inicial))
    val estadisticasAsegurado = _estadisticasAsegurado.asStateFlow()
    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()
    private val _evidencias = MutableStateFlow<List<Evidencia>>(emptyList())
    val evidencias = _evidencias.asStateFlow()
    private val _gestiones = MutableStateFlow<List<Gestion>>(emptyList())
    val gestiones = _gestiones.asStateFlow()
    private val _liquidador = MutableStateFlow(Liquidador.VEHICULOS)
    val liquidador = _liquidador.asStateFlow()
    private val _casosLiquidador = MutableStateFlow(db.listarAsignados(Liquidador.VEHICULOS))
    val casosLiquidador = _casosLiquidador.asStateFlow()
    private val _seguimientosVencidos = MutableStateFlow(db.listarSeguimientosVencidos(Liquidador.VEHICULOS))
    val seguimientosVencidos = _seguimientosVencidos.asStateFlow()
    private val _disponiblesLiquidador = MutableStateFlow(db.listarDisponibles(Liquidador.VEHICULOS))
    val disponiblesLiquidador = _disponiblesLiquidador.asStateFlow()
    private val _gestionesLiquidador = MutableStateFlow<List<Gestion>>(emptyList())
    val gestionesLiquidador = _gestionesLiquidador.asStateFlow()
    private val _solicitudes = MutableStateFlow<List<SolicitudDocumento>>(emptyList())
    val solicitudes = _solicitudes.asStateFlow()
    private val _solicitudesLiquidador = MutableStateFlow<List<SolicitudDocumento>>(emptyList())
    val solicitudesLiquidador = _solicitudesLiquidador.asStateFlow()
    private val _evidenciasLiquidador = MutableStateFlow<List<Evidencia>>(emptyList())
    val evidenciasLiquidador = _evidenciasLiquidador.asStateFlow()
    private val _avisos = MutableStateFlow<List<AvisoSiniestro>>(db.listarAvisos(inicial))
    val avisos = _avisos.asStateFlow()
    private val _consultas = MutableStateFlow<List<Consulta>>(emptyList())
    val consultas = _consultas.asStateFlow()
    private val _consultasLiquidador = MutableStateFlow<List<Consulta>>(emptyList())
    val consultasLiquidador = _consultasLiquidador.asStateFlow()
    private val _evaluacion = MutableStateFlow<EvaluacionCierre?>(null)
    val evaluacion = _evaluacion.asStateFlow()
    private val _sinAsignar = MutableStateFlow<List<Siniestro>>(emptyList())
    val sinAsignar = _sinAsignar.asStateFlow()
    private val _cargaEquipos = MutableStateFlow<Map<Liquidador, Int>>(emptyMap())
    val cargaEquipos = _cargaEquipos.asStateFlow()
    private val _actualizadoEn = MutableStateFlow(marcaDeTiempo())
    val actualizadoEn = _actualizadoEn.asStateFlow()
    private val _companias = MutableStateFlow(db.listarCompanias())
    val companias = _companias.asStateFlow()
    private val _tipos = MutableStateFlow(db.listarTipos())
    val tipos = _tipos.asStateFlow()
    private val _cargaPersonas = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cargaPersonas = _cargaPersonas.asStateFlow()
    private var identidadLiquidador: String? = null
    private var nombreLiquidador: String = ""

    private fun responsable(): String =
        nombreLiquidador.ifBlank { _liquidador.value.equipo }

    private val _novedades = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val novedades = _novedades.asStateFlow()
    private val _archivados = MutableStateFlow<List<Siniestro>>(emptyList())
    val archivados = _archivados.asStateFlow()

    fun seleccionarLiquidador(nuevo: Liquidador) {
        _liquidador.value = nuevo
        _gestionesLiquidador.value = emptyList()
        _solicitudesLiquidador.value = emptyList()
        _consultasLiquidador.value = emptyList()
        _archivados.value = emptyList()
        refrescarAsignados()
        refrescarArchivados()
    }

    fun refrescarAsignados() {
        _casosLiquidador.value = db.listarAsignados(_liquidador.value, identidadLiquidador)
        _disponiblesLiquidador.value = db.listarDisponibles(_liquidador.value)
        _seguimientosVencidos.value = db.listarSeguimientosVencidos(_liquidador.value)
        _novedades.value = db.novedadesPorCaso(_liquidador.value, identidadLiquidador)
        _actualizadoEn.value = marcaDeTiempo()
    }

    fun tomarCasoLiquidador(id: Long): String? {
        val resultado = runCatching {
            db.tomarCaso(
                id, _liquidador.value, identidadLiquidador.orEmpty(), nombreLiquidador
            )
        }
        val mensaje = resultado.fold(
            onSuccess = { asignado ->
                if (asignado) null else "El caso ya fue tomado o no corresponde a tu área"
            },
            onFailure = { mensajeError(it, "No se pudo tomar el caso") }
        )
        refrescarAsignados()
        if (mensaje == null) {
            refrescar()
            refrescarAvisos()
        }
        return mensaje
    }

    fun cargarGestionesLiquidador(id: Long) {
        _gestionesLiquidador.value = db.listarGestionesAsignadas(id, _liquidador.value)
        _solicitudesLiquidador.value = db.listarSolicitudesAsignadas(id, _liquidador.value)
        _consultasLiquidador.value = db.listarConsultasAsignadas(id, _liquidador.value)
    }

    fun solicitarDocumentoLiquidador(id: Long, tipo: String, comentario: String): String? {
        val error = runCatching {
            db.solicitarDocumento(id, _liquidador.value, tipo, comentario, responsable())
        }.exceptionOrNull()?.let { mensajeError(it, "No se pudo solicitar el documento") }
        if (error == null) {
            refrescarAsignados()
            cargarGestionesLiquidador(id)
            refrescar()
            refrescarAvisos()
        }
        return error
    }

    fun registrarGestionLiquidador(id: Long, descripcion: String, visible: Boolean): String? {
        val error = runCatching {
            db.registrarGestionAsignada(
                id, _liquidador.value, descripcion, visible, responsable()
            )
        }.exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar la gestión") }
        if (error == null) {
            refrescarAsignados()
            cargarGestionesLiquidador(id)
        }
        return error
    }

    fun avanzarLiquidador(id: Long, resultado: String, mensaje: String): String? {
        val error = runCatching {
            db.avanzarAsignado(
                id, _liquidador.value, resultado, mensaje, null, responsable()
            )
        }.exceptionOrNull()?.let { mensajeError(it, "No se pudo avanzar de etapa") }
        if (error == null) {
            refrescarAsignados()
            cargarGestionesLiquidador(id)
            refrescar()
            refrescarAvisos()
        }
        return error
    }

    fun cerrarAnticipadoLiquidador(id: Long, resultado: String, mensaje: String): String? {
        val error = runCatching {
            db.avanzarAsignado(
                id, _liquidador.value, resultado, mensaje,
                EtapaSiniestro.CERRADO, responsable()
            )
        }.exceptionOrNull()?.let { mensajeError(it, "No se pudo cerrar el siniestro") }
        if (error == null) {
            refrescarAsignados()
            cargarGestionesLiquidador(id)
            refrescar()
            refrescarAvisos()
        }
        return error
    }

    fun buscar(texto: String) {
        _busqueda.value = texto
        refrescar()
    }

    fun seleccionarAsegurado(nuevo: Asegurado) {
        _evidencias.value = emptyList()
        _gestiones.value = emptyList()
        _solicitudes.value = emptyList()
        _consultas.value = emptyList()
        _evaluacion.value = null
        _busqueda.value = ""
        _asegurado.value = nuevo
        preferencias.edit().putString("asegurado", nuevo.name).apply()
        refrescar()
        refrescarAvisos()
    }

    fun refrescar() {
        _siniestros.value = db.listar(_asegurado.value, _busqueda.value)
        _estadisticasAsegurado.value = db.listar(_asegurado.value)
        _actualizadoEn.value = marcaDeTiempo()
    }

    fun buscarPosibleDuplicado(estado: FormularioUiState): String? =
        if (estado.asegurado.name != _asegurado.value.name) null else db.buscarPosibleDuplicado(estado)

    var ultimoRegistrado: Long? = null
        private set

    fun registrar(estado: FormularioUiState): String {
        require(estado.asegurado == _asegurado.value) { "El asegurado seleccionado cambió" }
        val nuevo = db.registrar(estado)
        ultimoRegistrado = nuevo.id
        refrescar()
        refrescarAsignados()
        return nuevo.numero
    }

    fun obtener(id: Long): Siniestro? = db.obtener(id, _asegurado.value)

    fun actualizarDescripcion(id: Long, descripcion: String): Boolean {
        val actualizado = db.actualizarDescripcion(id, _asegurado.value, descripcion)
        if (actualizado) refrescar()
        return actualizado
    }

    suspend fun cargarImagen(siniestroId: Long, evidenciaId: Long): Bitmap? {
        val propietario = _asegurado.value
        return withContext(Dispatchers.IO) {
            runCatching { db.obtenerImagen(evidenciaId, siniestroId, propietario) }.getOrNull()
        }
    }

    suspend fun descargarDocumentoLiquidador(
        siniestroId: Long, evidenciaId: Long, destino: Uri
    ): String? {
        val equipo = _liquidador.value
        val contexto = getApplication<Application>()
        return withContext(Dispatchers.IO) {
            runCatching {
                val archivo = db.obtenerArchivoAsignado(evidenciaId, siniestroId, equipo)
                    ?: return@runCatching "El documento no está disponible"
                val salida = contexto.contentResolver.openOutputStream(destino)
                    ?: return@runCatching "No se pudo escribir el archivo"
                salida.use { it.write(archivo.bytes) }
                null
            }.getOrElse { mensajeError(it, "No se pudo descargar el documento") }
        }
    }

    suspend fun cargarMiniatura(siniestroId: Long, evidenciaId: Long): Bitmap? {
        val propietario = _asegurado.value
        return withContext(Dispatchers.IO) {
            runCatching { db.obtenerImagen(evidenciaId, siniestroId, propietario, 240) }.getOrNull()
        }
    }

    suspend fun cargarMiniaturaLiquidador(siniestroId: Long, evidenciaId: Long): Bitmap? {
        val equipo = _liquidador.value
        return withContext(Dispatchers.IO) {
            runCatching {
                db.obtenerImagenAsignada(evidenciaId, siniestroId, equipo, 240)
            }.getOrNull()
        }
    }

    fun refrescarGestiones(id: Long) {
        _gestiones.value = db.listarGestiones(id, _asegurado.value)
    }

    fun refrescarEvidencias(id: Long) {
        _evidencias.value = db.listarEvidencias(id, _asegurado.value)
        _solicitudes.value = db.listarSolicitudes(id, _asegurado.value)
    }

    fun refrescarAvisos() {
        db.generarRecordatorios()
        _avisos.value = db.listarAvisos(_asegurado.value)
    }

    fun marcarAvisoLeido(id: Long) {
        db.marcarAvisoLeido(id, _asegurado.value)
        refrescarAvisos()
    }

    fun cargarEvidenciasLiquidador(id: Long) {
        _evidenciasLiquidador.value =
            runCatching { db.listarEvidenciasAsignadas(id, _liquidador.value) }
                .getOrDefault(emptyList())
    }

    suspend fun cargarImagenLiquidador(siniestroId: Long, evidenciaId: Long): Bitmap? {
        val equipo = _liquidador.value
        return withContext(Dispatchers.IO) {
            runCatching { db.obtenerImagenAsignada(evidenciaId, siniestroId, equipo) }.getOrNull()
        }
    }

    suspend fun guardarFotoLiquidador(id: Long, tipoDocumento: String, bitmap: Bitmap): String? {
        val equipo = _liquidador.value
        val error = withContext(Dispatchers.IO) {
            runCatching {
                db.guardarEvidenciaLiquidador(
                    id, equipo, tipoDocumento, ProcesadorEvidencia.desdeCamara(bitmap)
                )
            }.exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar el documento") }
        }
        if (error == null) {
            cargarEvidenciasLiquidador(id)
            cargarGestionesLiquidador(id)
            refrescarAsignados()
        }
        return error
    }

    suspend fun guardarArchivoLiquidador(id: Long, tipoDocumento: String, uri: Uri): String? {
        val equipo = _liquidador.value
        val error = withContext(Dispatchers.IO) {
            runCatching {
                val archivo = ProcesadorEvidencia.desdeSelector(getApplication(), uri)
                db.guardarEvidenciaLiquidador(id, equipo, tipoDocumento, archivo)
            }.exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar el documento") }
        }
        if (error == null) {
            cargarEvidenciasLiquidador(id)
            cargarGestionesLiquidador(id)
            refrescarAsignados()
        }
        return error
    }

    suspend fun guardarFoto(id: Long, tipoDocumento: String, bitmap: Bitmap): String? {
        val propietario = _asegurado.value
        val error = withContext(Dispatchers.IO) {
            runCatching {
                db.guardarEvidencia(id, propietario, tipoDocumento, ProcesadorEvidencia.desdeCamara(bitmap))
            }.exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar el documento") }
        }
        if (error == null) {
            refrescarEvidencias(id)
            refrescar()
        }
        return error
    }

    suspend fun guardarArchivo(id: Long, tipoDocumento: String, uri: Uri): String? {
        val propietario = _asegurado.value
        val error = withContext(Dispatchers.IO) {
            runCatching {
                val archivo = ProcesadorEvidencia.desdeSelector(getApplication(), uri)
                db.guardarEvidencia(id, propietario, tipoDocumento, archivo)
            }.exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar el documento") }
        }
        if (error == null) {
            refrescarEvidencias(id)
            refrescar()
        }
        return error
    }

    fun archivarCaso(id: Long): String? {
        val error = runCatching { db.archivarCaso(id, _liquidador.value, responsable()) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo archivar el caso") }
        if (error == null) {
            refrescarAsignados()
            refrescarArchivados()
        }
        return error
    }

    fun restaurarCaso(id: Long): String? {
        val error = runCatching { db.restaurarCaso(id, _liquidador.value) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo restaurar el caso") }
        if (error == null) {
            refrescarAsignados()
            refrescarArchivados()
        }
        return error
    }

    fun marcarSolicitudRecibida(siniestroId: Long, solicitudId: Long): String? {
        val error = runCatching {
            db.marcarSolicitudRecibida(solicitudId, _liquidador.value, responsable())
        }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo confirmar la recepción") }
        if (error == null) {
            cargarGestionesLiquidador(siniestroId)
            refrescarAsignados()
            refrescar()
            refrescarAvisos()
        }
        return error
    }

    fun anularSolicitud(siniestroId: Long, solicitudId: Long): String? {
        val error = runCatching { db.anularSolicitud(solicitudId, _liquidador.value, responsable()) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo anular la solicitud") }
        if (error == null) {
            cargarGestionesLiquidador(siniestroId)
            refrescarAsignados()
            refrescar()
            refrescarAvisos()
        }
        return error
    }

    fun identificarLiquidador(id: String?, nombre: String = "") {
        identidadLiquidador = id
        nombreLiquidador = nombre
        refrescarAsignados()
        refrescarArchivados()
    }

    fun crearCompania(etiqueta: String, plazo: Int): String? {
        val error = runCatching { db.crearCompania(etiqueta, plazo) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo crear la compañía") }
        if (error == null) _companias.value = db.listarCompanias()
        return error
    }

    fun crearTipoSiniestro(
        etiqueta: String, esVehiculo: Boolean, documentos: List<String>
    ): String? {
        val error = runCatching { db.crearTipoSiniestro(etiqueta, esVehiculo, documentos) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo crear el tipo de siniestro") }
        if (error == null) _tipos.value = db.listarTipos()
        return error
    }

    fun refrescarArchivados() {
        _archivados.value = db.listarArchivados(_liquidador.value, identidadLiquidador)
    }

    fun refrescarAnalista() {
        _sinAsignar.value = db.listarSinAsignar()
        _cargaEquipos.value = db.cargaPorLiquidador()
        _cargaPersonas.value = db.cargaPorPersona()
        _actualizadoEn.value = marcaDeTiempo()
    }

    fun asignarCaso(id: Long, equipo: Liquidador, personaId: String, persona: String): String? {
        val error = runCatching { db.asignarLiquidador(id, equipo, personaId, persona) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo asignar el caso") }
        if (error == null) {
            refrescarAnalista()
            refrescar()
            refrescarAvisos()
        }
        return error
    }

    fun refrescarConsultas(id: Long) {
        _consultas.value = db.listarConsultas(id, _asegurado.value)
    }

    fun enviarConsulta(id: Long, pregunta: String): String? {
        val error = runCatching { db.registrarConsulta(id, _asegurado.value, pregunta) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo enviar la consulta") }
        if (error == null) {
            refrescarConsultas(id)
            refrescarGestiones(id)
            refrescar()
        }
        return error
    }

    fun responderConsulta(siniestroId: Long, consultaId: Long, respuesta: String): String? {
        val error = runCatching {
            db.responderConsulta(consultaId, _liquidador.value, respuesta, responsable())
        }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar la respuesta") }
        if (error == null) {
            cargarGestionesLiquidador(siniestroId)
            refrescarAsignados()
        }
        return error
    }

    fun desistir(id: Long, motivo: String): String? {
        val error = runCatching { db.desistir(id, _asegurado.value, motivo) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo registrar el desistimiento") }
        if (error == null) {
            refrescar()
            refrescarAvisos()
            refrescarGestiones(id)
        }
        return error
    }

    fun refrescarEvaluacion(id: Long) {
        _evaluacion.value = db.obtenerEvaluacion(id, _asegurado.value)
    }

    fun guardarEvaluacion(id: Long, nota: Int, comentario: String): String? {
        val error = runCatching { db.guardarEvaluacion(id, _asegurado.value, nota, comentario) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar la evaluación") }
        if (error == null) refrescarEvaluacion(id)
        return error
    }

    fun eliminarEvidencia(siniestroId: Long, evidenciaId: Long): String? {
        val error = runCatching { db.eliminarEvidencia(evidenciaId, siniestroId, _asegurado.value) }
            .exceptionOrNull()?.let { mensajeError(it, "No se pudo eliminar el documento") }
        if (error == null) {
            refrescarEvidencias(siniestroId)
            refrescar()
        }
        return error
    }

    fun eliminarEvidenciaLiquidador(siniestroId: Long, evidenciaId: Long): String? {
        val error = runCatching {
            db.eliminarEvidenciaLiquidador(
                evidenciaId, siniestroId, _liquidador.value, responsable()
            )
        }.exceptionOrNull()?.let { mensajeError(it, "No se pudo eliminar el documento") }
        if (error == null) {
            cargarEvidenciasLiquidador(siniestroId)
            cargarGestionesLiquidador(siniestroId)
        }
        return error
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
