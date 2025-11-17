package br.com.salus

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Enum para controlar qual tela está visível
enum class TelaAtual {
    LOGIN, CADASTRO, DASHBOARD, CRIAR_COMPETICAO, ENTRAR_COMPETICAO, LISTA_COMPETICOES
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavegacao()
                }
            }
        }
    }
}

@Composable
fun AppNavegacao() {
    // Estado global da navegação e do usuário logado
    var telaAtual by remember { mutableStateOf(TelaAtual.LOGIN) }
    var userIdLogado by remember { mutableStateOf<String?>(null) }
    var userNameLogado by remember { mutableStateOf<String?>(null) }

    when (telaAtual) {
        TelaAtual.LOGIN -> ScreenLogin(
            onLoginSuccess = { id ->
                userIdLogado = id
                telaAtual = TelaAtual.DASHBOARD
            },
            onIrParaCadastro = { telaAtual = TelaAtual.CADASTRO }
        )
        TelaAtual.CADASTRO -> ScreenCadastro(
            onVoltar = { telaAtual = TelaAtual.LOGIN }
        )
        TelaAtual.DASHBOARD -> ScreenDashboard(
            onNavegar = { novaTela -> telaAtual = novaTela },
            onSair = {
                userIdLogado = null
                telaAtual = TelaAtual.LOGIN
            }
        )
        TelaAtual.CRIAR_COMPETICAO -> ScreenCriarCompeticao(
            userId = userIdLogado ?: "",
            onVoltar = { telaAtual = TelaAtual.DASHBOARD }
        )
        TelaAtual.ENTRAR_COMPETICAO -> ScreenEntrarCompeticao(
            userId = userIdLogado ?: "",
            onVoltar = { telaAtual = TelaAtual.DASHBOARD }
        )
        TelaAtual.LISTA_COMPETICOES -> ScreenListaCompeticoes(
            userId = userIdLogado ?: "",
            onVoltar = { telaAtual = TelaAtual.DASHBOARD }
        )
    }
}

// --- TELA 1: LOGIN ---
@Composable
fun ScreenLogin(onLoginSuccess: (String) -> Unit, onIrParaCadastro: () -> Unit) {
    var email by remember { mutableStateOf("dududuefu.gomes@gmail.com") } // Preenchido para facilitar teste
    var senha by remember { mutableStateOf("Senhaforte123@") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login Salus", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = senha, onValueChange = { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    isLoading = true
                    val resp = NetworkManager.userSignIn(email, senha)
                    isLoading = false
                    if (resp.sucesso && resp.userId != null) {
                        onLoginSuccess(resp.userId)
                    } else {
                        Toast.makeText(context, "Erro: ${resp.mensagem}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Entrando..." else "Entrar")
        }

        TextButton(onClick = onIrParaCadastro) {
            Text("Não tem conta? Cadastre-se")
        }
    }
}

// --- TELA 2: CADASTRO ---
@Composable
fun ScreenCadastro(onVoltar: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var apelido by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Novo Usuário", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome Completo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = apelido, onValueChange = { apelido = it }, label = { Text("Apelido") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-mail") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = senha, onValueChange = { senha = it }, label = { Text("Senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            scope.launch {
                val resp = NetworkManager.userSignUp(nome, email, senha, apelido, 1)
                if (resp.sucesso) {
                    Toast.makeText(context, "Sucesso! Faça login.", Toast.LENGTH_SHORT).show()
                    onVoltar()
                } else {
                    Toast.makeText(context, "Erro: ${resp.mensagem}", Toast.LENGTH_LONG).show()
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Criar Conta")
        }
        TextButton(onClick = onVoltar) { Text("Voltar ao Login") }
    }
}

