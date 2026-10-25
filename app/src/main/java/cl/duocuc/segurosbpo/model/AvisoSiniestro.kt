package cl.duocuc.segurosbpo.model

data class AvisoSiniestro(
    val id: Long,
    val siniestroId: Long,
    val numeroSiniestro: String,
    val mensaje: String,
    val fecha: String,
    val leido: Boolean,
    val solicitudId: Long? = null,
    val documentoPedido: String? = null
)
