package br.com.salus

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _habitos = MutableStateFlow<List<DocumentoHabito>>(emptyList())
    val habitos: StateFlow<List<DocumentoHabito>> = _habitos

    private val _mensagemErro = MutableStateFlow<String?>(null)
    val mensagemErro: StateFlow<String?> = _mensagemErro

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarHabitos(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _mensagemErro.value = null

            try {
                Log.d(TAG, "Carregando hábitos para userId: $userId")
                val resposta = NetworkManager.getHabitos(userId)

                Log.d(TAG, "Resposta recebida - Sucesso: ${resposta.sucesso}")
                Log.d(TAG, "Mensagem: ${resposta.mensagem}")
                Log.d(TAG, "Hábitos: ${resposta.habitos?.size ?: 0}")

                if (resposta.sucesso && resposta.habitos != null) {
                    _habitos.value = resposta.habitos!!
                    Log.d(TAG, "✅ ${_habitos.value.size} hábitos carregados")
                } else {
                    _habitos.value = emptyList()
                    _mensagemErro.value = resposta.mensagem
                    Log.w(TAG, "⚠️ Falha ao carregar hábitos: ${resposta.mensagem}")
                }
            } catch (e: Exception) {
                _habitos.value = emptyList()
                _mensagemErro.value = "Erro crítico de rede: ${e.message}"
                Log.e(TAG, "❌ Exceção ao carregar hábitos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun mostrarErroDeLogin(mensagem: String?) {
        _isLoading.value = false
        val msgDeErro = mensagem ?: "Erro desconhecido (mensagem nula)"
        _mensagemErro.value = "Falha no login de teste: $msgDeErro"
        Log.e(TAG, "❌ Erro de login: $msgDeErro")
    }

    fun limparMensagemDeErro() {
        _mensagemErro.value = null
    }

    fun fazerCheckin(habitoParaAtualizar: DocumentoHabito) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Fazendo check-in para hábito: ${habitoParaAtualizar.nome}")
                val resposta = NetworkManager.realizarCheckin(habitoParaAtualizar.id)

                if (resposta.sucesso && resposta.habitoAtualizado != null) {
                    Log.d(TAG, "✅ Check-in realizado com sucesso")
                    val habitoAtualizado = resposta.habitoAtualizado!!

                    val listaAtual = _habitos.value.toMutableList()
                    val index = listaAtual.indexOfFirst { it.id == habitoAtualizado.id }

                    if (index != -1) {
                        listaAtual[index] = habitoAtualizado
                        _habitos.value = listaAtual
                        Log.d(TAG, "Lista local atualizada")
                    }
                } else {
                    Log.w(TAG, "⚠️ Falha no check-in: ${resposta.mensagem}")
                    _mensagemErro.value = resposta.mensagem
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao fazer check-in", e)
                _mensagemErro.value = "Erro de rede ao fazer check-in: ${e.message}"
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel = viewModel()) {

    val TEST_EMAIL = "dududuefu.gomes@gmail.com"
    val TEST_SENHA = "Senhaforte123@"

    val habitos by viewModel.habitos.collectAsState()
    val erro by viewModel.mensagemErro.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Log.d("HomeScreen_Recompose", "isLoading=$isLoading, erro=$erro, habitos.size=${habitos.size}")

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "🔐 Iniciando login de teste...")
        val loginResposta = NetworkManager.userSignIn(TEST_EMAIL, TEST_SENHA)

        Log.d("HomeScreen", "Resposta do login:")
        Log.d("HomeScreen", "  - Sucesso: ${loginResposta.sucesso}")
        Log.d("HomeScreen", "  - Mensagem: ${loginResposta.mensagem}")
        Log.d("HomeScreen", "  - UserId: ${loginResposta.userId}")

        if (loginResposta.sucesso && loginResposta.userId != null) {
            val idDoUsuarioLogado = loginResposta.userId!!
            Log.d("HomeScreen", "✅ Login bem-sucedido! Carregando hábitos...")
            viewModel.carregarHabitos(idDoUsuarioLogado)
        } else {
            Log.e("HomeScreen", "❌ Login falhou!")
            viewModel.mostrarErroDeLogin(loginResposta.mensagem)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meus Hábitos") })
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }

                habitos.isEmpty() && !isLoading && erro == null -> {
                    Text(text = "Você ainda não criou nenhum hábito...")
                }

                habitos.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(habitos) { habito ->
                            HabitoItem(
                                habito = habito,
                                onClick = {
                                    viewModel.limparMensagemDeErro()
                                    viewModel.fazerCheckin(habito)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            if (erro != null && !isLoading) {
                Text(
                    text = "Erro: $erro",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun HabitoItem(habito: DocumentoHabito, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = habito.nome,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sequência: ${habito.sequenciaCheckin ?: 0} dias",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Último check-in: ${formatDate(habito.ultimoCheckin)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

private fun formatDate(date: Date?): String {
    if (date == null) return "Nenhum"
    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return formatter.format(date)
}