package cl.duocuc.segurosbpo.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import java.io.ByteArrayOutputStream

data class ArchivoEvidencia(val nombre: String, val mime: String, val bytes: ByteArray)

/** Como ConvertidorImagen del profesor, pero acepta imágenes y PDF de máximo 5 MB. */
object ProcesadorEvidencia {
    fun desdeCamara(bitmap: Bitmap): ArchivoEvidencia {
        val salida = ByteArrayOutputStream()
        check(bitmap.compress(Bitmap.CompressFormat.JPEG, 85, salida)) {
            "No se pudo convertir la fotografía"
        }
        val bytes = salida.toByteArray()
        require(bytes.size <= ValidadorEvidencia.MAX_BYTES) { "Foto demasiado grande" }
        return ArchivoEvidencia("foto_camara.jpg", "image/jpeg", bytes)
    }

    fun desdeSelector(context: Context, uri: Uri): ArchivoEvidencia {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri).orEmpty().lowercase()
        require(mime in setOf("image/jpeg", "image/png", "image/webp", "application/pdf")) {
            "Selecciona una imagen JPG, PNG, WEBP o un PDF"
        }
        var nombre = "documento"
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) nombre = it.getString(0) ?: nombre
        }
        val salida = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        var total = 0
        val entrada = resolver.openInputStream(uri) ?: error("No se pudo abrir el archivo")
        entrada.use { flujo ->
            while (true) {
                val leidos = flujo.read(buffer)
                if (leidos == -1) break
                total += leidos
                require(total <= ValidadorEvidencia.MAX_BYTES) {
                    "El archivo supera los 5 MB permitidos"
                }
                salida.write(buffer, 0, leidos)
            }
        }
        val bytes = salida.toByteArray()
        // Comprobación mínima de contenido; no confiar únicamente en la extensión.
        val valido = when (mime) {
            "application/pdf" -> bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray())
            "image/jpeg" -> bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
            "image/png" -> bytes.size >= 8 && bytes.copyOfRange(0, 4).contentEquals(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47))
            "image/webp" -> bytes.size >= 12 && String(bytes, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP"
            else -> false
        }
        require(valido) { "El contenido del archivo no coincide con su formato" }
        return ArchivoEvidencia(nombre, mime, bytes)
    }
}
