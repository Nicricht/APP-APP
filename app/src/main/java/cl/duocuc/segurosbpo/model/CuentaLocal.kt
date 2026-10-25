package cl.duocuc.segurosbpo.model

enum class RolCuenta {
    ASEGURADO,
    LIQUIDADOR
}

data class CuentaLocal(
    val id: String,
    val nombre: String,
    val rut: String,
    val correo: String,
    val telefono: String,
    val rol: RolCuenta,
    val equipo: String?,
    val foto: ByteArray? = null
)

fun CuentaLocal.comoAsegurado(): Asegurado = Asegurado(
    etiqueta = nombre,
    rut = rut,
    rutOculto = "****" + rut.takeLast(4),
    name = id
)
