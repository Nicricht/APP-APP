package cl.duocuc.segurosbpo.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object ControlPlazos {
    fun diasCorridos(fecha: String, ahora: Date = Date()): Long {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val origen = runCatching { formato.parse(fecha.take(10)) }.getOrNull() ?: return 0
        val final = runCatching {
            formato.parse(formato.format(ahora))
        }.getOrNull() ?: return 0
        return maxOf(0L, TimeUnit.MILLISECONDS.toDays(final.time - origen.time))
    }

    fun diasHabilesTranscurridos(fecha: String, ahora: Date = Date()): Long {
        val formato = SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val origen = runCatching { formato.parse(fecha.take(10)) }.getOrNull() ?: return 0
        val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = origen }
        val corridos = diasCorridos(fecha, ahora)
        var habiles = 0L
        var contados = 0L
        while (contados < corridos) {
            calendario.add(Calendar.DAY_OF_MONTH, 1)
            contados++
            val dia = calendario.get(Calendar.DAY_OF_WEEK)
            if (dia != Calendar.SATURDAY && dia != Calendar.SUNDAY) habiles++
        }
        return habiles
    }

    fun resumenEtapa(
        fechaUltimoCambio: String,
        plazoDiasHabiles: Int,
        ahora: Date = Date()
    ): ResumenPlazo {
        val corridos = diasCorridos(fechaUltimoCambio, ahora)
        val habiles = diasHabilesTranscurridos(fechaUltimoCambio, ahora)
        return ResumenPlazo(
            diasCorridos = corridos,
            diasHabiles = habiles,
            plazoDiasHabiles = plazoDiasHabiles,
            excedido = habiles > plazoDiasHabiles
        )
    }
}

data class ResumenPlazo(
    val diasCorridos: Long,
    val diasHabiles: Long,
    val plazoDiasHabiles: Int,
    val excedido: Boolean
) {
    val diasRestantes: Int get() = maxOf(0, plazoDiasHabiles - diasHabiles.toInt())
}
