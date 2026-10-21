package cl.duocuc.segurosbpo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duocuc.segurosbpo.navigation.AppNavegacion
import cl.duocuc.segurosbpo.ui.theme.SegurosBpoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SegurosBpoTheme { AppNavegacion() } }
    }
}
