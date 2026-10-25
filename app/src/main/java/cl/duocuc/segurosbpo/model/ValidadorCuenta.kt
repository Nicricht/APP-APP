package cl.duocuc.segurosbpo.model

object ValidadorCuenta {
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

}
