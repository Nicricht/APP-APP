package cl.duocuc.segurosbpo.model

data class Consulta(
    val id: Long,
    val siniestroId: Long,
    val pregunta: String,
    val respuesta: String?,
    val fechaPregunta: String,
    val fechaRespuesta: String?
) {
    val respondida: Boolean get() = respuesta != null
}

data class EvaluacionCierre(
    val siniestroId: Long,
    val nota: Int,
    val comentario: String?,
    val fecha: String
)

object ValidadorConsulta {
    const val MAX_PREGUNTA = 500
    const val MAX_RESPUESTA = 500

    fun errorPregunta(pregunta: String): String? = when {
        pregunta.isBlank() -> "Escribe tu consulta"
        pregunta.length > MAX_PREGUNTA -> "La consulta supera los $MAX_PREGUNTA caracteres"
        else -> null
    }

    fun errorRespuesta(respuesta: String): String? = when {
        respuesta.isBlank() -> "Escribe la respuesta"
        respuesta.length > MAX_RESPUESTA -> "La respuesta supera los $MAX_RESPUESTA caracteres"
        else -> null
    }
}

object ValidadorEvaluacion {
    fun error(nota: Int, comentario: String): String? = when {
        nota !in 1..5 -> "Elige una nota de 1 a 5"
        comentario.length > 500 -> "El comentario supera los 500 caracteres"
        else -> null
    }
}

object ValidadorDesistimiento {
    fun error(etapa: EtapaSiniestro, motivo: String): String? = when {
        etapa != EtapaSiniestro.RECIBIDO && etapa != EtapaSiniestro.EVALUACION ->
            "Solo puedes desistir mientras el caso está en Recibido o En evaluación"
        motivo.isBlank() -> "Indica el motivo del desistimiento"
        motivo.length > 500 -> "El motivo supera los 500 caracteres"
        else -> null
    }
}
