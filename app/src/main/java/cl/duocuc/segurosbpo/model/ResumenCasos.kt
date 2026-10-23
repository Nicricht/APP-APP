package cl.duocuc.segurosbpo.model

import androidx.compose.runtime.Immutable

@Immutable
data class ResumenCasos(
    val recibidos: Int,
    val evaluacion: Int,
    val liquidacion: Int,
    val cerrados: Int
) {
    val total: Int get() = recibidos + evaluacion + liquidacion + cerrados
    val abiertos: Int get() = total - cerrados

    companion object {
        fun desde(casos: List<Siniestro>): ResumenCasos = ResumenCasos(
            recibidos = casos.count { it.etapa == EtapaSiniestro.RECIBIDO },
            evaluacion = casos.count { it.etapa == EtapaSiniestro.EVALUACION },
            liquidacion = casos.count { it.etapa == EtapaSiniestro.LIQUIDACION },
            cerrados = casos.count { it.etapa == EtapaSiniestro.CERRADO }
        )
    }
}
