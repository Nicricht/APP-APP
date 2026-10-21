package cl.duocuc.segurosbpo.navigation

sealed class Ruta(val ruta: String) {
    data object MisSiniestros : Ruta("mis_siniestros")
    data object Reportar : Ruta("reportar")
    data object Detalle : Ruta("detalle/{id}") {
        fun crear(id: Long) = "detalle/$id"
    }
    data object Evidencias : Ruta("evidencias/{id}") {
        fun crear(id: Long) = "evidencias/$id"
    }
    data object Editar : Ruta("editar/{id}") {
        fun crear(id: Long) = "editar/$id"
    }
}
