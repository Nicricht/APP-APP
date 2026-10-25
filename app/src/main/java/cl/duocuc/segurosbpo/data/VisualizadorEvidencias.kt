package cl.duocuc.segurosbpo.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Comparte una copia temporal de solo lectura; nunca expone el archivo de la base de datos. */
object VisualizadorEvidencias {
    /** Elimina únicamente copias temporales producidas por este visor al cerrar sesión. */
    fun limpiarTemporales(context: Context) {
        val carpeta = File(context.cacheDir, "evidencias_visibles")
        carpeta.listFiles()?.filter { it.isFile && it.name.startsWith("evidencia_") }
            ?.forEach { it.delete() }
    }

    fun abrir(context: Context, id: Long, archivo: ArchivoEvidencia): String? = runCatching<String?> {
        require(archivo.mime == "application/pdf" || archivo.mime.startsWith("image/")) {
            "El formato de este archivo no se puede abrir"
        }
        val extension = when (archivo.mime) {
            "application/pdf" -> "pdf"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val carpeta = File(context.cacheDir, "evidencias_visibles")
        check(carpeta.isDirectory || carpeta.mkdirs()) { "No se pudo preparar el archivo" }
        val temporal = File(carpeta, "evidencia_${id}.$extension")
        temporal.outputStream().use { it.write(archivo.bytes) }
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.evidencias", temporal
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, archivo.mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
        null
    }.getOrElse { error ->
        if (error is ActivityNotFoundException) "Instala un lector compatible para abrir este archivo"
        else error.message ?: "No se pudo abrir el archivo"
    }
}
