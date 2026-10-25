package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CuentaSesionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CuentaDbHelper(application)
    private val preferencias = application.getSharedPreferences("cuenta_sesion_local", 0)

    private fun cargar(): List<CuentaLocal> =
        runCatching { db.listar() }.getOrDefault(emptyList())

    private val _usuarios = MutableStateFlow(cargar())
    val usuarios = _usuarios.asStateFlow()

    private val _cuenta = MutableStateFlow(
        preferencias.getString("cuenta_id", null)?.let { id ->
            _usuarios.value.firstOrNull { it.id == id }
        }
    )
    val cuenta = _cuenta.asStateFlow()

    init {
        if (_cuenta.value == null && preferencias.contains("cuenta_id")) {
            preferencias.edit().remove("cuenta_id").apply()
        }
    }

    fun seleccionar(id: String) {
        val elegido = _usuarios.value.firstOrNull { it.id == id } ?: return
        preferencias.edit().putString("cuenta_id", elegido.id).apply()
        _cuenta.value = elegido
    }

    fun actualizarTelefono(telefono: String): String? {
        val actual = _cuenta.value ?: return "Elige un usuario"
        val resultado = runCatching { db.actualizarTelefono(actual.id, telefono) }
        return resultado.fold(
            onSuccess = { _cuenta.value = it; _usuarios.value = cargar(); null },
            onFailure = { mensajeError(it, "No se pudo guardar") }
        )
    }

    fun actualizarContacto(correo: String, telefono: String): String? {
        val actual = _cuenta.value ?: return "Elige un usuario"
        val resultado = runCatching { db.actualizarContacto(actual.id, correo, telefono) }
        return resultado.fold(
            onSuccess = { _cuenta.value = it; _usuarios.value = cargar(); null },
            onFailure = { mensajeError(it, "No se pudo guardar") }
        )
    }

    fun actualizarFoto(bitmap: Bitmap): String? {
        val actual = _cuenta.value ?: return "Elige un usuario"
        val salida = ByteArrayOutputStream()
        if (!runCatching { bitmap.compress(Bitmap.CompressFormat.JPEG, 82, salida) }
                .getOrDefault(false)) {
            return "No se pudo procesar la fotografía"
        }
        val resultado = runCatching { db.actualizarFoto(actual.id, salida.toByteArray()) }
        return resultado.fold(
            onSuccess = { _cuenta.value = it; _usuarios.value = cargar(); null },
            onFailure = { mensajeError(it, "No se pudo guardar la foto") }
        )
    }

    fun crearLiquidador(nombre: String, equipo: Liquidador, telefono: String): String? {
        val resultado = runCatching { db.crearLiquidador(nombre, equipo, telefono) }
        return resultado.fold(
            onSuccess = { _usuarios.value = cargar(); null },
            onFailure = { mensajeError(it, "No se pudo crear el liquidador") }
        )
    }

    fun liquidadoresDe(equipo: Liquidador): List<CuentaLocal> =
        _usuarios.value.filter {
            it.rol == RolCuenta.LIQUIDADOR && it.equipo == equipo.name
        }

    fun cerrarSesion() {
        preferencias.edit().remove("cuenta_id").apply()
        _cuenta.value = null
    }

    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}
