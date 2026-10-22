package cl.duocuc.segurosbpo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.duocuc.segurosbpo.model.ResumenCasos
import cl.duocuc.segurosbpo.model.Siniestro

@Composable
fun ResumenGraficosCasos(
    casos: List<Siniestro>,
    equipo: String,
    modifier: Modifier = Modifier
) {
    val resumen = remember(casos) { ResumenCasos.desde(casos) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Resumen del equipo " + equipo, style = MaterialTheme.typography.titleMedium)
            Text(
                "Casos asignados a este liquidador: " + resumen.total,
                style = MaterialTheme.typography.bodyMedium
            )
            if (resumen.total == 0) {
                Text("No hay casos asignados para mostrar en los gráficos.")
            } else {
                Text("Casos por etapa", style = MaterialTheme.typography.titleSmall)
                GraficoEtapas(resumen)
                Text("Abiertos y cerrados", style = MaterialTheme.typography.titleSmall)
                GraficoSituacion(resumen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TarjetaEstadistica(resumen.total.toString(), "Total", Modifier.weight(1f))
                    TarjetaEstadistica(resumen.abiertos.toString(), "Abiertos", Modifier.weight(1f))
                    TarjetaEstadistica(resumen.cerrados.toString(), "Cerrados", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GraficoEtapas(resumen: ResumenCasos) {
    val etiquetas = listOf("Recib.", "Evaluac.", "Liquid.", "Cerrado")
    val valores = listOf(
        resumen.recibidos,
        resumen.evaluacion,
        resumen.liquidacion,
        resumen.cerrados
    )
    val maximo = valores.maxOrNull()?.coerceAtLeast(1) ?: 1
    val progreso = remember(valores) { Animatable(0f) }
    LaunchedEffect(valores) { progreso.animateTo(1f, tween(650)) }
    val colorBarra = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        valores.forEachIndexed { indice, valor ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(valor.toString(), style = MaterialTheme.typography.labelLarge)
                Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                    val alto = size.height * valor.toFloat() / maximo * progreso.value
                    val ancho = size.width * 0.68f
                    drawRoundRect(
                        color = colorBarra,
                        topLeft = Offset((size.width - ancho) / 2f, size.height - alto),
                        size = Size(ancho, alto),
                        cornerRadius = CornerRadius(5.dp.toPx())
                    )
                }
                Text(
                    etiquetas[indice],
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GraficoSituacion(resumen: ResumenCasos) {
    val abiertos = resumen.abiertos
    val cerrados = resumen.cerrados
    val colorAbierto = MaterialTheme.colorScheme.primary
    val colorCerrado = MaterialTheme.colorScheme.tertiary
    val progreso = remember(abiertos, cerrados) { Animatable(0f) }
    LaunchedEffect(abiertos, cerrados) { progreso.animateTo(1f, tween(650)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val barrido = 360f * progreso.value
            val barridoAbiertos = barrido * abiertos / resumen.total
            val grosor = 24.dp.toPx()
            val esquina = Offset(grosor / 2f, grosor / 2f)
            val area = Size(size.width - grosor, size.height - grosor)
            drawArc(
                color = colorAbierto,
                startAngle = -90f,
                sweepAngle = barridoAbiertos,
                useCenter = false,
                topLeft = esquina,
                size = area,
                style = Stroke(width = grosor)
            )
            drawArc(
                color = colorCerrado,
                startAngle = -90f + barridoAbiertos,
                sweepAngle = barrido - barridoAbiertos,
                useCenter = false,
                topLeft = esquina,
                size = area,
                style = Stroke(width = grosor)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Abiertos: " + abiertos, color = colorAbierto)
            Text("Cerrados: " + cerrados, color = MaterialTheme.colorScheme.onSurface)
            Text("Total: " + resumen.total, style = MaterialTheme.typography.labelLarge)
        }
    }
}
