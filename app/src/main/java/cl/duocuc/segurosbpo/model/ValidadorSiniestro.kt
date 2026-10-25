package cl.duocuc.segurosbpo.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit


object ValidadorSiniestro {
    fun errorFecha(fecha: String, hoy: Date = Date()): String? {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply { isLenient = false }
        val parsed = try {
            if (!Regex("\\d{2}-\\d{2}-\\d{4}").matches(fecha)) null else formato.parse(fecha)
        } catch (_: Exception) { null }
        return when {
            parsed == null -> "Selecciona una fecha válida"
            parsed.after(hoy) -> "La fecha no puede ser futura"
            else -> null
        }
    }

    fun reporteFueraDePlazo(fechaOcurrencia: String, fechaReporte: String): Boolean {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun convertir(fecha: String): Date? = try {
            if (!Regex("\\d{2}-\\d{2}-\\d{4}").matches(fecha)) null
            else formato.parse(fecha)
        } catch (_: Exception) { null }

        val ocurrencia = convertir(fechaOcurrencia) ?: return false
        val reporte = convertir(fechaReporte) ?: return false
        return reporte.time - ocurrencia.time > TimeUnit.DAYS.toMillis(30)
    }

    fun errorComuna(comuna: String): String? =
        if (comuna.trim().isEmpty()) "Indica la comuna" else null

    fun errorDescripcion(descripcion: String): String? =
        if (descripcion.trim().isEmpty()) "Cuéntanos qué pasó" else null

    fun errorCompania(compania: Compania): String? =
        if (!compania.activa) "No se pueden registrar casos nuevos con esta compañía" else null
}
