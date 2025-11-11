package br.com.salus

import android.widget.Toast
import br.com.salus.mockCompetitionsList
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

// Imports das funções de utilitários (necessários para compilar)
import br.com.salus.CompetitionIconDialog
import br.com.salus.getCompetitionIconResourceId

class CompetitionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalusApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalusApp() {
    val context = LocalContext.current

    val competitionsList = mockCompetitionsList

    var showCreateDialog by remember { mutableStateOf(false) }

    SalusTheme {
        if (showCreateDialog) {
            CreateCompetitionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, duration, iconId ->
                    val newCompetition = Competition(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        streak = 0,
                        competitors = listOf(Competitor("eu", "Eu")),
                        iconId = iconId
                    )
                    mockCompetitionsList.add(0, newCompetition)
                    Toast.makeText(context, "Simulado: '$name' criada!", Toast.LENGTH_SHORT).show()

                    showCreateDialog = false
                }
            )
        }

        Scaffold(
            topBar = {
                SalusTopAppBar(onProfileClick = {
                    Toast.makeText(context, "Abrir Perfil", Toast.LENGTH_SHORT).show()
                })
            },
            bottomBar = { SalusBottomAppBar() },
            floatingActionButton = {
                SalusFAB(onCreateClick = {
                    showCreateDialog = true
                })
            },
            floatingActionButtonPosition = FabPosition.Center,
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CompetitionsScreen(
                        competitions = competitionsList,
                        onCompetitionClick = { competition ->
                            Toast.makeText(context, "Abrindo ${competition.name}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalusTopAppBar(onProfileClick: () -> Unit) {
    TopAppBar(
        title = { Text("Competições", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
fun SalusBottomAppBar() {
    val context = LocalContext.current
    val items = listOf(
        BottomNavItem("Detalhes", Icons.Default.List, "competitions"),
        BottomNavItem("Classif.", Icons.Default.EmojiEvents, "rankings"),
        BottomNavItem("Amigos", Icons.Default.Groups, "friends_list")
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = item.route == "competitions",
                onClick = {
                    if (item.route != "competitions") {
                        Toast.makeText(context, "Abrir ${item.label}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

data class BottomNavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String)

@Composable
fun SalusFAB(onCreateClick: () -> Unit) {
    FloatingActionButton(
        onClick = onCreateClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Add, contentDescription = "Criar")
    }
}

@Composable
fun CompetitionsScreen(competitions: List<Competition>, onCompetitionClick: (Competition) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(competitions) { competition ->
            CompetitionCard(competition = competition, onCardClick = { onCompetitionClick(competition) })
        }
    }
}

@Composable
fun CompetitionCard(competition: Competition, onCardClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                IconButton(onClick = {
                    Toast.makeText(context, "Ajustes de ${competition.name}", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                }
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

// --- PREVIEWS ---

@Preview(showBackground = true, name = "Tela Principal (App)")
@Composable
fun SalusAppPreview() {
    SalusTheme {
        SalusApp()
    }
}

@Preview(showBackground = true, name = "Card de Competição")
@Composable
fun CompetitionCardPreview() {
    val previewCompetitors = listOf(
        Competitor("1", "Ana"),
        Competitor("2", "Bruno"),
        Competitor("3", "Carla")
    )
    val previewCompetition = Competition(
        id = "p1",
        name = "Desafio de Foco",
        streak = 7,
        competitors = previewCompetitors,
        iconId = 1
    )

    SalusTheme {
        CompetitionCard(
            competition = previewCompetition,
            onCardClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dialog de Criação")
@Composable
fun CreateDialogPreview() {
    SalusTheme {
        CreateCompetitionDialog(
            onDismiss = {},
            onCreate = {_, _, _ ->}
        )
    }
}