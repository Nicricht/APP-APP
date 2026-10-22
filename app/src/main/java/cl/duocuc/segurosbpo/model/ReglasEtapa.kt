package cl.duocuc.segurosbpo.model

object ReglasEtapa {
    fun siguiente(actual: EtapaSiniestro): EtapaSiniestro? = when (actual) {
        EtapaSiniestro.RECIBIDO -> EtapaSiniestro.EVALUACION
        EtapaSiniestro.EVALUACION -> EtapaSiniestro.LIQUIDACION
        EtapaSiniestro.LIQUIDACION -> EtapaSiniestro.CERRADO
        EtapaSiniestro.CERRADO -> null
    }

    fun validar(
        actual: EtapaSiniestro,
        destino: EtapaSiniestro,
        pendientes: Int,
        resultado: String,
        mensajeCierre: String
    ): String? {
        val cierreAnticipado = destino == EtapaSiniestro.CERRADO &&
            actual in setOf(EtapaSiniestro.RECIBIDO, EtapaSiniestro.EVALUACION)
        if (siguiente(actual) != destino && !cierreAnticipado) {
            return "No está permitido saltar ni retroceder etapas"
        }
        if (destino == EtapaSiniestro.LIQUIDACION && pendientes > 0) {
            return "Primero debes recibir los documentos pendientes"
        }
        if (destino == EtapaSiniestro.CERRADO) {
            val permitidos = when (actual) {
                EtapaSiniestro.RECIBIDO -> setOf("Desistido")
                EtapaSiniestro.EVALUACION -> setOf("Rechazado", "Desistido")
                EtapaSiniestro.LIQUIDACION -> setOf("Aprobado", "Rechazado")
                EtapaSiniestro.CERRADO -> emptySet()
            }
            if (resultado !in permitidos) {
                return "Resultado de cierre no permitido para esta etapa"
            }
            if (mensajeCierre.isBlank()) {
                return "El mensaje de cierre es obligatorio"
            }
        }
        return null
    }

    fun validarGestion(descripcion: String): String? = when {
        descripcion.isBlank() -> "La descripción de la gestión es obligatoria"
        descripcion.length > 500 -> "La descripción no puede superar 500 caracteres"
        else -> null
    }
}
