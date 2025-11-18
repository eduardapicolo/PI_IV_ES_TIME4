package br.com.salus

import androidx.compose.ui.text.style.TextOverflow
import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class EachCompetitionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_COMPETITION_ID = "COMPETITION_ID"
    }

    private var competition: Competition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val competitionId = intent.getStringExtra(EXTRA_COMPETITION_ID)

        competition = mockCompetitionsList.find { it.id == competitionId }

        if (competition == null) {
            Toast.makeText(this, "Competição não encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            SalusTheme {
                EachCompetitionScreen(competition = competition)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EachCompetitionScreen(competition: Competition?) {

    val context = LocalContext.current as? Activity

    if (competition == null) {
        SalusTheme {
            Scaffold { padding ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Competição não encontrada", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        return
    }

    val sortedParticipants = remember(competition.participants) {
        competition.participants.sortedByDescending { it.currentStreak }
    }

    val topThree = sortedParticipants.take(3)
    val remainingParticipants = sortedParticipants.drop(3)

    val daysLeft = remember {
        try {
            val today = LocalDate.now()
            val endDate = today.plusDays(competition.durationDays.toLong())
            ChronoUnit.DAYS.between(today, endDate).coerceAtLeast(0)
        } catch (e: Exception) {
            competition.durationDays.toLong()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(competition.name) },
                navigationIcon = {
                    IconButton(onClick = { context?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "Abrir tela de Edição", Toast.LENGTH_SHORT).show()
                        // TODO: Implementar navegação para CompetitionConfigActivity
                        val intent = Intent(context, CompetitionConfigActivity::class.java).apply {
                        }
                        context?.startActivity(intent)
                    }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Editar Competição")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = getCompetitionIconResourceId(competition.iconId)),
                    contentDescription = "Ícone da Competição",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = competition.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sua sequência: ${competition.streak} dias",
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
                        text = "Detalhes da Duração",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    DetailRow(
                        label = "Tempo de Duração:",
                        value = "${competition.durationDays} dias"
                    )

                    DetailRow(
                        label = "Faltam para Acabar:",
                        value = "$daysLeft dias",
                        valueColor = if (daysLeft <= 7 && daysLeft > 0) MaterialTheme.colorScheme.error else Color.Unspecified
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
                CompetitionPodium(topParticipants = topThree)
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
                    ParticipantItem(name = participant.name, currentStreak = participant.currentStreak)
                }
            } else if (competition.participants.isEmpty()) {
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
}

@Composable
fun CompetitionPodium(topParticipants: List<Participant>) {
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
        PodiumRank(participant = second, rank = 2, heightMultiplier = 0.8f, containerColor = podiumColor)
        Spacer(Modifier.width(8.dp))

        PodiumRank(participant = first, rank = 1, heightMultiplier = 1.0f, containerColor = gold)
        Spacer(Modifier.width(8.dp))

        PodiumRank(participant = third, rank = 3, heightMultiplier = 0.6f, containerColor = podiumColor)
    }
}

@Composable
fun RowScope.PodiumRank(
    participant: Participant?,
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
            if (rank == 1) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = "Primeiro Lugar",
                    tint = Color.Black,
                    modifier = Modifier.size(30.dp).offset(y = 10.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.name.first().uppercase(),
                    color = MaterialTheme.colorScheme.onTertiary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(text = participant.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold, maxLines = 1
            )
            Text(
                text = "${participant.currentStreak} dias",
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
fun ParticipantItem(name: String, currentStreak: Int) {
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

@Preview(showBackground = true)
@Composable
fun EachCompetitionScreenPreview() {
    SalusTheme {
        EachCompetitionScreen(
            competition = Competition(
                id = "1",
                name = "Desafio do Pódio",
                streak = 15,
                durationDays = 30,
                participants = listOf(
                    Participant("Alice", 25),
                    Participant("Bob", 15),
                    Participant("Charlie", 5),
                    Participant("Diana", 3),
                    Participant("Eduardo", 1)
                ),
                competitors = emptyList(),
                iconId = 1
            )
        )
    }
}