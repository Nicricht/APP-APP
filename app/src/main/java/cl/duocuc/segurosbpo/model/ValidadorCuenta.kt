package cl.duocuc.segurosbpo.model

data class ErroresRegistro(
    val nombre: String? = null,
    val rut: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val clave: String? = null,
    val confirmacion: String? = null
) {
    fun primerError(): String? = nombre ?: rut ?: correo ?: telefono ?: clave ?: confirmacion
}

object ValidadorCuenta {
    fun normalizarCorreo(correo: String) = correo.trim().lowercase()

    fun rutValido(rut: String): Boolean {
        val limpio = rut.trim().uppercase().replace(".", "")
        val partes = limpio.split("-")
        if (partes.size != 2 || !partes[0].matches(Regex("[0-9]{7,8}")) ||
            !partes[1].matches(Regex("[0-9K]"))) return false
        var suma = 0
        var factor = 2
        for (digito in partes[0].reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        val resultado = 11 - suma % 11
        val dv = when (resultado) {
            11 -> "0"
            10 -> "K"
            else -> resultado.toString()
        }
        return partes[1] == dv
    }

    fun erroresRegistro(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        clave: String,
        confirmacion: String
    ): ErroresRegistro = ErroresRegistro(
        nombre = if (nombre.trim().length < 3) "Ingresa tu nombre completo" else null,
        rut = if (!rutValido(rut)) "Ingresa un RUT válido" else null,
        correo = if (!Regex("^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$")
                .matches(normalizarCorreo(correo))) "Ingresa un correo válido" else null,
        telefono = if (telefono.trim().length < 8) "Ingresa un teléfono válido" else null,
        clave = if (clave.length < 8)
            "La contraseña debe tener al menos 8 caracteres" else null,
        confirmacion = if (clave != confirmacion) "Las claves no coinciden" else null
    )

    fun errorRegistro(
        nombre: String,
        rut: String,
        correo: String,
        telefono: String,
        clave: String,
        confirmacion: String
    ): String? = erroresRegistro(nombre, rut, correo, telefono, clave, confirmacion).primerError()
}
