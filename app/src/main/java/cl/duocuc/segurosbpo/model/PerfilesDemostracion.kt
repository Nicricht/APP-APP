package cl.duocuc.segurosbpo.model

/** Identidades ficticias del caso académico: no requieren ni almacenan credenciales. */
object PerfilesDemostracion {
    val todos: List<CuentaLocal> = Asegurado.entries.map { asegurado ->
        CuentaLocal(
            id = asegurado.name,
            nombre = asegurado.etiqueta,
            rut = asegurado.rut,
            correo = "",
            telefono = "",
            rol = RolCuenta.ASEGURADO,
            equipo = null
        )
    } + Liquidador.entries.map { liquidador ->
        CuentaLocal(
            id = "LIQ_${liquidador.name}",
            nombre = liquidador.nombre,
            rut = "",
            correo = "",
            telefono = "",
            rol = RolCuenta.LIQUIDADOR,
            equipo = liquidador.name
        )
    }

    fun porId(id: String): CuentaLocal? = todos.firstOrNull { it.id == id }
}
