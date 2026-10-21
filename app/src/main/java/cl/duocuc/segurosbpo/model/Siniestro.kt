package cl.duocuc.segurosbpo.model

enum class EtapaSiniestro(val etiqueta: String) {
    RECIBIDO("Recibido"),
    EVALUACION("En evaluación"),
    LIQUIDACION("En liquidación"),
    CERRADO("Cerrado")
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
    val mensajeCierre: String? = null
)
