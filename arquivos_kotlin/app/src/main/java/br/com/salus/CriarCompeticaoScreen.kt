package br.com.salus

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CriarCompeticaoViewModel : ViewModel() {
    companion object {
        private const val TAG = "CriarCompeticaoVM"
    }

    private val _nomeCompeticao = MutableStateFlow("")
    val nomeCompeticao: StateFlow<String> = _nomeCompeticao

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _mensagemSucesso = MutableStateFlow<String?>(null)
    val mensagemSucesso: StateFlow<String?> = _mensagemSucesso

    private val _mensagemErro = MutableStateFlow<String?>(null)
    val mensagemErro: StateFlow<String?> = _mensagemErro

    private val _codigoGerado = MutableStateFlow<String?>(null)
    val codigoGerado: StateFlow<String?> = _codigoGerado

    fun atualizarNome(novoNome: String) {
        _nomeCompeticao.value = novoNome
    }

    fun criarCompeticao(userId: String) {
        if (_nomeCompeticao.value.trim().isEmpty()) {
            _mensagemErro.value = "Digite um nome para a competição"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _mensagemErro.value = null
            _mensagemSucesso.value = null
            _codigoGerado.value = null

            try {
                Log.d(TAG, "=== INÍCIO: Criar Competição ===")
                Log.d(TAG, "Nome: ${_nomeCompeticao.value}")
                Log.d(TAG, "UserId: $userId")

                // Verificar se está conectado ANTES de criar
                if (!NetworkManager.isConectado()) {
                    Log.w(TAG, "⚠️ NÃO ESTÁ CONECTADO! Tentando conectar...")
                }

                val resposta = NetworkManager.criarCompeticao(_nomeCompeticao.value, userId)

                Log.d(TAG, "Resposta recebida:")
                Log.d(TAG, "  - Sucesso: ${resposta.sucesso}")
                Log.d(TAG, "  - Mensagem: ${resposta.mensagem}")
                Log.d(TAG, "  - Código: ${resposta.codigo}")

                if (resposta.sucesso && resposta.codigo != null) {
                    Log.d(TAG, "✅ Competição criada! Código: ${resposta.codigo}")
                    _mensagemSucesso.value = resposta.mensagem
                    _codigoGerado.value = resposta.codigo
                    _nomeCompeticao.value = "" // Limpa o campo
                } else {
                    Log.w(TAG, "⚠️ Falha: ${resposta.mensagem}")
                    _mensagemErro.value = resposta.mensagem
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao criar competição", e)
                _mensagemErro.value = "Erro: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "=== FIM: Criar Competição ===")
            }
        }
    }

    fun limparMensagens() {
        _mensagemErro.value = null
        _mensagemSucesso.value = null
        _codigoGerado.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CriarCompeticaoScreen(
    viewModel: CriarCompeticaoViewModel = viewModel()
) {
    // Credenciais de teste - mesmas da HomeScreen
    val TEST_EMAIL = "dududuefu.gomes@gmail.com"
    val TEST_SENHA = "Senhaforte123@"

    var userId by remember { mutableStateOf<String?>(null) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(true) }

    // Fazer login automaticamente ao carregar a tela
    LaunchedEffect(Unit) {
        Log.d("CriarCompeticaoScreen", "🔐 Fazendo login...")
        try {
            val loginResposta = NetworkManager.userSignIn(TEST_EMAIL, TEST_SENHA)

            Log.d("CriarCompeticaoScreen", "Login - Sucesso: ${loginResposta.sucesso}")
            Log.d("CriarCompeticaoScreen", "Login - UserId: ${loginResposta.userId}")

            if (loginResposta.sucesso && loginResposta.userId != null) {
                userId = loginResposta.userId
                Log.d("CriarCompeticaoScreen", "✅ Login bem-sucedido!")
            } else {
                loginError = loginResposta.mensagem ?: "Erro ao fazer login"
                Log.e("CriarCompeticaoScreen", "❌ Login falhou: $loginError")
            }
        } catch (e: Exception) {
            loginError = "Erro: ${e.message}"
            Log.e("CriarCompeticaoScreen", "❌ Exceção no login", e)
        } finally {
            isLoggingIn = false
        }
    }

    val nomeCompeticao by viewModel.nomeCompeticao.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val mensagemSucesso by viewModel.mensagemSucesso.collectAsState()
    val mensagemErro by viewModel.mensagemErro.collectAsState()
    val codigoGerado by viewModel.codigoGerado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Criar Competição") })
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
                text = "🏆 Nova Competição",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de nome
            OutlinedTextField(
                value = nomeCompeticao,
                onValueChange = { viewModel.atualizarNome(it) },
                label = { Text("Nome da Competição") },
                placeholder = { Text("Ex: Desafio 30 Dias") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true
            )

            // Botão criar
            Button(
                onClick = {
                    userId?.let { viewModel.criarCompeticao(it) }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && nomeCompeticao.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Criando..." else "Criar Competição")
            }

            // Mensagem de sucesso com código
            if (codigoGerado != null) {
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
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Código da Competição:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = codigoGerado!!,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Compartilhe este código com outros usuários!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
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