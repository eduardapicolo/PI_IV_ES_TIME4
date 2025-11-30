package br.com.salus

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
import androidx.compose.material.icons.filled.Close
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

        // Recuperar dados passados pela Intent
        val idCompeticao = intent.getStringExtra("ID_COMPETICAO") ?: ""
        val nomeAtual = intent.getStringExtra("NOME_COMPETICAO") ?: ""
        val iconeAtualId = intent.getIntExtra("ICONE_COMPETICAO", 1)

        setContent {
            SalusTheme {
                CompetitionConfigScreen(
                    initialName = nomeAtual,
                    initialIconId = iconeAtualId,
                    onBackClicked = {
                        finish()
                    },
                    onSaveClicked = { nome, iconeId ->
                        salvarAlteracoes(idCompeticao, nome, iconeId)
                    },
                    onDeleteClicked = {
                        Toast.makeText(this, "Funcionalidade de excluir em breve", Toast.LENGTH_SHORT).show()
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionConfigScreen(
    initialName: String = "",
    initialIconId: Int = 1,
    onBackClicked: () -> Unit = {},
    onSaveClicked: (String, Int) -> Unit = { _, _ -> },
    onDeleteClicked: () -> Unit = {}
) {
    var nome by remember { mutableStateOf(initialName) }
    var selectedIconId by remember { mutableIntStateOf(initialIconId) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes da competição", fontWeight = FontWeight.Bold) },
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

        if (showDialog) {
            CompetitionIconDialog(
                onDismiss = { showDialog = false },
                onPictureSelected = { id ->
                    selectedIconId = id
                    showDialog = false
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
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getCompetitionIconResourceId(selectedIconId)),
                    contentDescription = "Ícone da competição",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { onSaveClicked(nome, selectedIconId) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Salvar", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDeleteClicked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Excluir", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionConfigScreenPreview() {
    SalusTheme(darkTheme = false) {
        CompetitionConfigScreen()
    }
}