// --- TELA 3: DASHBOARD ---
@Composable
fun ScreenDashboard(onNavegar: (TelaAtual) -> Unit, onSair: () -> Unit) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Menu Principal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        BotaoMenu("🏆 Criar Competição") { onNavegar(TelaAtual.CRIAR_COMPETICAO) }
        Spacer(Modifier.height(16.dp))
        BotaoMenu("🚪 Entrar em Competição") { onNavegar(TelaAtual.ENTRAR_COMPETICAO) }
        Spacer(Modifier.height(16.dp))
        BotaoMenu("📋 Minhas Competições / Check-in") { onNavegar(TelaAtual.LISTA_COMPETICOES) }

        Spacer(Modifier.height(48.dp))

        OutlinedButton(onClick = {
            scope.launch {
                NetworkManager.desconectar()
                onSair()
            }
        }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)) {
            Text("Sair / Desconectar")
        }
    }
}

@Composable
fun BotaoMenu(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(texto, fontSize = 16.sp)
    }
}

// --- TELA 4: CRIAR COMPETIÇÃO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCriarCompeticao(userId: String, onVoltar: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var codigoGerado by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Criar Competição") }, navigationIcon = { IconButton(onClick = onVoltar){ Icon(Icons.Default.ArrowBack, "Voltar") } }) }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(16.dp)) {
            OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome da Competição") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                scope.launch {
                    val resp = NetworkManager.criarCompeticao(nome, userId)
                    if (resp.sucesso) codigoGerado = resp.codigo
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Gerar Competição")
            }

            if (codigoGerado != null) {
                Spacer(Modifier.height(24.dp))
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Competição Criada!", color = Color.Black, fontWeight = FontWeight.Bold)
                        Text(codigoGerado!!, fontSize = 32.sp, color = Color(0xFF006064), fontWeight = FontWeight.Bold)
                        Text("Compartilhe este código.", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// --- TELA 5: ENTRAR EM COMPETIÇÃO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEntrarCompeticao(userId: String, onVoltar: () -> Unit) {
    var codigo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Entrar na Competição") }, navigationIcon = { IconButton(onClick = onVoltar){ Icon(Icons.Default.ArrowBack, "Voltar") } }) }
    ) { pad ->
        Column(modifier = Modifier.padding(pad).padding(16.dp)) {
            Text("Digite o código fornecido pelo criador:")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = codigo, onValueChange = { codigo = it.uppercase() }, label = { Text("Código (6 chars)") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            Button(onClick = {
                scope.launch {
                    val resp = NetworkManager.entrarNaCompeticao(codigo, userId)
                    if (resp.sucesso) {
                        Toast.makeText(context, "Você entrou na: ${resp.nomeCompeticao}", Toast.LENGTH_LONG).show()
                        onVoltar()
                    } else {
                        Toast.makeText(context, "Erro: ${resp.mensagem}", Toast.LENGTH_SHORT).show()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Entrar")
            }
        }
    }
}

// --- TELA 6: LISTA E CHECK-IN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenListaCompeticoes(userId: String, onVoltar: () -> Unit) {
    var lista by remember { mutableStateOf<List<DocumentoCompeticao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Carregar ao abrir a tela
    LaunchedEffect(Unit) {
        val resp = NetworkManager.buscarCompeticoes(userId)
        if (resp.sucesso && resp.competicoes != null) {
            lista = resp.competicoes!!
        }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Competições & Check-in") }, navigationIcon = { IconButton(onClick = onVoltar){ Icon(Icons.Default.ArrowBack, "Voltar") } }) }
    ) { pad ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (lista.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) { Text("Nenhuma competição encontrada.") }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.padding(pad)) {
                items(lista) { competicao ->
                    CardCompeticaoItem(competicao, userId)
                }
            }
        }
    }
}

@Composable
fun CardCompeticaoItem(competicao: DocumentoCompeticao, userId: String) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(competicao.nome, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Código: ${competicao.codigo}", fontSize = 14.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Text("Participantes: ${competicao.participantes.size}", fontSize = 14.sp)

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        val resp = NetworkManager.realizarCheckinCompeticao(competicao.id, userId)
                        if (resp.sucesso) {
                            Toast.makeText(context, "✅ Check-in realizado na competição!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "❌ ${resp.mensagem}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Fazer Check-in na Competição")
            }
        }
    }
}