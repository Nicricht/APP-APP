package cl.duocuc.segurosbpo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cl.duocuc.segurosbpo.navigation.AppNavegacion
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencias = getSharedPreferences("tema_local", MODE_PRIVATE)
        setContent {
            val sistemaOscuro = isSystemInDarkTheme()
            var temaOscuro by rememberSaveable {
                mutableStateOf(preferencias.getBoolean("oscuro", sistemaOscuro))
            }
            SegurosBpoTheme(darkTheme = temaOscuro) {
                AppNavegacion(
                    temaOscuro = temaOscuro,
                    onAlternarTema = {
                        temaOscuro = !temaOscuro
                        preferencias.edit().putBoolean("oscuro", temaOscuro).apply()
                    }
                )
            }
        }
    }
}
