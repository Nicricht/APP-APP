package cl.duocuc.segurosbpo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    primaryContainer = PrimarioOscuro,
    onPrimaryContainer = Blanco,
    secondary = PrimarioClaro,
    onSecondary = TextoOscuro,
    tertiary = Acento,
    onTertiary = TextoOscuro,
    background = FondoOscuro,
    onBackground = TextoClaro,
    surface = SuperficieOscura,
    onSurface = TextoClaro
)

private val LightColorScheme = lightColorScheme(
    primary = Primario,
    onPrimary = Blanco,
    primaryContainer = PrimarioClaro,
    onPrimaryContainer = PrimarioOscuro,
    secondary = Secundario,
    onSecondary = Blanco,
    tertiary = Acento,
    onTertiary = Blanco,
    background = FondoClaro,
    onBackground = TextoOscuro,
    surface = SuperficieClara,
    onSurface = TextoOscuro

)


internal fun admiteColoresDinamicos(solicitado: Boolean, sdk: Int): Boolean =
    solicitado && sdk >= Build.VERSION_CODES.S

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun SegurosBpoTheme(content: @Composable () -> Unit) {
    SegurosBpoTheme(isSystemInDarkTheme(), true, content)
}

@Composable
fun SegurosBpoTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    SegurosBpoTheme(darkTheme, true, content)
}

@Composable
fun SegurosBpoTheme(
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    SegurosBpoTheme(isSystemInDarkTheme(), dynamicColor, content)
}

@Composable
fun SegurosBpoTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        admiteColoresDinamicos(dynamicColor, Build.VERSION.SDK_INT) -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Tipografia,
        shapes = Formas,
        content = content
    )
}