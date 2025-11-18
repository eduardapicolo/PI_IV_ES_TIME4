package br.com.salus

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EntrarCompeticaoViewModel : ViewModel() {
    companion object {
        private const val TAG = "EntrarCompeticaoVM"
    }

    private val _codigo = MutableStateFlow("")
    val codigo: StateFlow<String> = _codigo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensagemSucesso = MutableStateFlow<String?>(null)
    val mensagemSucesso: StateFlow<String?> = _mensagemSucesso

    private val _mensagemErro = MutableStateFlow<String?>(null)
    val mensagemErro: StateFlow<String?> = _mensagemErro

    private val _nomeCompeticao = MutableStateFlow<String?>(null)
    val nomeCompeticao: StateFlow<String?> = _nomeCompeticao

    fun atualizarCodigo(novoCodigo: String) {
        // Permitir apenas letras e números, máximo 6 caracteres
        val codigoLimpo = novoCodigo.uppercase().filter { it.isLetterOrDigit() }.take(6)
        _codigo.value = codigoLimpo
    }

    fun entrarNaCompeticao(userId: String) {
        if (_codigo.value.length != 6) {
            _mensagemErro.value = "O código deve ter 6 caracteres"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _mensagemErro.value = null
            _mensagemSucesso.value = null
            _nomeCompeticao.value = null

            try {
                Log.d(TAG, "Entrando na competição com código: ${_codigo.value}")
                val resposta = NetworkManager.entrarNaCompeticao(_codigo.value, userId)

                Log.d(TAG, "Resposta:")
                Log.d(TAG, "  - Sucesso: ${resposta.sucesso}")
                Log.d(TAG, "  - Mensagem: ${resposta.mensagem}")
                Log.d(TAG, "  - Nome: ${resposta.nomeCompeticao}")

                if (resposta.sucesso && resposta.nomeCompeticao != null) {
                    Log.d(TAG, "✅ Entrada bem-sucedida!")
                    _mensagemSucesso.value = resposta.mensagem
                    _nomeCompeticao.value = resposta.nomeCompeticao
                    _codigo.value = "" // Limpa o campo
                } else {
                    Log.w(TAG, "⚠️ Falha: ${resposta.mensagem}")
                    _mensagemErro.value = resposta.mensagem
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao entrar na competição", e)
                _mensagemErro.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limparMensagens() {
        _mensagemErro.value = null
        _mensagemSucesso.value = null
        _nomeCompeticao.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrarCompeticaoScreen(
    viewModel: EntrarCompeticaoViewModel = viewModel()
) {
    // Credenciais de teste
    val TEST_EMAIL = "teste@gmail.com"
    val TEST_SENHA = "Senhaforte123@"

    var userId by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(true) }

    // Fazer login automaticamente
    LaunchedEffect(Unit) {
        Log.d("EntrarCompeticaoScreen", "🔐 Fazendo login...")
        try {
            val loginResposta = NetworkManager.userSignIn(TEST_EMAIL, TEST_SENHA)

            if (loginResposta.sucesso && loginResposta.userId != null) {
                userId = loginResposta.userId
                Log.d("EntrarCompeticaoScreen", "✅ Login bem-sucedido!")
            } else {
                loginError = loginResposta.mensagem ?: "Erro ao fazer login"
                Log.e("EntrarCompeticaoScreen", "❌ Login falhou: $loginError")
            }
        } catch (e: Exception) {
            loginError = "Erro: ${e.message}"
            Log.e("EntrarCompeticaoScreen", "❌ Exceção no login", e)
        } finally {
            isLoggingIn = false
        }
    }

    val codigo by viewModel.codigo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensagemSucesso by viewModel.mensagemSucesso.collectAsState()
    val mensagemErro by viewModel.mensagemErro.collectAsState()
    val nomeCompeticao by viewModel.nomeCompeticao.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Entrar na Competição") })
        }
    ) { paddingValues ->

        // Tela de loading durante o login
        if (isLoggingIn) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Conectando ao servidor...")
                }
            }
            return@Scaffold
        }

        // Tela de erro se login falhou
        if (loginError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "❌ Erro de Conexão",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loginError!!,
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            return@Scaffold
        }

        // Tela principal após login bem-sucedido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🎯 Entrar na Competição",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Digite o código de 6 caracteres fornecido pelo criador da competição",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de código
            OutlinedTextField(
                value = codigo,
                onValueChange = { viewModel.atualizarCodigo(it) },
                label = { Text("Código da Competição") },
                placeholder = { Text("ABC123") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters
                ),
                supportingText = {
                    Text("${codigo.length}/6 caracteres")
                }
            )

            // Botão entrar
            Button(
                onClick = {
                    userId?.let { viewModel.entrarNaCompeticao(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && codigo.length == 6
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Entrando..." else "Entrar na Competição")
            }

            // Mensagem de sucesso
            if (nomeCompeticao != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✅ $mensagemSucesso",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Competição:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = nomeCompeticao!!,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Mensagem de erro
            if (mensagemErro != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "❌ $mensagemErro",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}