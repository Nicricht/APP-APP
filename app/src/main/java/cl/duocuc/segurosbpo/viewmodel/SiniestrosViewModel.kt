package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
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

    fun seleccionarAsegurado(nuevo: Asegurado) {
        _asegurado.value = nuevo
        preferencias.edit().putString("asegurado", nuevo.name).apply()
        refrescar()
    }

    fun refrescar() {
        _siniestros.value = db.listar(_asegurado.value)
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

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
