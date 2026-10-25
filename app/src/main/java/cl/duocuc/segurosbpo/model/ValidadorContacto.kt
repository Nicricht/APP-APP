package cl.duocuc.segurosbpo.model

object ValidadorContacto {
    private const val USUARIO = """[a-z0-9]([a-z0-9._%+-]*[a-z0-9])?"""
    private const val DOMINIO = """[a-z0-9]([a-z0-9-]*[a-z0-9])?(\.[a-z0-9-]+)*\.[a-z]{2,6}"""
    private val CORREO = Regex(USUARIO + "@" + DOMINIO)

    fun normalizarCorreo(correo: String): String = correo.trim().lowercase()

    fun normalizarTelefono(telefono: String): String =
        telefono.trim().replace(" ", "").replace("-", "").replace(".", "")

    fun errorCorreo(correo: String): String? {
        val limpio = normalizarCorreo(correo)
        return when {
            limpio.isBlank() -> "Indica tu correo electrónico"
            limpio.length > 80 -> "El correo supera los 80 caracteres"
            limpio.contains("..") -> "El correo no puede tener dos puntos seguidos"
            !CORREO.matches(limpio) -> "Escribe un correo válido, por ejemplo nombre@correo.cl"
            else -> null
        }
    }

    fun errorTelefono(telefono: String): String? {
        val limpio = normalizarTelefono(telefono)
        val digitos = limpio.removePrefix("+56")
        return when {
            limpio.isBlank() -> "Indica un teléfono de contacto"
            limpio.startsWith("+") && !limpio.startsWith("+56") ->
                "Usa un número chileno (+56) o escríbelo sin prefijo"
            digitos.any { !it.isDigit() } -> "El teléfono solo admite números"
            digitos.length < 8 -> "El teléfono debe tener al menos 8 dígitos"
            digitos.length > 11 -> "El teléfono no puede superar los 11 dígitos"
            else -> null
        }
    }
}
