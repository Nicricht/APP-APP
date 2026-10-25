package cl.duocuc.segurosbpo.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import cl.duocuc.segurosbpo.model.ValidadorEvidencia
import java.io.ByteArrayOutputStream

data class ArchivoEvidencia(val nombre: String, val mime: String, val bytes: ByteArray)


object ProcesadorEvidencia {
    private const val MAX_LADO = 1600

    private val validadoresFirma: Map<String, (ByteArray) -> Boolean> = mapOf(
        "application/pdf" to { bytes ->
            bytes.size >= 5 && bytes.copyOfRange(0, 5).contentEquals("%PDF-".toByteArray())
        },
        "image/jpeg" to { bytes ->
            bytes.size >= 3 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()
        },
        "image/png" to { bytes ->
            bytes.size >= 8 && bytes.copyOfRange(0, 4).contentEquals(
                byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
            )
        },
        "image/webp" to { bytes ->
            bytes.size >= 12 && String(bytes, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                String(bytes, 8, 4, Charsets.US_ASCII) == "WEBP"
        }
    )

    fun desdeCamara(bitmap: Bitmap): ArchivoEvidencia =
        desdeCamara(bitmap) { imagen, salida ->
            imagen.compress(Bitmap.CompressFormat.JPEG, 85, salida)
        }

    internal fun desdeCamara(
        bitmap: Bitmap,
        comprimir: (Bitmap, ByteArrayOutputStream) -> Boolean
    ): ArchivoEvidencia {
        val salida = ByteArrayOutputStream()
        check(comprimir(bitmap, salida)) {
            "No se pudo convertir la fotografía"
        }
        val bytes = salida.toByteArray()
        require(bytes.size <= ValidadorEvidencia.MAX_BYTES) { "Foto demasiado grande" }
        return ArchivoEvidencia("foto_camara.jpg", "image/jpeg", bytes)
    }

    fun desdeSelector(context: Context, uri: Uri): ArchivoEvidencia {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri).orEmpty().lowercase()
        val verificarFirma = validadoresFirma[mime]
            ?: throw IllegalArgumentException("Selecciona una imagen JPG, PNG, WEBP o un PDF")
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

        val valido = verificarFirma(bytes)
        require(valido) { "El contenido del archivo no coincide con su formato" }
        if (!mime.startsWith("image/")) return ArchivoEvidencia(nombre, mime, bytes)
        return comprimirImagen(nombre, bytes)
    }

    private fun comprimirImagen(nombre: String, original: ByteArray): ArchivoEvidencia {
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(original, 0, original.size, medidas)
        var escala = 1
        while (medidas.outWidth / escala > MAX_LADO || medidas.outHeight / escala > MAX_LADO) {
            escala *= 2
        }
        val reducida = runCatching {
            BitmapFactory.decodeByteArray(
                original, 0, original.size,
                BitmapFactory.Options().apply { inSampleSize = escala }
            )
        }.getOrNull() ?: return ArchivoEvidencia(nombre, "image/jpeg", original)
        val salida = ByteArrayOutputStream()
        val comprimida = reducida.compress(Bitmap.CompressFormat.JPEG, 80, salida)
        reducida.recycle()
        if (!comprimida) return ArchivoEvidencia(nombre, "image/jpeg", original)
        return ArchivoEvidencia(nombre, "image/jpeg", salida.toByteArray())
    }
}
