package br.com.salus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.salus.ui.theme.SalusTheme
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Bem-vindo(a) à salus!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SalusTheme {
        Greeting("Android")
    }
}

// Exemplo em uma LoginViewModel
class LoginViewModel : ViewModel() {

    // LiveData ou StateFlow para comunicar com a UI
    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> get() = _loginResult

    fun fazerLogin(email: String, senha: String) {
        // Inicia uma Coroutine no escopo do ViewModel
        viewModelScope.launch {
            try {
                val resposta = NetworkManager.userSignIn(email, senha)

                if (resposta.sucesso) {
                    _loginResult.postValue("Login bem-sucedido!")
                    // Aqui você navegaria para a próxima tela
                } else {
                    _loginResult.postValue("Falha no login: ${resposta.mensagem}")
                }
            } catch (e: Exception) {
                _loginResult.postValue("Erro: ${e.message}")
            }
        }
    }
}

// Exemplo na sua UI (Composable)
@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel()) {
    val loginMessage by viewModel.loginResult.observeAsState()

    // ... campos de email e senha ...

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = {
            // A ViewModel vai lidar com a Coroutine
            viewModel.fazerLogin("dududuefu.gomes@gmail.com", "Senhaforte123@")
        }) {
            Text("Entrar")
        }

        loginMessage?.let {
            Text(it)
        }
    }
}