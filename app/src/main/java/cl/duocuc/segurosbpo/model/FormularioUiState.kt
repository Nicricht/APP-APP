package cl.duocuc.segurosbpo.model


data class Asegurado(
    val etiqueta: String,
    val rut: String,
    val rutOculto: String,
    val name: String
) {
    companion object {
        val CAMILA = Asegurado("Camila Rojas Fuentes", "15.842.371-5", "**.***.371-5", "CAMILA")
        val JORGE = Asegurado("Jorge Pizarro Contreras", "9.418.552-1", "*.***.552-1", "JORGE")
        val VALENTINA = Asegurado("Valentina Soto Munoz", "19.205.884-8", "**.***.884-8", "VALENTINA")
        val HECTOR = Asegurado("Hector Araya Diaz", "7.836.219-7", "*.***.219-7", "HECTOR")
        val DANIELA = Asegurado("Daniela Vergara Lagos", "17.552.093-7", "**.***.093-7", "DANIELA")
        val RICARDO = Asegurado("Ricardo Espinoza Tapia", "13.664.708-3", "**.***.708-3", "RICARDO")
        val FRANCISCA = Asegurado("Francisca Morales Henriquez", "20.118.465-7", "**.***.465-7", "FRANCISCA")
        val LUIS = Asegurado("Luis Carrasco Pena", "11.290.337-2", "**.***.337-2", "LUIS")
        val entries: List<Asegurado> = listOf(
            CAMILA, JORGE, VALENTINA, HECTOR, DANIELA, RICARDO, FRANCISCA, LUIS
        )

        fun valueOf(codigo: String): Asegurado =
            entries.firstOrNull { it.name == codigo }
                ?: Asegurado("Asegurado", "", "***", codigo)
    }
}

enum class Compania(val etiqueta: String, val activa: Boolean){
    CORDILLERA("Seguros Cordillera", true),
    LITORAL("Seguros Litoral", true),
    PATAGONIA("Aseguradora Patagonia Sur", true),
    VALLE_CENTRAL("Seguros Valle Central", true),

    PROTEGE("Protege Seguros (no disponible)", false)
}

enum class TipoSiniestro(val etiqueta: String, val esVehiculo: Boolean){
    CHOQUE("Vehículo · choque o colisión", true),
    ROBO_VEHICULO("Vehículo · robo", true),
    CRISTALES("Vehículo · rotura de cristales", true),
    DANO_AGUA("Hogar · daño por agua", false),
    ROBO_HOGAR("Hogar · robo", false),
    INCENDIO("Hogar · incendio", false)
}

data class FormularioUiState(

    val asegurado: Asegurado = Asegurado.CAMILA,

    val fechaOcurrencia: String = "",
    val comuna: String = "",
    val descripcion: String = "",

    val compania: Compania = Compania.CORDILLERA,
    val tipoSiniestro: TipoSiniestro = TipoSiniestro.CHOQUE,
    val huboTerceros: Boolean = false,
    val datosCorrectos: Boolean = false,

    val errorFecha: String? = null,
    val errorComuna: String? = null,
    val errorDescripcion: String? = null,
    val errorCompania: String? = null,
    val errorDatos: String? = null,

    val reporteExitoso: Boolean = false,
    val numeroSiniestro: String = ""

){

}
