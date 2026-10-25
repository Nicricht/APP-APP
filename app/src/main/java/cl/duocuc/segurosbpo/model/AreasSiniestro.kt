package cl.duocuc.segurosbpo.model

sealed interface ClasificadorArea {
    val nombre: String
    val tipos: Set<TipoSiniestro>
    fun admite(tipo: TipoSiniestro): Boolean
}

data object AreaVehiculos : ClasificadorArea {
    override val nombre = "Vehículos"
    override val tipos: Set<TipoSiniestro>
        get() = TipoSiniestro.entries.filter { it.esVehiculo }.toSet()

    override fun admite(tipo: TipoSiniestro): Boolean = tipo.esVehiculo
}

data object AreaHogar : ClasificadorArea {
    override val nombre = "Hogar"
    override val tipos: Set<TipoSiniestro>
        get() = TipoSiniestro.entries.filter { !it.esVehiculo }.toSet()

    override fun admite(tipo: TipoSiniestro): Boolean = !tipo.esVehiculo
}

object AreasSiniestro {
    fun paraLiquidador(liquidador: Liquidador): ClasificadorArea =
        if (liquidador.esVehiculo) AreaVehiculos else AreaHogar

    fun paraTipo(tipo: TipoSiniestro): ClasificadorArea =
        if (tipo.esVehiculo) AreaVehiculos else AreaHogar
}
