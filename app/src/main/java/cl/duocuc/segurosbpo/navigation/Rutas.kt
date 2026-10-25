package cl.duocuc.segurosbpo.navigation

sealed class Ruta(val ruta: String) {
    data object MisSiniestros : Ruta("mis_siniestros")
    data object Reportar : Ruta("reportar")
    data object Avisos : Ruta("avisos")
    data object Estadisticas : Ruta("estadisticas")
    data object Perfil : Ruta("perfil")
    data object EditarPerfil : Ruta("editar_perfil")
    data object Liquidador : Ruta("liquidador")
    data object Disponibles : Ruta("disponibles")
    data object EstadisticasLiquidador : Ruta("estadisticas_liquidador")
    data object Analista : Ruta("analista")
    data object CargaEquipos : Ruta("carga_equipos")
    data object DocumentosLiquidador : Ruta("documentos_liquidador/{id}") {
        fun crear(id: Long) = "documentos_liquidador/$id"
    }
    data object GestionCaso : Ruta("gestion_caso/{id}") {
        fun crear(id: Long) = "gestion_caso/$id"
    }
    data object Detalle : Ruta("detalle/{id}") {
        fun crear(id: Long) = "detalle/$id"
    }
    data object Historial : Ruta("historial/{id}") {
        fun crear(id: Long) = "historial/$id"
    }
    data object Evidencias : Ruta("evidencias/{id}") {
        fun crear(id: Long) = "evidencias/$id"
    }
    data object Consultas : Ruta("consultas/{id}") {
        fun crear(id: Long) = "consultas/$id"
    }
    data object Editar : Ruta("editar/{id}") {
        fun crear(id: Long) = "editar/$id"
    }
}
