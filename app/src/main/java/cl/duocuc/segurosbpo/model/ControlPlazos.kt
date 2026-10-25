package cl.duocuc.segurosbpo.model

import java.text.SimpleDateFormat
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
}
