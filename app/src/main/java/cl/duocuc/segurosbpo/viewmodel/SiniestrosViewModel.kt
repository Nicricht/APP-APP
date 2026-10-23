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
import cl.duocuc.segurosbpo.model.EtapaSiniestro
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Siniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


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
    private val _disponiblesLiquidador = MutableStateFlow(db.listarDisponibles(Liquidador.VEHICULOS))
    val disponiblesLiquidador = _disponiblesLiquidador.asStateFlow()
    private val _gestionesLiquidador = MutableStateFlow<List<Gestion>>(emptyList())
    val gestionesLiquidador = _gestionesLiquidador.asStateFlow()
    private val _solicitudes = MutableStateFlow<List<SolicitudDocumento>>(emptyList())
    val solicitudes = _solicitudes.asStateFlow()
    private val _solicitudesLiquidador = MutableStateFlow<List<SolicitudDocumento>>(emptyList())
    val solicitudesLiquidador = _solicitudesLiquidador.asStateFlow()
    private val _avisos = MutableStateFlow<List<AvisoSiniestro>>(db.listarAvisos(inicial))
    val avisos = _avisos.asStateFlow()

    fun seleccionarLiquidador(nuevo: Liquidador) {
        _liquidador.value = nuevo
        _gestionesLiquidador.value = emptyList()
        _solicitudesLiquidador.value = emptyList()
        refrescarAsignados()
    }

    fun refrescarAsignados() {
        _casosLiquidador.value = db.listarAsignados(_liquidador.value)
        _disponiblesLiquidador.value = db.listarDisponibles(_liquidador.value)
    }

    fun tomarCasoLiquidador(id: Long): String? {
        val resultado = runCatching {
            db.tomarCaso(id, _liquidador.value)
        }
        val mensaje = resultado.exceptionOrNull()?.let {
            mensajeError(it, "No se pudo tomar el caso")
        } ?: if (resultado.getOrNull() != true) "El caso ya fue tomado o no corresponde a tu área" else null
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
    }

    fun solicitarDocumentoLiquidador(id: Long, tipo: String, comentario: String): String? {
        val error = runCatching {
            db.solicitarDocumento(id, _liquidador.value, tipo, comentario)
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
            db.registrarGestionAsignada(id, _liquidador.value, descripcion, visible)
        }.exceptionOrNull()?.let { mensajeError(it, "No se pudo guardar la gestión") }
        if (error == null) {
            refrescarAsignados()
            cargarGestionesLiquidador(id)
        }
        return error
    }

    fun avanzarLiquidador(id: Long, resultado: String, mensaje: String): String? {
        val error = runCatching {
            db.avanzarAsignado(id, _liquidador.value, resultado, mensaje)
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
            db.avanzarAsignado(id, _liquidador.value, resultado, mensaje, EtapaSiniestro.CERRADO)
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
        _busqueda.value = ""
        _asegurado.value = nuevo
        preferencias.edit().putString("asegurado", nuevo.name).apply()
        refrescar()
        refrescarAvisos()
    }

    fun refrescar() {
        _siniestros.value = db.listar(_asegurado.value, _busqueda.value)
        _estadisticasAsegurado.value = db.listar(_asegurado.value)
    }

    fun buscarPosibleDuplicado(estado: FormularioUiState): String? =
        if (estado.asegurado.name != _asegurado.value.name) null else db.buscarPosibleDuplicado(estado)

    fun registrar(estado: FormularioUiState): String {
        require(estado.asegurado == _asegurado.value) { "El asegurado seleccionado cambió" }
        val nuevo = db.registrar(estado)
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
        return withContext(Dispatchers.IO) { db.obtenerImagen(evidenciaId, siniestroId, propietario) }
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

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
