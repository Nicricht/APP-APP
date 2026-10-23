package cl.duocuc.segurosbpo.model

sealed interface ClasificadorArea {
    val nombre: String
    val tipos: Set<TipoSiniestro>
    fun admite(tipo: TipoSiniestro): Boolean
}

data object AreaVehiculos : ClasificadorArea {
    override val nombre = "Vehículos"
    override val tipos = setOf(
        TipoSiniestro.CHOQUE,
        TipoSiniestro.ROBO_VEHICULO,
        TipoSiniestro.CRISTALES
    )
    override fun admite(tipo: TipoSiniestro): Boolean = tipo in tipos
}

data object AreaHogar : ClasificadorArea {
    override val nombre = "Hogar"
    override val tipos = setOf(
        TipoSiniestro.DANO_AGUA,
        TipoSiniestro.ROBO_HOGAR,
        TipoSiniestro.INCENDIO
    )
    override fun admite(tipo: TipoSiniestro): Boolean = tipo in tipos
}

object AreasSiniestro {
    fun paraLiquidador(liquidador: Liquidador): ClasificadorArea =
        if (liquidador.esVehiculo) AreaVehiculos else AreaHogar

    fun paraTipo(tipo: TipoSiniestro): ClasificadorArea =
        if (AreaVehiculos.admite(tipo)) AreaVehiculos else AreaHogar
}
