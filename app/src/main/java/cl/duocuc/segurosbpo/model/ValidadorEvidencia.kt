package cl.duocuc.segurosbpo.model


object ValidadorEvidencia {
    const val MAX_ARCHIVOS = 15
    const val MAX_BYTES = 5 * 1024 * 1024

    fun error(tipoDocumento: String, tipoMime: String, tamano: Int, cantidadActual: Int): String? =
        when {
            tipoDocumento.isBlank() -> "Indica qué documento estás adjuntando"
            tipoMime !in setOf("image/jpeg", "image/png", "image/webp", "application/pdf") ->
                "Solo se aceptan imágenes JPG, PNG, WEBP o PDF"
            tamano == 0 -> "El archivo está vacío"
            tamano > MAX_BYTES -> "El archivo supera los 5 MB permitidos"
            cantidadActual >= MAX_ARCHIVOS -> "El siniestro ya tiene 15 archivos"
            else -> null
        }
}
