package br.com.salus

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

    val currentTitle = if (selectedScreen == Screen.Habits.route) "Meus Hábitos" else "Competições"

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = { TopBarContent(title = currentTitle) },
        bottomBar = { BottomBarContent(selectedScreen) { selectedScreen = it } },
        floatingActionButton = {
            if (selectedScreen == Screen.Habits.route) {
                HabitsFabContent(currentUserId)
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
                Screen.Habits.route -> HabitsContent(currentUserId)
                Screen.Competitions.route -> CompetitionsContent(currentUserId)
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

