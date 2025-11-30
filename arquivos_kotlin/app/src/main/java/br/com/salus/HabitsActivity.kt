@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitsFabContent(onAddClick: () -> Unit) {
    FloatingActionButton(
        onClick = onAddClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar Hábito"
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HabitsContent(
    userId: String,
    refreshTrigger: Int,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var listaDeHabitos by remember { mutableStateOf<List<DocumentoHabito>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoadingHabits by remember { mutableStateOf(false) }

    suspend fun loadHabits() {
        if (isLoadingHabits) {
            Log.d("HabitsContent", "⚠️ Já está carregando hábitos, ignorando nova requisição")
            return
        }

        isLoadingHabits = true
        Log.d("HabitsContent", "📋 Carregando hábitos para userId: $userId")

        try {
            val resposta = NetworkManager.getHabitos(userId)

            if (resposta.sucesso && resposta.habitos != null) {
                listaDeHabitos = resposta.habitos!!
                Log.d("HabitsContent", "✅ ${listaDeHabitos.size} hábitos carregados")
                errorMessage = null
            } else {
                if (listaDeHabitos.isEmpty()) {
                    errorMessage = resposta.mensagem
                }
                Log.w("HabitsContent", "⚠️ Falha: ${resposta.mensagem}")
            }
        } catch (e: Exception) {
            if (listaDeHabitos.isEmpty()) {
                errorMessage = "Erro ao carregar hábitos: ${e.message}"
            }
            Log.e("HabitsContent", "❌ Exceção", e)
        } finally {
            isLoadingHabits = false
        }
    }

    LaunchedEffect(key1 = userId, key2 = refreshTrigger) {
        Log.d("HabitsContent", "🔄 LaunchedEffect disparado - refreshTrigger: $refreshTrigger")
        isLoading = true
        errorMessage = null
        loadHabits()
        isLoading = false
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                loadHabits()
                isRefreshing = false
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            errorMessage != null && listaDeHabitos.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch {
                            isLoading = true
                            loadHabits()
                            isLoading = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tentar novamente")
                    }
                }
            }

            listaDeHabitos.isEmpty() -> {
                EmptyHabitsView()
            }

            else -> {
                HabitsListView(
                    habits = listaDeHabitos,
                    userId = userId,
                    onCheckin = { habit ->
                        scope.launch {
                            performHabitCheckin(
                                habit = habit,
                                context = context,
                                onSuccess = {
                                    scope.launch {
                                        loadHabits()
                                    }
                                }
                            )
                        }
                    },
                    onCardClick = { habitId ->
                        Log.d("HabitsContent", "🎯 Navegando para EachHabitActivity")
                        Log.d("HabitsContent", "   Habit ID: $habitId")

                        val intent = Intent(context, EachHabitActivity::class.java).apply {
                            putExtra(EachHabitActivity.EXTRA_HABIT_ID, habitId)
                            putExtra("USER_ID", userId)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun EmptyHabitsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.estagio_1),
                contentDescription = "Sem hábitos",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Você ainda não criou nenhum hábito...",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Adicione um novo hábito usando o botão +",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HabitsListView(
    habits: List<DocumentoHabito>,
    userId: String,
    onCheckin: (DocumentoHabito) -> Unit,
    onCardClick: (String) -> Unit
) {
    Log.d("HabitsListView", "📋 Renderizando ${habits.size} hábitos")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(habits) { habit ->
            Log.d("HabitsListView", "  - ${habit.nome} (ID: ${habit.id})")
            HabitCard(
                habit = habit,
                userId = userId,
                onCardClick = {
                    Log.d("HabitsListView", "🎯 Card clicado! Passando ID: ${habit.id}")
                    onCardClick(habit.id)
                },
                onCheckin = { onCheckin(habit) }
            )
        }
    }
}

@Composable
fun HabitCard(
    habit: DocumentoHabito,
    userId: String,
    onCardClick: () -> Unit,
    onCheckin: () -> Unit
) {
    val context = LocalContext.current
    val currentStreak = habit.sequenciaCheckin ?: 0
    val canCheckInToday = canCheckInToday(habit.ultimoCheckin)

    val cardBackgroundColor = Color(0xFFF7F4D9)

    val plantaRes = getPlantaDrawableId(
        context = context,
        sequenciaCheckin = currentStreak,
        idPlantaFinal = habit.idFotoPlanta
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("HabitCard", "🎯 Card clicado! ID: ${habit.id}")
                onCardClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.nome,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Sequência",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Sequência: ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$currentStreak")
                            }
                            append(" dias")
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCheckin,
                        enabled = canCheckInToday,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (canCheckInToday)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = if (canCheckInToday) Icons.Default.Check else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (canCheckInToday) "Check-in" else "Feito!")
                    }

                    OutlinedButton(
                        onClick = {
                            Log.d("HabitCard", "📋 Botão Detalhes clicado! ID: ${habit.id}")
                            onCardClick()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Detalhes")
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = plantaRes),
                    contentDescription = "Status do hábito",
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}

suspend fun performHabitCheckin(
    habit: DocumentoHabito,
    context: android.content.Context,
    onSuccess: () -> Unit
) {
    Log.d("HabitCheckin", "🌱 Iniciando check-in: ${habit.nome}")

    try {
        val resposta = NetworkManager.realizarCheckin(habit.id)

        if (resposta.sucesso) {
            Log.d("HabitCheckin", "✅ Check-in realizado com sucesso!")

            Toast.makeText(
                context,
                "Check-in realizado em '${habit.nome}'!",
                Toast.LENGTH_SHORT
            ).show()

            onSuccess()
        } else {
            Log.w("HabitCheckin", "⚠️ Falha: ${resposta.mensagem}")
            Toast.makeText(context, resposta.mensagem, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("HabitCheckin", "❌ Erro", e)
        Toast.makeText(
            context,
            "Erro ao fazer check-in: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun AddHabitDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var habitName by remember { mutableStateOf("") }
    var selectedPlantId by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    val opcoesDePlantas = listOf(1, 2, 3, 4, 5, 6)

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(text = "Novo Hábito", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Qual hábito você quer começar?")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Nome do hábito") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Escolha sua planta companheira:")

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(opcoesDePlantas) { idPlanta ->
                        val resourceId = when (idPlanta) {
                            1 -> R.drawable.planta_final_1
                            2 -> R.drawable.planta_final_2
                            3 -> R.drawable.planta_final_3
                            4 -> R.drawable.planta_final_4
                            5 -> R.drawable.planta_final_5
                            6 -> R.drawable.planta_final_6
                            else -> R.drawable.planta_final_1
                        }

                        val finalResId = if (resourceId != 0) resourceId else R.drawable.planta_final_1

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
                                painter = painterResource(id = finalResId),
                                contentDescription = "Opção de planta $idPlanta",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        scope.launch {
                            isLoading = true

                            Log.d("AddHabitDialog", "🌱 Criando novo hábito: $habitName")
                            val resposta = NetworkManager.newHabit(habitName, userId, selectedPlantId)

                            isLoading = false

                            if (resposta.sucesso) {
                                Log.d("AddHabitDialog", "✅ Hábito criado com sucesso!")
                                Toast.makeText(context, "Hábito criado!", Toast.LENGTH_SHORT).show()
                                onSuccess()
                            } else {
                                Log.w("AddHabitDialog", "⚠️ Falha: ${resposta.mensagem}")
                                Toast.makeText(context, resposta.mensagem, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Digite um nome para o hábito", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatDate(date: Date?): String {
    if (date == null) return "Nenhum"
    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return formatter.format(date)
}