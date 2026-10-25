package cl.duocuc.segurosbpo.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** Conserva la selección temporal al recrearse la pantalla (por ejemplo, al girar). */
class BorradorEvidenciaViewModel : ViewModel() {
    var fotoPendiente by mutableStateOf<Bitmap?>(null)
        private set
    var archivoPendiente by mutableStateOf<Uri?>(null)
        private set
    var origenPendiente by mutableStateOf("")
        private set
    var guardando by mutableStateOf(false)
        private set
    var resultado by mutableStateOf<String?>(null)
        private set
    var guardadoCorrecto by mutableStateOf(false)
        private set

    fun prepararFoto(foto: Bitmap) {
        fotoPendiente = foto
        archivoPendiente = null
        origenPendiente = "Fotografía tomada"
    }

    fun prepararArchivo(uri: Uri, nombre: String) {
        archivoPendiente = uri
        fotoPendiente = null
        origenPendiente = nombre
    }

    fun limpiar() {
        fotoPendiente = null
        archivoPendiente = null
        origenPendiente = ""
    }

    fun guardar(
        tipo: String,
        onGuardarFoto: suspend (String, Bitmap) -> String?,
        onGuardarArchivo: suspend (String, Uri) -> String?
    ) {
        if (guardando) return
        val foto = fotoPendiente
        val archivo = archivoPendiente
        if (tipo.isBlank() || (foto == null && archivo == null)) {
            resultado = "Completa el tipo de documento y selecciona un archivo"
            guardadoCorrecto = false
            return
        }
        guardando = true
        viewModelScope.launch {
            val error = try {
                when {
                    foto != null -> onGuardarFoto(tipo.trim(), foto)
                    archivo != null -> onGuardarArchivo(tipo.trim(), archivo)
                    else -> "Selecciona un documento"
                }
            } catch (e: Exception) {
                e.message ?: "No se pudo guardar el documento"
            } finally {
                guardando = false
            }
            guardadoCorrecto = error == null
            if (error == null) limpiar()
            resultado = error ?: "Documento adjuntado correctamente"
        }
    }

    fun consumirResultado() { resultado = null }
}
