package br.com.salus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EachHabitActivity : ComponentActivity() {

    companion object {
        const val EXTRA_HABIT_ID = "HABIT_ID"
        const val EXTRA_USER_ID = "USER_ID"
        const val RESULT_HABIT_DELETED = 100
        const val REQUEST_CODE_CONFIG = 200
        private const val TAG = "EachHabitActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val habitId = intent.getStringExtra(EXTRA_HABIT_ID)
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""

        Log.d(TAG, "onCreate - Habit ID: $habitId, User ID: $userId")

        if (habitId == null) {
            Log.e(TAG, "Habit ID é NULL!")
            Toast.makeText(this, "ID do hábito não fornecido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            SalusTheme {
                EachHabitScreen(
                    habitId = habitId,
                    userId = userId,
                    onNavigateToConfig = {
                        val intent = Intent(this, HabitConfigActivity::class.java).apply {
                            putExtra("HABIT_ID", habitId)
                            putExtra("USER_ID", userId)
                        }
                        startActivityForResult(intent, REQUEST_CODE_CONFIG)
                    }
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CONFIG) {
            when (resultCode) {
                HabitConfigActivity.RESULT_HABIT_DELETED -> {
                    setResult(RESULT_HABIT_DELETED)
                    finish()
                }
                HabitConfigActivity.RESULT_HABIT_UPDATED -> {
                    Log.d(TAG, "Hábito atualizado, recarregando dados...")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EachHabitScreen(
    habitId: String,
    userId: String,
    onNavigateToConfig: () -> Unit
) {
    val context = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()

    var habit by remember { mutableStateOf<DocumentoHabito?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCheckinDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    suspend fun loadHabit() {
        Log.d("EachHabitScreen", "🔍 Buscando hábito com ID: $habitId")

        try {
            val resposta = NetworkManager.getHabitos(userId)

            if (resposta.sucesso && resposta.habitos != null) {
                habit = resposta.habitos!!.find { it.id == habitId }

                if (habit != null) {
                    Log.d("EachHabitScreen", " Hábito encontrado: ${habit!!.nome}")
                    errorMessage = null
                } else {
                    Log.w("EachHabitScreen", " Hábito não encontrado na lista")
                    errorMessage = "Hábito não encontrado"
                }
            } else {
                Log.w("EachHabitScreen", " Falha ao buscar hábitos: ${resposta.mensagem}")
                errorMessage = resposta.mensagem
            }
        } catch (e: Exception) {
            Log.e("EachHabitScreen", " Erro ao buscar hábito", e)
            errorMessage = "Erro: ${e.message}"
        }
    }

    LaunchedEffect(habitId, refreshTrigger) {
        isLoading = true
        loadHabit()
        isLoading = false
    }

    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity

        val listener = androidx.core.util.Consumer<Intent> { intent ->
            refreshTrigger++
        }
        activity?.addOnNewIntentListener(listener)
        onDispose {
            activity?.removeOnNewIntentListener(listener)
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                loadHabit()
                isRefreshing = false
            }
        }
    )

    if (showCheckinDialog && habit != null) {
        CheckinConfirmationDialog(
            habitName = habit!!.nome,
            onDismiss = { showCheckinDialog = false },
            onConfirm = {
                showCheckinDialog = false
                scope.launch {
                    val resposta = NetworkManager.realizarCheckin(habitId)
                    if (resposta.sucesso) {
                        Toast.makeText(
                            context,
                            " Check-in realizado!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadHabit()
                    } else {
                        Toast.makeText(
                            context,
                            resposta.mensagem,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.nome ?: "Carregando...") },
                navigationIcon = {
                    IconButton(onClick = { context?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (habit != null) {
                        IconButton(onClick = onNavigateToConfig) {
                            Icon(Icons.Filled.Settings, contentDescription = "Editar Hábito")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (habit != null && canCheckInToday(habit!!.ultimoCheckin)) {
                ExtendedFloatingActionButton(
                    onClick = { showCheckinDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Check-in")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fazer Check-in")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pullRefresh(pullRefreshState)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { context?.finish() }) {
                            Text("Voltar")
                        }
                    }
                }

                habit != null -> {
                    HabitDetailsContent(habit = habit!!)
                }
            }

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun CheckinConfirmationDialog(
    habitName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Confirmar Check-in",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Você realmente cumpriu o hábito",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$habitName\"",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "hoje?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "💪 Seja honesto com você mesmo!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sim, cumpri!")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun HabitDetailsContent(habit: DocumentoHabito) {
    val context = LocalContext.current
    val sequencia = habit.sequenciaCheckin ?: 0

    val plantaRes = getPlantaDrawableId(
        context = context,
        sequenciaCheckin = sequencia,
        idPlantaFinal = habit.idFotoPlanta
    )

    val motivationalMessage = getMotivationalMessage(sequencia)
    val canCheckIn = canCheckInToday(habit.ultimoCheckin)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(Color(0xFFF7F4D9)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(plantaRes),
                contentDescription = "Planta do hábito",
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = habit.nome,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$sequencia",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (sequencia == 1) "dia consecutivo" else "dias consecutivos",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!canCheckIn) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Check-in de hoje já realizado!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💪",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = motivationalMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (habit.ultimoCheckin != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informações",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    HabitInfoRow(
                        label = "Último check-in:",
                        value = formatDate(habit.ultimoCheckin!!)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HabitInfoRow(
                        label = "Estágio da planta:",
                        value = getPlantStage(sequencia)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun HabitInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun getMotivationalMessage(streak: Int): String {
    return when {
        streak == 0 -> "Comece sua jornada! Cada grande conquista começa com um primeiro passo."
        streak in 1..2 -> "Ótimo começo! Você está plantando as sementes do sucesso."
        streak in 3..5 -> "Continue assim! Sua planta está começando a crescer."
        streak in 6..10 -> "Incrível! Você está construindo um hábito sólido."
        streak in 11..20 -> "Você está arrasando! Sua dedicação está florescendo."
        streak in 21..29 -> "Quase lá! Em breve você terá uma planta completamente desenvolvida."
        streak >= 30 -> "Parabéns! Você cultivou um hábito forte e duradouro. Continue assim!"
        else -> "Continue firme! Cada dia é uma vitória."
    }
}

fun getPlantStage(streak: Int): String {
    return when {
        streak <= 2 -> "Semente (Estágio 1)"
        streak <= 5 -> "Broto (Estágio 2)"
        streak <= 10 -> "Muda Pequena (Estágio 3)"
        streak <= 15 -> "Muda Média (Estágio 4)"
        streak <= 20 -> "Planta Jovem (Estágio 5)"
        streak < 30 -> "Planta Crescida (Estágio 6)"
        else -> "Planta Completa"
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
    return formatter.format(date)
}