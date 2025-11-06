@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.salus.ui.theme.SalusTheme


sealed class Screen(val route: String, val iconVector: ImageVector, val label: String) {
    object Habits : Screen("habits", Icons.Default.Home, "Meus Hábitos")
    object Competitions : Screen("competitions", Icons.Default.Star, "Competições")
    object Friends : Screen("friends", Icons.Default.Group, "Amigos")
}

val navItems = listOf(
    Screen.Habits,
    Screen.Competitions,
    Screen.Friends
)


class HabitsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusTheme {
                HabitsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen() {
    var selectedScreen by remember { mutableStateOf(Screen.Habits.route) }

    Scaffold(
        topBar = { TopBarContent() },
        bottomBar = { BottomBarContent(selectedScreen) { selectedScreen = it } },
        floatingActionButton = { FabContent() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedScreen) {
                Screen.Habits.route -> HabitsContent() // Conteúdo modificado
                Screen.Competitions.route -> Text("Tela de Competições", Modifier.fillMaxSize())
                Screen.Friends.route -> Text("Tela de Amigos", Modifier.fillMaxSize())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent() {
    CenterAlignedTopAppBar(
        title = { Text("Hábitos", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = { /* Ação de perfil */ }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        actions = {
            IconButton(onClick = { /* Ação de configurações */ }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurações",
                    modifier = Modifier.size(36.dp),
                    // MUDANÇA AQUI: Usando a cor primária do tema (seu verde)
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

@Composable
fun FabContent() {
    FloatingActionButton(
        onClick = { /* Navegar para a tela / pop-up de adicionar hábito */ },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Adicionar Hábito"
        )
    }
}

@Composable
fun HabitsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        Image(
            painter = painterResource(R.drawable.empty_pile),
            contentDescription = "Vazio",
            modifier = Modifier.size(300.dp)
        )
        Spacer(Modifier.height(1.dp))

        Text(
            text = "Está muito vazio aqui...",
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Adicione um novo hábito.",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { /* Ação de clicar no texto para adicionar */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHabitsScreen() {
    SalusTheme {
        HabitsScreen()
    }
}