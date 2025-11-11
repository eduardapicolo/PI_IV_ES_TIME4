package br.com.salus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.salus.ui.theme.SalusTheme


data class Competitor(
    val id: String,
    val name: String,
)

data class Competition(
    val id: String,
    val name: String,
    val streak: Int,
    val competitors: List<Competitor>
)

val mockCompetitors = listOf(
    Competitor("1", "Ana"),
    Competitor("2", "Bruno"),
    Competitor("3", "Carla"),
    Competitor("4", "Daniel"),
    Competitor("5", "Elisa")
)

val mockCompetitions = listOf(
    Competition("c1", "21 Dias de Foco", 5, mockCompetitors.shuffled().take(3)),
    Competition("c2", "Desafio da Meditação", 12, mockCompetitors.shuffled().take(4)),
    Competition("c3", "Manhãs Milagrosas", 2, mockCompetitors.shuffled().take(5))
)

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

    SalusTheme {
        Scaffold(
            topBar = {
                SalusTopAppBar(onProfileClick = {
                    Toast.makeText(context, "Abrir Perfil", Toast.LENGTH_SHORT).show()
                    // TODO: context.startActivity(Intent(context, ProfileActivity::class.java))
                })
            },
            bottomBar = { SalusBottomAppBar() },
            floatingActionButton = {
                SalusFAB(onCreateClick = {
                    context.startActivity(Intent(context, CreateCompetitionActivity::class.java))
                })
            },
            floatingActionButtonPosition = FabPosition.Center,
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CompetitionsScreen(
                        competitions = mockCompetitions,
                        onCompetitionClick = { competition ->
                            val intent = Intent(context, EachCompetitionActivity::class.java)
                            intent.putExtra(EachCompetitionActivity.EXTRA_COMPETITION_ID, competition.id)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalusTopAppBar(onProfileClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Competições",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Perfil do Usuário",
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = item.route == "competitions", // Esta é a tela de "Detalhes"
                onClick = {
                    if (item.route != "competitions") {
                        // TODO: Chamar as Activities de Ranking e Amigos
                        Toast.makeText(context, "Abrir tela de ${item.label}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
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
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Criar nova competição")
    }
}

@Composable
fun CompetitionsScreen(
    competitions: List<Competition>,
    onCompetitionClick: (Competition) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(competitions) { competition ->
            CompetitionCard(
                competition = competition,
                onCardClick = { onCompetitionClick(competition) }
            )
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = competition.name,
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = {
                    Toast.makeText(context, "Ajustes para ${competition.name}", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ajustes da Competição")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFlorist,
                    contentDescription = "Ícone de Flor",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Sua sequência: ${competition.streak} dias",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Competidores:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            CompetitorsHorizontalList(competitors = competition.competitors)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCardClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ver Detalhes",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun CompetitorsHorizontalList(competitors: List<Competitor>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(competitors) { competitor ->
            CompetitorItem(competitor)
        }
    }
}

@Composable
fun CompetitorItem(competitor: Competitor) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar de ${competitor.name}",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = competitor.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true)
@Composable
fun CompetitionCardPreview() {
    SalusTheme {
        CompetitionCard(
            competition = mockCompetitions[0],
            onCardClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SalusAppPreview() {
    SalusApp()
}