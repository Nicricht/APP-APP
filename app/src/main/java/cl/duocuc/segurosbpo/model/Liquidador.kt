package cl.duocuc.segurosbpo.model


enum class Liquidador(val nombre: String, val equipo: String, val esVehiculo: Boolean) {
    VEHICULOS("Equipo de Vehículos", "Vehículos", true),
    HOGAR("Equipo de Hogar", "Hogar", false)
}

fun etiquetaEquipo(codigo: String?): String =
    Liquidador.entries.firstOrNull { it.name == codigo }?.equipo ?: "sin equipo"
