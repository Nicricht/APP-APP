package cl.duocuc.segurosbpo.model

/** Mantiene legibles los movimientos creados por versiones anteriores de la app. */
object TextoPresentacion {
    fun responsable(valor: String): String =
        if (valor == "Ejemplo académico") "Sistema" else valor

    fun gestion(valor: String): String = when {
        valor.startsWith("Caso ficticio disponible en etapa: ") ->
            valor.replaceFirst("Caso ficticio disponible en etapa: ", "Caso registrado en etapa: ")
        valor.startsWith("Documento solicitado entregado localmente: ") ->
            valor.replaceFirst("Documento solicitado entregado localmente: ", "Documento recibido: ")
        else -> valor
    }
}
