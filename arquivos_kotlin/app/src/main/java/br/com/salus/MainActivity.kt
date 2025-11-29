package br.com.salus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // Importe o ViewModel
import br.com.salus.ui.screens.ProfileScreen // Importe a nova tela
import br.com.salus.ui.screens.ProfileViewModel // Importe o ViewModel
import br.com.salus.ui.theme.SalusTheme // Seu tema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusTheme {
                // Inicializa o ViewModel
                val viewModel: ProfileViewModel = viewModel()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        viewModel = viewModel,
                        onLogout = {
                            // Lógica para deslogar e navegar para outra tela
                            finish()
                        }
                    )
                }
            }
        }
    }
}

// Remova as funções Greeting e GreetingPreview se elas existirem no seu arquivo.
