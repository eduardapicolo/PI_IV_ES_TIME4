package br.com.salus

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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

class EachCompetitionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_COMPETITION_ID = "COMPETITION_ID"
        const val EXTRA_USER_ID = "USER_ID"
        private const val TAG = "EachCompetitionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val competitionId = intent.getStringExtra(EXTRA_COMPETITION_ID)
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: ""

        Log.d(TAG, "onCreate - Competition ID: $competitionId, User ID: $userId")

        if (competitionId == null) {
            Log.e(TAG, "Competition ID é NULL!")
            Toast.makeText(this, "ID da competição não fornecido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            SalusTheme {
                EachCompetitionScreen(competitionId = competitionId, userId = userId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun EachCompetitionScreen(competitionId: String, userId: String) {
    val context = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()

    var competition by remember { mutableStateOf<DocumentoCompeticao?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCheckinDialog by remember { mutableStateOf(false) }

    suspend fun loadCompetition() {
        Log.d("EachCompetitionScreen", "🔍 Buscando competição com ID: $competitionId")

        try {
            val resposta = NetworkManager.buscarCompeticoes(userId)

            if (resposta.sucesso && resposta.competicoes != null) {
                competition = resposta.competicoes!!.find { it.id == competitionId }

                if (competition != null) {
                    Log.d("EachCompetitionScreen", "✅ Competição encontrada: ${competition!!.nome}")
                    errorMessage = null
                } else {
                    Log.w("EachCompetitionScreen", "⚠️ Competição não encontrada na lista")
                    errorMessage = "Competição não encontrada"
                }
            } else {
                Log.w("EachCompetitionScreen", "⚠️ Falha ao buscar competições: ${resposta.mensagem}")
                errorMessage = resposta.mensagem
            }
        } catch (e: Exception) {
            Log.e("EachCompetitionScreen", "❌ Erro ao buscar competição", e)
            errorMessage = "Erro: ${e.message}"
        }
    }

    LaunchedEffect(competitionId) {
        isLoading = true
        loadCompetition()
        isLoading = false
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                loadCompetition()
                isRefreshing = false
            }
        }
    )

    val currentParticipant = competition?.participantes?.find { it.idUsuario == userId }
    val canCheckIn = canCheckInToday(currentParticipant?.ultimoCheckin)

    if (showCheckinDialog && competition != null) {
        CompetitionCheckinConfirmationDialog(
            competitionName = competition!!.nome,
            onDismiss = { showCheckinDialog = false },
            onConfirm = {
                showCheckinDialog = false
                scope.launch {
                    val resposta = NetworkManager.realizarCheckinCompeticao(competitionId, userId)
                    if (resposta.sucesso) {
                        Toast.makeText(
                            context,
                            "✅ Check-in realizado!",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadCompetition()
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
                title = { Text(competition?.nome ?: "Carregando...") },
                navigationIcon = {
                    IconButton(onClick = { context?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (competition != null) {
                        IconButton(onClick = {
                            val intent = Intent(context, CompetitionConfigActivity::class.java).apply {
                                putExtra("COMPETITION_ID", competitionId)
                            }
                            context?.startActivity(intent)
                        }) {
                            Icon(Icons.Filled.Settings, contentDescription = "Editar Competição")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            if (competition != null && canCheckIn) {
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

                competition != null -> {
                    CompetitionDetailsContent(
                        competition = competition!!,
                        userId = userId,
                        canCheckIn = canCheckIn
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
}

@Composable
fun CompetitionCheckinConfirmationDialog(
    competitionName: String,
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
                    text = "Você realmente cumpriu seu objetivo na competição",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"$competitionName\"",
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
                    text = "🏆 Seus competidores contam com sua honestidade!",
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
fun CompetitionDetailsContent(
    competition: DocumentoCompeticao,
    userId: String,
    canCheckIn: Boolean
) {
    val context = LocalContext.current

    val sortedParticipants = remember(competition.participantes) {
        competition.participantes.sortedByDescending { it.sequencia ?: 0 }
    }

    val topThree = sortedParticipants.take(3)
    val remainingParticipants = sortedParticipants.drop(3)
    val currentParticipant = competition.participantes.find { it.idUsuario == userId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(getCompetitionIconResourceId(competition.idIcone)),
                    contentDescription = "Ícone da competição",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = competition.nome,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Código: ${competition.codigo}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                        text = "✅ Check-in de hoje já realizado!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Informações",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DetailRow(
                    label = "Sua sequência:",
                    value = "${currentParticipant?.sequencia ?: 0} dias",
                    valueColor = MaterialTheme.colorScheme.primary
                )

                DetailRow(
                    label = "Total de Participantes:",
                    value = "${competition.participantes.size}"
                )

                val criadorNome = competition.participantes
                    .find { it.idUsuario == competition.idCriador }?.apelidoUsuario ?: "Admin"

                DetailRow(
                    label = "Criador:",
                    value = criadorNome
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (topThree.isNotEmpty()) {
            Text(
                text = "🏆 Pódio",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold
            )
            CompetitionPodiumReal(topParticipants = topThree, context = context)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (remainingParticipants.isNotEmpty()) {
            Text(
                text = "📋 Outros Participantes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold
            )

            remainingParticipants.forEach { participant ->
                ParticipantItemReal(
                    participant = participant,
                    context = context
                )
            }
        } else if (competition.participantes.isEmpty()) {
            Text(
                text = "Nenhum participante ainda",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun CompetitionPodiumReal(
    topParticipants: List<DocumentoParticipante>,
    context: Context
) {
    val podiumColor = MaterialTheme.colorScheme.primaryContainer
    val gold = Color(0xFFD4AF37)

    val second = topParticipants.getOrNull(1)
    val first = topParticipants.getOrNull(0)
    val third = topParticipants.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        PodiumRankReal(
            participant = second,
            rank = 2,
            heightMultiplier = 0.8f,
            containerColor = podiumColor,
            context = context
        )
        Spacer(Modifier.width(8.dp))
        PodiumRankReal(
            participant = first,
            rank = 1,
            heightMultiplier = 1.0f,
            containerColor = gold,
            context = context
        )
        Spacer(Modifier.width(8.dp))
        PodiumRankReal(
            participant = third,
            rank = 3,
            heightMultiplier = 0.6f,
            containerColor = podiumColor,
            context = context
        )
    }
}

@Composable
fun RowScope.PodiumRankReal(
    participant: DocumentoParticipante?,
    rank: Int,
    heightMultiplier: Float,
    containerColor: Color,
    context: Context
) {
    val height = 150.dp * heightMultiplier
    val width = 80.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(width)
    ) {
        if (participant != null) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
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
                        color = MaterialTheme.colorScheme.onTertiary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = participant.apelidoUsuario,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            val sequencia = participant.sequencia ?: 0
            val plantaRes = getPlantaDrawableId(
                context = context,
                sequenciaCheckin = sequencia,
                idPlantaFinal = null
            )

            Image(
                painter = painterResource(plantaRes),
                contentDescription = "Planta do participante",
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = "$sequencia dias",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        } else {
            Spacer(modifier = Modifier.height(54.dp))
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(containerColor),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = "#$rank",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (valueColor != Color.Unspecified) valueColor else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ParticipantItemReal(
    participant: DocumentoParticipante,
    context: Context
) {
    val sequencia = participant.sequencia ?: 0

    val plantaRes = getPlantaDrawableId(
        context = context,
        sequenciaCheckin = sequencia,
        idPlantaFinal = null
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Participante",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    participant.apelidoUsuario,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(plantaRes),
                    contentDescription = "Planta",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$sequencia dias",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}