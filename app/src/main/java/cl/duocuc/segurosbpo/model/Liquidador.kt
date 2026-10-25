package cl.duocuc.segurosbpo.model


enum class Liquidador(val nombre: String, val equipo: String, val esVehiculo: Boolean) {
    VEHICULOS("María P.", "Vehículos", true),
    HOGAR("Pedro C.", "Hogar", false)
}

fun etiquetaEquipo(codigo: String?): String =
    Liquidador.entries.firstOrNull { it.name == codigo }?.equipo ?: "sin equipo"
