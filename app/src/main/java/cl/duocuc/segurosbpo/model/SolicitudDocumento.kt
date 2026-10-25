package cl.duocuc.segurosbpo.model

data class SolicitudDocumento(
    val id: Long,
    val siniestroId: Long,
    val tipo: String,
    val comentario: String,
    val fechaSolicitud: String,
    val fechaEntrega: String?
) {
    val pendiente: Boolean get() = fechaEntrega == null
}
