package br.com.salus

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import kotlinx.coroutines.launch

class HabitConfigActivity : ComponentActivity() {

    companion object {
        const val RESULT_HABIT_DELETED = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val habitId = intent.getStringExtra("HABIT_ID") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""

        setContent {
            SalusTheme {
                HabitConfigScreen(
                    habitId = habitId,
                    userId = userId,
                    onBackClicked = {
                        finish()
                    },
                    onDeleteClicked = {
                        setResult(RESULT_HABIT_DELETED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitConfigScreen(
    habitId: String,
    userId: String,
    onBackClicked: () -> Unit = {},
    onDeleteClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var habit by remember { mutableStateOf<DocumentoHabito?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var nome by remember { mutableStateOf("") }
    var selectedPlantId by remember { mutableStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(habitId) {
        try {
            val resposta = NetworkManager.getHabitos(userId)
            if (resposta.sucesso && resposta.habitos != null) {
                habit = resposta.habitos!!.find { it.id == habitId }
                habit?.let {
                    nome = it.nome
                    selectedPlantId = it.idFotoPlanta ?: 1
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao carregar hábito: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Excluir Hábito") },
            text = { Text("Tem certeza que deseja excluir este hábito? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            try {
                                val resposta = NetworkManager.excluirHabito(habitId, userId)
                                if (resposta.sucesso) {
                                    Toast.makeText(context, "Hábito excluído com sucesso!", Toast.LENGTH_SHORT).show()
                                    onDeleteClicked()
                                } else {
                                    Toast.makeText(context, resposta.mensagem, Toast.LENGTH_SHORT).show()
                                    isDeleting = false
                                    showDeleteDialog = false
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                                isDeleting = false
                                showDeleteDialog = false
                            }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Excluir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajustes do hábito",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (habit == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hábito não encontrado", color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClicked) {
                        Text("Voltar")
                    }
                }
            }
        } else {
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
                        .background(Color(0xFFF7F4D9), shape = CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val plantaRes = when (selectedPlantId) {
                        1 -> R.drawable.planta_final_1
                        2 -> R.drawable.planta_final_2
                        3 -> R.drawable.planta_final_3
                        4 -> R.drawable.planta_final_4
                        5 -> R.drawable.planta_final_5
                        6 -> R.drawable.planta_final_6
                        else -> R.drawable.planta_final_1
                    }

                    Image(
                        painter = painterResource(id = plantaRes),
                        contentDescription = "Planta do hábito",
                        modifier = Modifier.size(150.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Escolha sua planta companheira",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(listOf(1, 2, 3, 4, 5, 6)) { idPlanta ->
                        val resourceId = when (idPlanta) {
                            1 -> R.drawable.planta_final_1
                            2 -> R.drawable.planta_final_2
                            3 -> R.drawable.planta_final_3
                            4 -> R.drawable.planta_final_4
                            5 -> R.drawable.planta_final_5
                            6 -> R.drawable.planta_final_6
                            else -> R.drawable.planta_final_1
                        }

                        val isSelected = (selectedPlantId == idPlanta)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPlantId = idPlanta }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = resourceId),
                                contentDescription = "Opção de planta $idPlanta",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
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
                    onClick = {
                        scope.launch {
                            isSaving = true
                            Toast.makeText(context, "Salvando alterações...", Toast.LENGTH_SHORT).show()
                            kotlinx.coroutines.delay(1000)
                            Toast.makeText(context, "Alterações salvas! (implementar endpoint no servidor)", Toast.LENGTH_LONG).show()
                            isSaving = false
                            onBackClicked()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    enabled = !isSaving && nome.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Text(
                            "Salvar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { showDeleteDialog = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excluir",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}