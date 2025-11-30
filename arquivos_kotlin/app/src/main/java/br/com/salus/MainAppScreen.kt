@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import br.com.salus.ui.theme.SalusTheme

class MainAppScreen : ComponentActivity() {
    private val USER_ID_KEY = "br.com.salus.USER_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val currentUserId = intent.getStringExtra(USER_ID_KEY) ?: ""

        enableEdgeToEdge()
        setContent {
            SalusTheme {
                HomePage(currentUserId.toString())
            }
        }
    }
}

sealed class Screen(val route: String, val iconVector: ImageVector, val label: String) {
    object Habits : Screen("habits", Icons.Default.Home, "Meus Hábitos")
    object Competitions : Screen("competitions", Icons.Default.Star, "Competições")
}

val navItems = listOf(
    Screen.Habits,
    Screen.Competitions,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(currentUserId: String) {
    var selectedScreen by remember { mutableStateOf(Screen.Habits.route) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showTutorialDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentTitle = if (selectedScreen == Screen.Habits.route) "Meus Hábitos" else "Competições"

    if (showAddHabitDialog) {
        AddHabitDialog(
            userId = currentUserId,
            onDismiss = { showAddHabitDialog = false },
            onSuccess = {
                showAddHabitDialog = false
                refreshTrigger++
            }
        )
    }

    if (showTutorialDialog) {
        TutorialDialog(onDismiss = { showTutorialDialog = false })
    }

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            TopBarContent(
                title = currentTitle,
                onInfoClick = { showTutorialDialog = true },
                currentUserId = currentUserId
            )
        },
        bottomBar = { BottomBarContent(selectedScreen) { selectedScreen = it } },
        floatingActionButton = {
            if (selectedScreen == Screen.Habits.route) {
                HabitsFabContent(onAddClick = { showAddHabitDialog = true })
            } else {
                CompetitionsFabContent(currentUserId)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedScreen) {
                Screen.Habits.route -> HabitsContent(
                    userId = currentUserId,
                    refreshTrigger = refreshTrigger,
                    onAddClick = { showAddHabitDialog = true }
                )
                Screen.Competitions.route -> CompetitionsContent(currentUserId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(title: String, onInfoClick: () -> Unit, currentUserId: String) {
    var context = LocalContext.current

    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { mudarTelaFinish(context, EditAccountActivity::class.java, currentUserId) }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Informações do App",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun TutorialDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Entendi!",
                    modifier = Modifier.padding(vertical = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Tutorial Salus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Aprenda a usar o app para cultivar seus hábitos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                TutorialSection(
                    emoji = "🌱",
                    title = "Meus Hábitos",
                    description = "Crie e acompanhe seus hábitos diários"
                )

                TutorialStep(
                    number = "1",
                    title = "Criar um hábito",
                    description = "Toque no botão + para adicionar um novo hábito. Escolha um nome e selecione uma planta companheira que crescerá junto com você!"
                )

                TutorialStep(
                    number = "2",
                    title = "Fazer check-in",
                    description = "Todos os dias que você cumprir seu hábito, faça check-in tocando no botão verde. Sua sequência aumentará e sua planta crescerá!"
                )

                TutorialStep(
                    number = "3",
                    title = "Acompanhar progresso",
                    description = "Toque em um hábito para ver detalhes, sua sequência atual e mensagens motivacionais. Você pode ver sua planta evoluir através de 7 estágios!"
                )

                TutorialStep(
                    number = "4",
                    title = "Editar ou excluir",
                    description = "Toque em um hábito e depois no ícone de configurações para editar o nome, trocar a planta ou excluir o hábito."
                )

                Spacer(modifier = Modifier.height(16.dp))

                TutorialSection(
                    emoji = "🏆",
                    title = "Competições",
                    description = "Compita com amigos e familiares"
                )

                TutorialStep(
                    number = "1",
                    title = "Criar competição",
                    description = "Toque no botão + e escolha 'Criar competição'. Dê um nome, escolha um ícone e compartilhe o código de 6 caracteres com seus amigos."
                )

                TutorialStep(
                    number = "2",
                    title = "Entrar em competição",
                    description = "Recebeu um código? Toque no botão + e escolha 'Entrar com código'. Digite o código de 6 caracteres para participar!"
                )

                TutorialStep(
                    number = "3",
                    title = "Check-in na competição",
                    description = "Faça check-in diariamente para aumentar sua sequência. Quanto mais dias consecutivos, mais alta sua posição no pódio!"
                )

                TutorialStep(
                    number = "4",
                    title = "Ver ranking",
                    description = "Toque em uma competição para ver o pódio com os 3 primeiros colocados e a lista completa de participantes com suas sequências."
                )

                Spacer(modifier = Modifier.height(16.dp))

                TutorialSection(
                    emoji = "💡",
                    title = "Dicas Importantes",
                    description = "Para aproveitar melhor o app"
                )

                TutorialTip(
                    icon = "⏰",
                    text = "Você só pode fazer um check-in por dia. Se perder um dia, sua sequência será reiniciada!"
                )

                TutorialTip(
                    icon = "🌿",
                    text = "Sua planta cresce conforme sua sequência: 0-2 dias (semente), 3-5 (broto), 6-10 (muda), até 30+ dias (planta completa)."
                )

                TutorialTip(
                    icon = "🤝",
                    text = "Nas competições, seja honesto! Seus amigos confiam em você para fazer check-in apenas quando realmente cumprir o objetivo."
                )

                TutorialTip(
                    icon = "🔄",
                    text = "Puxe para baixo em qualquer lista para atualizar os dados e ver as mudanças mais recentes."
                )
            }
        }
    )
}

@Composable
fun TutorialSection(emoji: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "$emoji $title",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun TutorialStep(number: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = number,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun TutorialTip(icon: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun BottomBarContent(
    selectedRoute: String,
    onItemSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background
    ) {
        navItems.forEach { screen ->
            val isSelected = selectedRoute == screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(screen.route) },
                label = {
                    Text(
                        screen.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = screen.iconVector,
                        contentDescription = screen.label
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}