package cl.duocuc.miregistro.ui.style

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import cl.duocuc.miregistro.ui.theme.Blanco
import cl.duocuc.miregistro.ui.theme.Dimens
import cl.duocuc.miregistro.ui.theme.GrisDeshabilitado
import cl.duocuc.miregistro.ui.theme.Primario

object EstilosBotones {
    val forma: Shape = RoundedCornerShape(Dimens.radioBoton) //border-radius
    val relleno = PaddingValues(horizontal = 20.dp, vertical = 12.dp) //padding

    //botones principales: relleno morado, texto blanco
    @Composable
    fun colorPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Primario, //background-color
        contentColor = Blanco, //color
        disabledContainerColor = GrisDeshabilitado,
        disabledContentColor = Blanco
    )

    //botones secundarios: transparentes, texto primario del tema
    @Composable
    fun coloresSecundarios(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )

    //bordes de los botones secundarios
    @Composable
    fun bordeSecundario(): BorderStroke = BorderStroke(
        Dimens.bordeBoton,
        MaterialTheme.colorScheme.primary
    )

    //sombra del boton principal (se hunde al presionar)
    @Composable
    fun elevacion(): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = Dimens.elevacionBoton,
        pressedElevation = 1.dp
    )
}

//Clase CSS

fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.alturaBoton)