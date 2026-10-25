package cl.duocuc.segurosbpo.model

/** Catálogo visible para asegurados y liquidadores; no fuerza texto arbitrario. */
object TipoDocumento {
    val opciones = listOf(
        "Fotografía del daño",
        "Declaración del siniestro",
        "Padrón del vehículo",
        "Licencia de conducir",
        "Constancia policial",
        "Presupuesto de reparación",
        "Comprobante de domicilio",
        "Póliza de seguro",
        "Otro documento"
    )

    /** Incluye nombres de solicitudes antiguas para poder entregarlas sin perder su vínculo. */
    fun paraSolicitudes(solicitados: List<String>): List<String> =
        (opciones + solicitados.map { it.trim() }.filter { it.isNotBlank() })
            .distinctBy { it.lowercase() }
}
