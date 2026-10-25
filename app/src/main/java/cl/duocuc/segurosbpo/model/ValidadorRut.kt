package cl.duocuc.segurosbpo.model

object ValidadorRut {

    fun normalizar(rut: String): String =
        rut.trim().uppercase().replace(".", "").replace("-", "")

    fun valido(rut: String): Boolean {
        val limpio = normalizar(rut)
        if (limpio.length < 2) return false
        val cuerpo = limpio.dropLast(1)
        val verificador = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return false
        if (!verificador.isDigit() && verificador != 'K') return false
        return calcularVerificador(cuerpo) == verificador
    }

    fun calcularVerificador(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        cuerpo.reversed().forEach { digito ->
            suma += Character.getNumericValue(digito) * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }

    fun error(rut: String): String? = when {
        rut.isBlank() -> "Indica el RUT"
        !valido(rut) -> "El RUT no es válido"
        else -> null
    }

    fun ocultar(rut: String): String {
        val limpio = normalizar(rut)
        if (limpio.length < 5) return "***"
        return "*".repeat(limpio.length - 4) + limpio.takeLast(4)
    }
}
