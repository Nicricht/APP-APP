package cl.duocuc.segurosbpo.model

//catalogos del caso de siniestros (Anexo 9) - todos los datos son ficticios

//los 8 asegurados del Anexo 9 (identificacion simulada, S-02)
//el rutOculto es el que se muestra en pantalla (RNF-10)
enum class Asegurado(val etiqueta: String, val rut: String, val rutOculto: String){
    CAMILA("Camila Rojas Fuentes", "15.842.371-5", "**.***.371-5"),
    JORGE("Jorge Pizarro Contreras", "9.418.552-1", "*.***.552-1"),
    VALENTINA("Valentina Soto Munoz", "19.205.884-8", "**.***.884-8"),
    HECTOR("Hector Araya Diaz", "7.836.219-7", "*.***.219-7"),
    DANIELA("Daniela Vergara Lagos", "17.552.093-7", "**.***.093-7"),
    RICARDO("Ricardo Espinoza Tapia", "13.664.708-3", "**.***.708-3"),
    FRANCISCA("Francisca Morales Henriquez", "20.118.465-7", "**.***.465-7"),
    LUIS("Luis Carrasco Pena", "11.290.337-2", "**.***.337-2")
}

enum class Compania(val etiqueta: String, val activa: Boolean){
    CORDILLERA("Seguros Cordillera", true),
    LITORAL("Seguros Litoral", true),
    PATAGONIA("Aseguradora Patagonia Sur", true),
    VALLE_CENTRAL("Seguros Valle Central", true),
    //RN-18: contrato terminado, se puede consultar pero no reportar
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
    //quien reporta (se elige, no se escribe)
    val asegurado: Asegurado = Asegurado.CAMILA,
    //variables para alamacenar informacion de campos de texto
    val fechaOcurrencia: String = "",
    val comuna: String = "",
    val descripcion: String = "",
    //opciones que se eligen
    val compania: Compania = Compania.CORDILLERA,
    val tipoSiniestro: TipoSiniestro = TipoSiniestro.CHOQUE,
    val huboTerceros: Boolean = false,
    val datosCorrectos: Boolean = false,
    //mensajes de error bajo cada campo
    val errorFecha: String? = null,
    val errorComuna: String? = null,
    val errorDescripcion: String? = null,
    val errorCompania: String? = null,
    val errorDatos: String? = null,
    //resultado
    val reporteExitoso: Boolean = false,
    val numeroSiniestro: String = "",
    //RN-06: correlativo del numero de siniestro, sigue a los datos de ejemplo (SIN-26-004860)
    val correlativo: Int = 4861

){

}
