package br.com.salus

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme

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

        Log.d(TAG, " onCreate - Competition ID: $competitionId, User ID: $userId")

        if (competitionId == null) {
            Log.e(TAG, " Competition ID é NULL!")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EachCompetitionScreen(competitionId: String, userId: String) {
    val context = LocalContext.current as? Activity

    var competition by remember { mutableStateOf<DocumentoCompeticao?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(competitionId) {
        Log.d("EachCompetitionScreen", "🔍 Buscando competição com ID: $competitionId para User: $userId")
        isLoading = true
        errorMessage = null

        try {
            val resposta = NetworkManager.buscarCompeticoes(userId)

            if (resposta.sucesso && resposta.competicoes != null) {
                competition = resposta.competicoes!!.find { it.id == competitionId }

                if (competition != null) {
                    Log.d("EachCompetitionScreen", "✅ Competição encontrada: ${competition!!.nome}")
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
        } finally {
            isLoading = false
        }
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    CompetitionDetailsContent(competition = competition!!)
                }
            }
        }
    }
}

@Composable
fun CompetitionDetailsContent(competition: DocumentoCompeticao) {
    val sortedParticipants = remember(competition.participantes) {
        competition.participantes.sortedByDescending { it.sequencia ?: 0 }
    }

    val topThree = sortedParticipants.take(3)
    val remainingParticipants = sortedParticipants.drop(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header da competição
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
                Text(
                    text = competition.nome.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
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

        Spacer(modifier = Modifier.height(32.dp))

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
                text = "Líderes da Sequência",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start
            )
            CompetitionPodiumReal(topParticipants = topThree)
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (remainingParticipants.isNotEmpty()) {
            Text(
                text = "Outros Participantes",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start
            )

            remainingParticipants.forEach { participant ->
                ParticipantItemReal(
                    name = participant.apelidoUsuario,
                    currentStreak = participant.sequencia ?: 0
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CompetitionPodiumReal(topParticipants: List<DocumentoParticipante>) {
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
        PodiumRankReal(participant = second, rank = 2, heightMultiplier = 0.8f, containerColor = podiumColor)
        Spacer(Modifier.width(8.dp))
        PodiumRankReal(participant = first, rank = 1, heightMultiplier = 1.0f, containerColor = gold)
        Spacer(Modifier.width(8.dp))
        PodiumRankReal(participant = third, rank = 3, heightMultiplier = 0.6f, containerColor = podiumColor)
    }
}

@Composable
fun RowScope.PodiumRankReal(
    participant: DocumentoParticipante?,
    rank: Int,
    heightMultiplier: Float,
    containerColor: Color
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
                Text(
                    text = participant.apelidoUsuario.firstOrNull()?.uppercase() ?: "?",
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = participant.apelidoUsuario,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "${participant.sequencia ?: 0} dias",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
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
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = valueColor)
    }
}

@Composable
fun ParticipantItemReal(name: String, currentStreak: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = "Participante",
                    modifier = Modifier.size(24.dp).padding(end = 8.dp)
                )
                Text(name, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = "Sequência: $currentStreak dias",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}