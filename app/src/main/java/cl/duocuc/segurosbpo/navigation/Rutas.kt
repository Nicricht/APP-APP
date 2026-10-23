package cl.duocuc.segurosbpo.navigation

sealed class Ruta(val ruta: String) {
    data object MisSiniestros : Ruta("mis_siniestros")
    data object Reportar : Ruta("reportar")
    data object Avisos : Ruta("avisos")
    data object Estadisticas : Ruta("estadisticas")
    data object Liquidador : Ruta("liquidador")
    data object Detalle : Ruta("detalle/{id}") {
        fun crear(id: Long) = "detalle/$id"
    }
    data object Historial : Ruta("historial/{id}") {
        fun crear(id: Long) = "historial/$id"
    }
    data object Evidencias : Ruta("evidencias/{id}") {
        fun crear(id: Long) = "evidencias/$id"
    }
    data object Editar : Ruta("editar/{id}") {
        fun crear(id: Long) = "editar/$id"
    }
}
