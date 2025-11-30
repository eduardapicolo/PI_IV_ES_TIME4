package br.com.salus

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CompetitionsFabContent(userId: String) {
    var showCreateCompetitionDialog by remember { mutableStateOf(false) }
    var showJoinCompetitionDialog by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showFabMenu) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 88.dp, end = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.clickable {
                        showCreateCompetitionDialog = true
                        showFabMenu = false
                    }
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Criar competição",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            showCreateCompetitionDialog = true
                            showFabMenu = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Criar")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.clickable {
                        showJoinCompetitionDialog = true
                        showFabMenu = false
                    }
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "Entrar com código",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            showJoinCompetitionDialog = true
                            showFabMenu = false
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Entrar")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showFabMenu = !showFabMenu },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Menu"
            )
        }
    }

    if (showCreateCompetitionDialog) {
        CreateCompetitionDialog(
            userId = userId,
            onDismiss = { showCreateCompetitionDialog = false },
            onSuccess = {
                showCreateCompetitionDialog = false
            }
        )
    }

    if (showJoinCompetitionDialog) {
        JoinCompetitionDialog(
            userId = userId,
            onDismiss = { showJoinCompetitionDialog = false },
            onSuccess = {
                showJoinCompetitionDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CompetitionsContent(userId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var competitionsList by remember { mutableStateOf<List<DocumentoCompeticao>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var isLoadingCompetitions by remember { mutableStateOf(false) }

    suspend fun loadCompetitions() {
        if (isLoadingCompetitions) {
            Log.d("CompetitionsContent", "⚠️ Já está carregando competições, ignorando nova requisição")
            return
        }

        isLoadingCompetitions = true
        Log.d("CompetitionsContent", "📋 Carregando competições para userId: $userId")

        try {
            val resposta = NetworkManager.buscarCompeticoes(userId)

            Log.d("CompetitionsContent", "Resposta - Sucesso: ${resposta.sucesso}")
            Log.d("CompetitionsContent", "Mensagem: ${resposta.mensagem}")
            Log.d("CompetitionsContent", "Competições: ${resposta.competicoes?.size ?: 0}")

            if (resposta.sucesso && resposta.competicoes != null) {
                competitionsList = resposta.competicoes!!
                Log.d("CompetitionsContent", "✅ ${competitionsList.size} competições carregadas")
                errorMessage = null
            } else {
                if (competitionsList.isEmpty()) {
                    errorMessage = resposta.mensagem
                }
                Log.w("CompetitionsContent", "⚠️ Falha: ${resposta.mensagem}")
            }
        } catch (e: Exception) {
            if (competitionsList.isEmpty()) {
                errorMessage = "Erro ao carregar competições: ${e.message}"
            }
            Log.e("CompetitionsContent", "❌ Exceção", e)
        } finally {
            isLoadingCompetitions = false
        }
    }

    LaunchedEffect(key1 = refreshTrigger) {
        isLoading = true
        errorMessage = null
        loadCompetitions()
        isLoading = false
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                loadCompetitions()
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

            errorMessage != null && competitionsList.isEmpty() -> {
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
                    Button(onClick = { refreshTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tentar novamente")
                    }
                }
            }

            competitionsList.isEmpty() -> {
                EmptyCompetitionsView()
            }

            else -> {
                CompetitionsListView(
                    competitions = competitionsList,
                    userId = userId,
                    onCheckin = { competition ->
                        scope.launch {
                            performCheckin(
                                competition = competition,
                                userId = userId,
                                context = context,
                                onSuccess = { refreshTrigger++ }
                            )
                        }
                    },
                    onCardClick = { competitionId ->
                        Log.d("CompetitionsContent", "🎯 Navegando para EachCompetitionActivity")
                        Log.d("CompetitionsContent", "   Competition ID: $competitionId")

                        val intent = Intent(context, EachCompetitionActivity::class.java).apply {
                            putExtra(EachCompetitionActivity.EXTRA_COMPETITION_ID, competitionId)
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
fun CompetitionsListView(
    competitions: List<DocumentoCompeticao>,
    userId: String,
    onCheckin: (DocumentoCompeticao) -> Unit,
    onCardClick: (String) -> Unit
) {
    Log.d("CompetitionsListView", "📋 Renderizando ${competitions.size} competições")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(competitions) { competition ->
            Log.d("CompetitionsListView", "  - ${competition.nome} (ID: ${competition.id})")
            CompetitionCard(
                competition = competition,
                userId = userId,
                onCardClick = {
                    Log.d("CompetitionsListView", "🎯 Card clicado! Passando ID: ${competition.id}")
                    onCardClick(competition.id)
                },
                onCheckin = { onCheckin(competition) }
            )
        }
    }
}

@Composable
fun EmptyCompetitionsView() {
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
                contentDescription = "Sem competições",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Você ainda não participa de competições...",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Crie uma nova ou entre com um código!",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

suspend fun performCheckin(
    competition: DocumentoCompeticao,
    userId: String,
    context: android.content.Context,
    onSuccess: () -> Unit
) {
    Log.d("CompetitionCheckin", "🏆 Iniciando check-in: ${competition.nome}")

    try {
        val resposta = NetworkManager.realizarCheckinCompeticao(competition.id, userId)

        if (resposta.sucesso) {
            Log.d("CompetitionCheckin", "✅ Check-in realizado com sucesso!")

            Toast.makeText(
                context,
                "Check-in realizado em '${competition.nome}'!",
                Toast.LENGTH_SHORT
            ).show()

            onSuccess()
        } else {
            Log.w("CompetitionCheckin", "⚠️ Falha: ${resposta.mensagem}")
            Toast.makeText(context, resposta.mensagem, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("CompetitionCheckin", "❌ Erro", e)
        Toast.makeText(
            context,
            "Erro ao fazer check-in: ${e.message}",
            Toast.LENGTH_SHORT
        ).show()
    }
}

@Composable
fun CompetitionCard(
    competition: DocumentoCompeticao,
    userId: String,
    onCardClick: () -> Unit,
    onCheckin: () -> Unit
) {
    val currentParticipant = competition.participantes.find { it.idUsuario == userId }
    val currentStreak = currentParticipant?.sequencia ?: 0
    val canCheckInToday = canCheckInToday(currentParticipant?.ultimoCheckin)

    val cardBackgroundColor = Color(0xFFF7F4D9)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d("CompetitionCard", "🎯 Card clicado! ID: ${competition.id}")
                onCardClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(getCompetitionIconResourceId(competition.idIcone)),
                            contentDescription = "Ícone da competição",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = competition.nome,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = competition.codigo,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Sequência",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sua sequência: $currentStreak dias",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            currentParticipant?.ultimoCheckin?.let { ultimoCheckin ->
                Text(
                    text = "Último check-in: ${formatDate(ultimoCheckin)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Competidores: ${competition.participantes.size}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            CompetitorsHorizontalList(participants = competition.participantes.take(5))

            Spacer(modifier = Modifier.height(16.dp))

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
                        Log.d("CompetitionCard", "📋 Botão Detalhes clicado! ID: ${competition.id}")
                        onCardClick()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Detalhes")
                }
            }
        }
    }
}

fun canCheckInToday(ultimoCheckin: Date?): Boolean {
    if (ultimoCheckin == null) return true

    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_YEAR)
    val todayYear = calendar.get(Calendar.YEAR)

    calendar.time = ultimoCheckin
    val lastCheckinDay = calendar.get(Calendar.DAY_OF_YEAR)
    val lastCheckinYear = calendar.get(Calendar.YEAR)

    return !(today == lastCheckinDay && todayYear == lastCheckinYear)
}

@Composable
fun JoinCompetitionDialog(userId: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var code by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isJoining) onDismiss() },
        title = { Text("Entrar em Competição") },
        text = {
            Column {
                Text("Digite o código de 6 caracteres:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { if (it.length <= 6) code = it.uppercase() },
                    label = { Text("Código") },
                    singleLine = true,
                    enabled = !isJoining
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.length == 6) {
                        isJoining = true
                        scope.launch {
                            try {
                                val resposta = NetworkManager.entrarNaCompeticao(code, userId)

                                if (resposta.sucesso) {
                                    Toast.makeText(
                                        context,
                                        "✅ Você entrou em '${resposta.nomeCompeticao}'!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onSuccess()
                                } else {
                                    Toast.makeText(context, resposta.mensagem, Toast.LENGTH_SHORT).show()
                                    isJoining = false
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                                isJoining = false
                            }
                        }
                    }
                },
                enabled = !isJoining && code.length == 6
            ) {
                if (isJoining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Entrar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isJoining) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CreateCompetitionDialog(userId: String, onDismiss: () -> Unit, onSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIconId by remember { mutableStateOf(1) }
    var showIconPicker by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (showIconPicker) {
        CompetitionIconDialog(
            onDismiss = { showIconPicker = false },
            onPictureSelected = { id ->
                selectedIconId = id
                showIconPicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        title = { Text("Nova Competição") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { if (!isCreating) showIconPicker = true }
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(getCompetitionIconResourceId(selectedIconId)),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Competição") },
                    placeholder = { Text("Ex: Desafio 30 Dias") },
                    singleLine = true,
                    enabled = !isCreating,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        isCreating = true
                        scope.launch {
                            try {
                                Log.d("CreateCompetition", "🏆 Criando competição: $name")

                                val resposta = NetworkManager.criarCompeticao(name, userId, selectedIconId)

                                if (resposta.sucesso && resposta.codigo != null) {
                                    Log.d("CreateCompetition", "✅ Sucesso! Código: ${resposta.codigo}")
                                    Toast.makeText(
                                        context,
                                        "🎉 Competição criada! Código: ${resposta.codigo}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onSuccess()
                                } else {
                                    Log.w("CreateCompetition", "⚠️ Falha: ${resposta.mensagem}")
                                    Toast.makeText(context, resposta.mensagem, Toast.LENGTH_SHORT).show()
                                    isCreating = false
                                }
                            } catch (e: Exception) {
                                Log.e("CreateCompetition", "❌ Erro ao criar competição", e)
                                Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                                isCreating = false
                            }
                        }
                    }
                },
                enabled = !isCreating && name.isNotBlank()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Criar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCreating) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun CompetitorsHorizontalList(participants: List<DocumentoParticipante>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(participants) { participant ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    participant.idFotoPerfil?.let { fotoId ->
                        Image(
                            painter = painterResource(getProfilePictureResourceId(fotoId)),
                            contentDescription = participant.apelidoUsuario,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Text(
                            text = participant.apelidoUsuario.firstOrNull()?.uppercase() ?: "?",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = participant.apelidoUsuario,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
    return formatter.format(date)
}