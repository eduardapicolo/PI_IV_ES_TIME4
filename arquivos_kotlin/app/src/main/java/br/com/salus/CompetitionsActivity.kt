package br.com.salus

import android.content.Intent
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.salus.ui.theme.SalusTheme
import java.util.UUID

@Composable
fun CompetitionsFabContent() {
    var showCreateCompetitionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showCreateCompetitionDialog) {
        CreateCompetitionDialog(
            onDismiss = { showCreateCompetitionDialog = false },
            onCreate = { name, duration, iconId ->
                val durationInt = duration.toIntOrNull() ?: 7

                val newCompetition = Competition(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    streak = 0,
                    competitors = listOf(Competitor("eu", "Eu")),
                    iconId = iconId,
                    durationDays = durationInt,
                    participants = listOf(
                        Participant(
                            name = "Eu",
                            currentStreak = 0
                        )
                    )
                )
                mockCompetitionsList.add(0, newCompetition)
                Toast.makeText(context, "Simulado: '$name' criada!", Toast.LENGTH_SHORT).show()
                showCreateCompetitionDialog = false
            }
        )
    }

    FloatingActionButton(
        onClick = { showCreateCompetitionDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Criar nova competição")
    }
}

@Composable
fun CompetitionsContent() {
    val context = LocalContext.current
    val competitionsList = mockCompetitionsList

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(competitionsList) { competition ->
            CompetitionCard(
                competition = competition,
                onCardClick = {
                    val intent = Intent(context, EachCompetitionActivity::class.java).apply {
                        putExtra(EachCompetitionActivity.EXTRA_COMPETITION_ID, competition.id)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun CreateCompetitionDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, duration: String, iconId: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var selectedIconId by remember { mutableStateOf(1) }
    var showIconPicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

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
        onDismissRequest = onDismiss,
        title = { Text("Nova Competição", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { showIconPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = getCompetitionIconResourceId(selectedIconId)),
                        contentDescription = "Ícone selecionado",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(60.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(20.dp)
                    )
                }
                Text("Toque para alterar o ícone", style = MaterialTheme.typography.bodySmall)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Competição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duração (em dias)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && duration.isNotBlank()) {
                        onCreate(name, duration, selectedIconId)
                    } else {
                        Toast.makeText(context, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Criar")
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
fun CompetitionCard(competition: Competition, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = competition.name, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = getCompetitionIconResourceId(competition.iconId)),
                    contentDescription = "Ícone",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(60.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Sua sequência: ${competition.streak} dias", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Competidores:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            CompetitorsHorizontalList(competitors = competition.competitors)
            Spacer(modifier = Modifier.height(24.dp))
            // Botão mantido para fins de UX, mas o clique faz a mesma navegação da Card
            Button(onClick = onCardClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                Text("Ver Detalhes")
            }
        }
    }
}

@Composable
fun CompetitorsHorizontalList(competitors: List<Competitor>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
        items(competitors) { competitor -> CompetitorItem(competitor) }
    }
}

@Composable
fun CompetitorItem(competitor: Competitor) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp).clip(CircleShape), tint = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = competitor.name, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@Preview(showBackground = true, name = "Conteúdo - Tela de Competições")
@Composable
fun CompetitionsContentPreview() {
    SalusTheme {
        CompetitionsContent()
    }
}

@Preview(showBackground = true, name = "Card de Competição")
@Composable
fun CompetitionCardPreview() {
    SalusTheme {
        CompetitionCard(
            competition = mockCompetitionsList[0],
            onCardClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dialog - Criar Competição")
@Composable
fun CreateCompetitionDialogPreview() {
    SalusTheme {
        CreateCompetitionDialog(
            onDismiss = {},
            onCreate = { _, _, _ -> }
        )
    }
}