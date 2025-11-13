package br.com.salus

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
import androidx.compose.material.icons.filled.Build
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
import br.com.salus.ui.theme.SalusTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// ** Assumindo que getCompetitionIconResourceId está definida em CompetitionUtilities.kt **
// import br.com.salus.getCompetitionIconResourceId

class EachCompetitionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_COMPETITION_ID = "COMPETITION_ID"
    }

    private var competition: Competition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val competitionId = intent.getStringExtra(EXTRA_COMPETITION_ID)

        // Busca a competição pelo ID passado no Intent
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

    // ... [Seu código para competition == null está aqui]

    // Tratamento inicial de competition == null
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

    // Calculando quantos dias faltam
    val daysLeft = remember {
        try {
            val today = LocalDate.now()
            // Assumindo que a competição começou hoje e durará durationDays
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
                        val intent = Intent(context, CompetitionConfigActivity::class.java).apply {
                            // TODO: Passar o ID para edição
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

            // --- 1. CABEÇALHO COM NOME, ÍCONE E STREAK ---
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                // 🔄 CORREÇÃO: Usando o ícone real da competição (iconId)
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

            // --- 2. DURAÇÃO E TEMPO RESTANTE (Card) ---
            // ... (restante do código da DURAÇÃO inalterado)

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

            // --- 3. PARTICIPANTES (Lista) ---
            Text(
                text = "Participantes",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                textAlign = TextAlign.Start
            )

            if (competition.participants.isNotEmpty()) {
                competition.participants.forEach { participant ->
                    // 🔄 CORREÇÃO: Passando a sequência (assumindo que score é a sequência)
                    ParticipantItem(name = participant.name, currentStreak = participant.currentStreak)
                }
            } else {
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

// 🔄 CORREÇÃO: Renomeado "score" para "currentStreak" para clareza e exibição
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
            // 🔄 CORREÇÃO: Exibindo a Sequência (Streak)
            Text(
                text = "Sequência: $currentStreak dias",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun EachCompetitionScreenPreview() {
    SalusTheme {
        EachCompetitionScreen(
            competition = Competition(
                id = "1",
                name = "30 Dias de Hidratação",
                streak = 15,
                durationDays = 30,
                // Assumindo que 'Participant' foi atualizado para usar 'currentStreak' em vez de 'score'
                participants = listOf(
                    Participant("Alice", 15),
                    Participant("Bob", 12),
                    Participant("Charlie", 9)
                ),
                competitors = emptyList(),
                iconId = 1
            )
        )
    }
}