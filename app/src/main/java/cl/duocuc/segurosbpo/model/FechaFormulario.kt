package cl.duocuc.segurosbpo.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FechaFormulario {
    fun formatear(milisegundosUtc: Long): String =
        SimpleDateFormat("dd-MM-yyyy", Locale.ROOT).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(milisegundosUtc))
}
