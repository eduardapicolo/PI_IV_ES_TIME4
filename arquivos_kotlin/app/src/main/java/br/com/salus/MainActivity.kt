@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

// Import da sua Splash Screen (garanta que o arquivo existe)
import br.com.salus.SplashScreenVideo

// Imports das "páginas" que vamos "puxar"
import br.com.salus.CompetitionsContent
import br.com.salus.CompetitionsFabContent
import br.com.salus.HabitsContent
import br.com.salus.HabitsFabContent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.salus.ui.theme.SalusTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreenVideo(
                                onVideoEnded = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            MainAppScreen()
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun SplashScreenVideo(onVideoEnded: () -> Unit) {
//    LaunchedEffect(Unit) {
//        kotlinx.coroutines.delay(1000)
//        onVideoEnded()
 //   }
//   Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
 //       Text("Sua Animação/Vídeo de Splash aqui", style = MaterialTheme.typography.headlineSmall)
 //   }
//}


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
fun MainAppScreen() {
    var selectedScreen by remember { mutableStateOf(Screen.Habits.route) }

    val currentTitle = if (selectedScreen == Screen.Habits.route) "Meus Hábitos" else "Competições"

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { TopBarContent(title = currentTitle) },
        bottomBar = { BottomBarContent(selectedScreen) { selectedScreen = it } },
        floatingActionButton = {
            if (selectedScreen == Screen.Habits.route) {
                HabitsFabContent()
            } else {
                CompetitionsFabContent()
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (selectedScreen) {
                Screen.Habits.route -> HabitsContent()
                Screen.Competitions.route -> CompetitionsContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarContent(title: String) {
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
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

// --- TELA 1: LOGIN ---
@Composable
fun MainAppScreenPreview() {
    SalusTheme {
        MainAppScreen()
    }
}