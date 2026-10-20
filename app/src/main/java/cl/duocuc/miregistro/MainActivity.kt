package cl.duocuc.miregistro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cl.duocuc.miregistro.ui.screens.FormularioScreen
import cl.duocuc.miregistro.ui.theme.MiRegistroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiRegistroTheme {
                FormularioScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MiRegistroTheme {
        FormularioScreen()
    }
}