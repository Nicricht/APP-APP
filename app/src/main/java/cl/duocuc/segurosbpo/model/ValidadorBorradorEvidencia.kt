package cl.duocuc.segurosbpo.model

object ValidadorBorradorEvidencia {
    fun puedeGuardar(tipo: String, archivoSeleccionado: Boolean, total: Int): Boolean =
        tipo.isNotBlank() && archivoSeleccionado && total < 15
}
