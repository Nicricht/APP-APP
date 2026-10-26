package cl.duocuc.segurosbpo.model


enum class Liquidador(val nombre: String, val equipo: String, val esVehiculo: Boolean) {
    VEHICULOS("Equipo de Vehículos", "Vehículos", true),
    HOGAR("Equipo de Hogar", "Hogar", false)
}

fun etiquetaEquipo(codigo: String?): String =
    Liquidador.entries.firstOrNull { it.name == codigo }?.equipo ?: "sin equipo"

fun nombreLiquidadorPublico(nombre: String): String {
    val partes = nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return if (partes.size >= 2) "${partes[0]} ${partes[1][0].uppercaseChar()}."
        else nombre.trim()
}
