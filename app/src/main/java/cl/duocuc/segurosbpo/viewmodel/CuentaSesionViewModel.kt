package cl.duocuc.segurosbpo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.segurosbpo.data.CuentaDbHelper
import cl.duocuc.segurosbpo.model.Asegurado
import cl.duocuc.segurosbpo.model.CuentaLocal
import cl.duocuc.segurosbpo.model.Liquidador
import cl.duocuc.segurosbpo.model.RolCuenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CuentaSesionViewModel(application: Application) : AndroidViewModel(application) {
    private val db = CuentaDbHelper(application)
    private val preferencias = application.getSharedPreferences("cuenta_sesion_local", 0)
    private val inicial = preferencias.getString("cuenta_id", null)?.let { id ->
        if (id == "DEMO_ASEG_CAMILA") {
            crearAseguradoDemo()
        } else if (id.startsWith("DEMO_LIQ_")) {
            val equipo = runCatching {
                Liquidador.valueOf(id.removePrefix("DEMO_LIQ_"))
            }.getOrNull()
            equipo?.let { crearLiquidadorDemo(it) }
        } else db.obtener(id)
    }
    private val _cuenta = MutableStateFlow(inicial)
    val cuenta = _cuenta.asStateFlow()

    private fun guardar(cuenta: CuentaLocal) {
        preferencias.edit().putString("cuenta_id", cuenta.id).apply()
        _cuenta.value = cuenta
    }

    fun iniciar(correo: String, clave: String): String? {
        val resultado = runCatching { db.iniciarSesion(correo, clave) }
        val cuentaEncontrada = resultado.getOrNull()
        if (cuentaEncontrada != null) {
            guardar(cuentaEncontrada)
            return null
        }
        return resultado.exceptionOrNull()?.message ?: "Correo o contraseña incorrectos"
    }

    fun registrar(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        clave: String,
        confirmacion: String
    ): String? {
        val resultado = runCatching {
            db.registrarAsegurado(nombre, rut, correo, telefono, clave, confirmacion)
        }
        resultado.getOrNull()?.let {
            guardar(it)
            return null
        }
        return resultado.exceptionOrNull()?.message ?: "No se pudo registrar la cuenta"
    }

    private fun crearAseguradoDemo(): CuentaLocal = CuentaLocal(
        id = Asegurado.CAMILA.name,
        nombre = Asegurado.CAMILA.etiqueta,
        rut = Asegurado.CAMILA.rut,
        correo = "",
        telefono = "",
        rol = RolCuenta.ASEGURADO,
        equipo = null
    )

    fun accederAseguradoDemo() {
        preferencias.edit().putString("cuenta_id", "DEMO_ASEG_CAMILA").apply()
        _cuenta.value = crearAseguradoDemo()
    }

    private fun crearLiquidadorDemo(equipo: Liquidador): CuentaLocal = CuentaLocal(
        id = "DEMO_LIQ_" + equipo.name,
        nombre = equipo.nombre,
        rut = "",
        correo = "",
        telefono = "",
        rol = RolCuenta.LIQUIDADOR,
        equipo = equipo.name
    )

    fun accederLiquidadorDemo(equipo: Liquidador) {
        guardar(crearLiquidadorDemo(equipo))
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
