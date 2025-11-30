package br.com.salus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompetitionConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val idCompeticao = intent.getStringExtra("COMPETITION_ID") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val isCreator = intent.getBooleanExtra("IS_CREATOR", false)
        val nomeAtual = intent.getStringExtra("NOME_COMPETICAO") ?: ""
        val iconeAtualId = intent.getIntExtra("ICONE_COMPETICAO", 1)

        setContent {
            SalusTheme {
                CompetitionConfigScreen(
                    initialName = nomeAtual,
                    initialIconId = iconeAtualId,
                    isCreator = isCreator,
                    onBackClicked = {
                        finish()
                    },
                    onSaveClicked = { nome, iconeId ->
                        if (isCreator) {
                            salvarAlteracoes(idCompeticao, nome, iconeId)
                        }
                    },
                    onDeleteClicked = {
                        // Ação de EXCLUIR (apenas Criador)
                        confirmarEExcluir(idCompeticao, userId)
                    },
                    onLeaveClicked = {
                        // Ação de SAIR (apenas Participante)
                        confirmarESair(idCompeticao, userId)
                    }
                )
            }
        }
    }

    private fun salvarAlteracoes(idCompeticao: String, novoNome: String, novoIconeId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val resposta = NetworkManager.editarCompeticao(
                idCompeticao = idCompeticao,
                novoNome = novoNome,
                novoIdIcone = novoIconeId
            )

            withContext(Dispatchers.Main) {
                if (resposta.sucesso) {
                    Toast.makeText(this@CompetitionConfigActivity, "Competição atualizada!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@CompetitionConfigActivity, resposta.mensagem, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun confirmarESair(idCompeticao: String, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val resposta = NetworkManager.sairDaCompeticao(idCompeticao, userId)
            withContext(Dispatchers.Main) {
                if (resposta.sucesso) {
                    Toast.makeText(this@CompetitionConfigActivity, "Você saiu da competição.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@CompetitionConfigActivity, MainAppScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent.putExtra("USER_ID", userId)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CompetitionConfigActivity, resposta.mensagem, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun confirmarEExcluir(idCompeticao: String, userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val resposta = NetworkManager.excluirCompeticao(idCompeticao, userId)
            withContext(Dispatchers.Main) {
                if (resposta.sucesso) {
                    Toast.makeText(this@CompetitionConfigActivity, "Competição excluída.", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@CompetitionConfigActivity, MainAppScreen::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    intent.putExtra("USER_ID", userId)

                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CompetitionConfigActivity, resposta.mensagem, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionConfigScreen(
    initialName: String = "",
    initialIconId: Int = 1,
    isCreator: Boolean = false,
    onBackClicked: () -> Unit = {},
    onSaveClicked: (String, Int) -> Unit = { _, _ -> },
    onDeleteClicked: () -> Unit = {},
    onLeaveClicked: () -> Unit = {}
) {
    var nome by remember { mutableStateOf(initialName) }
    var selectedIconId by remember { mutableIntStateOf(initialIconId) }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val screenTitle = if (isCreator) "Ajustes da competição" else "Detalhes da competição"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->

        if (showDialog && isCreator) {
            CompetitionIconDialog(
                onDismiss = { showDialog = false },
                onPictureSelected = { id ->
                    selectedIconId = id
                    showDialog = false
                }
            )
        }

        if (showConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmationDialog = false },
                title = {
                    Text(if (isCreator) "Excluir Competição?" else "Sair da Competição?")
                },
                text = {
                    Text(
                        if (isCreator)
                            "Tem certeza que deseja excluir esta competição permanentemente? Todos os participantes serão removidos."
                        else
                            "Tem certeza que deseja sair desta competição? Seu progresso será perdido."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmationDialog = false
                            if (isCreator) onDeleteClicked() else onLeaveClicked()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (isCreator) "Excluir" else "Sair")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmationDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
                    .clip(CircleShape)
                    .clickable(enabled = isCreator) { showDialog = true }, // Só clica se for criador
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getCompetitionIconResourceId(selectedIconId)),
                    contentDescription = "Ícone da competição",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )

                if (isCreator) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.1f))
                    )
                }
            }

            if (isCreator) {
                Text(
                    text = "Toque para alterar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = nome,
                onValueChange = { if (isCreator) nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = isCreator,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isCreator) {
                Button(
                    onClick = { onSaveClicked(nome, selectedIconId) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        "Salvar Alterações",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            TextButton(
                onClick = { showConfirmationDialog = true }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCreator) Icons.Default.Delete else Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCreator) "Excluir Competição" else "Sair da Competição",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}