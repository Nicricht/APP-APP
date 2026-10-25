package cl.duocuc.segurosbpo.model


data class Evidencia(
    val id: Long,
    val siniestroId: Long,
    val tipoDocumento: String,
    val nombreArchivo: String,
    val tipoMime: String,
    val tamanoBytes: Int,
    val fechaRegistro: String
)

object DocumentosLiquidador {
    val tipos = listOf(
        "Informe de liquidación",
        "Acta de inspección",
        "Presupuesto de reparación",
        "Registro fotográfico del perito"
    )
}
