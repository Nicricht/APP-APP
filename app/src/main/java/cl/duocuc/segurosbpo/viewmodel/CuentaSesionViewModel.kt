package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.data.VisualizadorEvidencias
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CuentaSesionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CuentaDbHelper(application)
    private val preferencias = application.getSharedPreferences("cuenta_sesion_local", 0)
    private fun equipoValido(cuenta: CuentaLocal): Boolean = when (cuenta.rol) {
        RolCuenta.ASEGURADO ->
            cuenta.equipo == null && (Asegurado.entries.any { it.name == cuenta.id } ||
                cuenta.id.startsWith("USR_"))
        RolCuenta.LIQUIDADOR ->
            Liquidador.entries.any { it.name == cuenta.equipo } &&
                cuenta.id == "LIQ_" + cuenta.equipo
    }

    val perfilesDisponibles: List<CuentaLocal> = db.listarPerfiles().filter(::equipoValido)

    private val inicial = preferencias.getString("cuenta_id", null)?.let { id ->
        val idActual = when {
            id == "DEMO_ASEG_CAMILA" -> Asegurado.CAMILA.name
            id.startsWith("DEMO_LIQ_") -> "LIQ_" + id.removePrefix("DEMO_LIQ_")
            else -> id
        }
        runCatching { db.obtener(idActual) }.getOrNull()?.takeIf(::equipoValido)
    }
    private val _cuenta = MutableStateFlow(inicial)
    val cuenta = _cuenta.asStateFlow()

    init {
        if (inicial == null && preferencias.contains("cuenta_id")) {
            preferencias.edit().remove("cuenta_id").apply()
        }
    }

    private fun guardar(cuenta: CuentaLocal) {
        preferencias.edit().putString("cuenta_id", cuenta.id).apply()
        _cuenta.value = cuenta
    }

    fun seleccionarPerfil(id: String): String? {
        val perfil = runCatching { db.obtener(id) }.getOrNull()
            ?: return "No se encontró el perfil seleccionado"
        if (!equipoValido(perfil)) return "Este perfil de prueba no está disponible"
        guardar(perfil)
        return null
    }

    fun actualizarTelefono(telefono: String): String? {
        val actual = _cuenta.value ?: return "Selecciona un perfil"
        val resultado = runCatching { db.actualizarTelefono(actual.id, telefono) }
        return resultado.fold(
            onSuccess = { _cuenta.value = it; null },
            onFailure = { mensajeError(it, "No se pudo guardar") }
        )
    }

    fun actualizarFoto(bitmap: Bitmap): String? {
        val actual = _cuenta.value ?: return "Selecciona un perfil"
        val salida = ByteArrayOutputStream()
        if (!runCatching { bitmap.compress(Bitmap.CompressFormat.JPEG, 82, salida) }
                .getOrDefault(false)) {
            return "No se pudo procesar la fotografía"
        }
        val resultado = runCatching { db.actualizarFoto(actual.id, salida.toByteArray()) }
        return resultado.fold(
            onSuccess = { _cuenta.value = it; null },
            onFailure = { mensajeError(it, "No se pudo guardar la foto") }
        )
    }

    fun cerrarSesion() {
        VisualizadorEvidencias.limpiarTemporales(getApplication())
        preferencias.edit().remove("cuenta_id").apply()
        _cuenta.value = null
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
