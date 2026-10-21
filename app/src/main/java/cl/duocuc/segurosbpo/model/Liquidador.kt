package cl.duocuc.segurosbpo.model

/** Identidades ficticias para la demostración local, sin login real. */
enum class Liquidador(val nombre: String, val equipo: String, val esVehiculo: Boolean) {
    VEHICULOS("María P.", "Vehículos", true),
    HOGAR("Pedro C.", "Hogar", false)
}
