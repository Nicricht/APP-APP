package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import cl.duocuc.segurosbpo.data.ProcesadorEvidencia
import cl.duocuc.segurosbpo.model.Evidencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.segurosbpo.data.SiniestroDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.FormularioUiState
import cl.duocuc.segurosbpo.model.Siniestro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Lista y persistencia local siguiendo UsuariosViewModel de MiRegistroAvatar. */
class SiniestrosViewModel(application: Application) : AndroidViewModel(application) {
    private val db = SiniestroDbHelper(application)
    private val preferencias = application.getSharedPreferences("sesion_demo", 0)
    private val inicial = runCatching {
        Asegurado.valueOf(preferencias.getString("asegurado", Asegurado.CAMILA.name)!!)
    }.getOrDefault(Asegurado.CAMILA)

    private val _asegurado = MutableStateFlow(inicial)
    val asegurado = _asegurado.asStateFlow()
    private val _siniestros = MutableStateFlow(db.listar(inicial))
    val siniestros = _siniestros.asStateFlow()
    private val _busqueda = MutableStateFlow("")
    val busqueda = _busqueda.asStateFlow()
    private val _evidencias = MutableStateFlow<List<Evidencia>>(emptyList())
    val evidencias = _evidencias.asStateFlow()

    fun buscar(texto: String) {
        _busqueda.value = texto
        refrescar()
    }

    fun seleccionarAsegurado(nuevo: Asegurado) {
        _evidencias.value = emptyList()
        _busqueda.value = ""
        _asegurado.value = nuevo
        preferencias.edit().putString("asegurado", nuevo.name).apply()
        refrescar()
    }

    fun refrescar() {
        _siniestros.value = db.listar(_asegurado.value, _busqueda.value)
    }

    fun registrar(estado: FormularioUiState): String {
        require(estado.asegurado == _asegurado.value) { "El asegurado seleccionado cambió" }
        val nuevo = db.registrar(estado)
        refrescar()
        return nuevo.numero
    }

    fun obtener(id: Long): Siniestro? = db.obtener(id, _asegurado.value)

    fun actualizarDescripcion(id: Long, descripcion: String): Boolean {
        val actualizado = db.actualizarDescripcion(id, _asegurado.value, descripcion)
        if (actualizado) refrescar()
        return actualizado
    }

    fun refrescarEvidencias(id: Long) {
        _evidencias.value = db.listarEvidencias(id, _asegurado.value)
    }

    suspend fun guardarFoto(id: Long, tipoDocumento: String, bitmap: Bitmap): String? {
        val propietario = _asegurado.value
        val error = withContext(Dispatchers.IO) {
            runCatching {
                db.guardarEvidencia(id, propietario, tipoDocumento, ProcesadorEvidencia.desdeCamara(bitmap))
            }.exceptionOrNull()?.let { it.message ?: "No se pudo guardar el documento" }
        }
        if (error == null) refrescarEvidencias(id)
        return error
    }

    suspend fun guardarArchivo(id: Long, tipoDocumento: String, uri: Uri): String? {
        val propietario = _asegurado.value
        val error = withContext(Dispatchers.IO) {
            runCatching {
                val archivo = ProcesadorEvidencia.desdeSelector(getApplication(), uri)
                db.guardarEvidencia(id, propietario, tipoDocumento, archivo)
            }.exceptionOrNull()?.let { it.message ?: "No se pudo guardar el documento" }
        }
        if (error == null) refrescarEvidencias(id)
        return error
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
