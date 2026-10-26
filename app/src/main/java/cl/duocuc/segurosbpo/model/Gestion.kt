package cl.duocuc.segurosbpo.model

data class Gestion(
    val id: Long,
    val siniestroId: Long,
    val fecha: String,
    val responsable: String,
    val descripcion: String,
    val visibleAsegurado: Boolean,
    val autor: String = "SISTEMA"
)
