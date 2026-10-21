package cl.duocuc.segurosbpo.model

/** Archivo entregado por el asegurado. Los bytes se guardan en SQLite. */
data class Evidencia(
    val id: Long,
    val siniestroId: Long,
    val tipoDocumento: String,
    val nombreArchivo: String,
    val tipoMime: String,
    val tamanoBytes: Int,
    val fechaRegistro: String
)
