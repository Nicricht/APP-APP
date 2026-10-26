package cl.duocuc.segurosbpo.model

enum class EtapaSiniestro(val etiqueta: String, val paso: String) {
    RECIBIDO("Recibido", "recepción"),
    EVALUACION("En evaluación", "evaluación"),
    LIQUIDACION("En liquidación", "liquidación"),
    CERRADO("Cerrado", "cierre")
}

data class Siniestro(
    val id: Long,
    val numero: String,
    val asegurado: Asegurado,
    val compania: Compania,
    val tipo: TipoSiniestro,
    val fechaOcurrencia: String,
    val fechaReporte: String,
    val comuna: String,
    val descripcion: String,
    val huboTerceros: Boolean,
    val etapa: EtapaSiniestro,
    val ultimaActualizacion: String,
    val liquidadorAsignado: Liquidador? = null,
    val resultadoCierre: String? = null,
    val mensajeCierre: String? = null,
    val liquidadorId: String? = null,
    val liquidadorNombre: String? = null,
    val fechaCambioEtapa: String = ultimaActualizacion
)

fun Siniestro.coincideCon(texto: String): Boolean {
    val buscado = texto.trim().lowercase()
    if (buscado.isEmpty()) return true
    return numero.lowercase().contains(buscado) ||
        tipo.etiqueta.lowercase().contains(buscado) ||
        comuna.lowercase().contains(buscado) ||
        asegurado.etiqueta.lowercase().contains(buscado) ||
        compania.etiqueta.lowercase().contains(buscado)
}
