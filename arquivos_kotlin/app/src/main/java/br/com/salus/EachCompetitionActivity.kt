package br.com.salus

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.salus.ui.theme.SalusTheme

class EachCompetitionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_COMPETITION_ID = "COMPETITION_ID"
    }

    private var competition: Competition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val competitionId = intent.getStringExtra(EXTRA_COMPETITION_ID)

        competition = mockCompetitions.find { it.id == competitionId }

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

    var name by remember { mutableStateOf(competition.name) }
    var streak by remember { mutableStateOf(competition.streak.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Competição") },
                navigationIcon = {
                    IconButton(onClick = { context?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Editando Competição",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome da Competição") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TextField(
                value = streak,
                onValueChange = { streak = it },
                label = { Text("Sequência Atual") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    Toast.makeText(context, "Alterações salvas para $name", Toast.LENGTH_SHORT).show()
                    context?.finish()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Salvar Alterações", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun EachCompetitionScreenPreview() {
    SalusTheme {
        // A preview usa os dados importados
        EachCompetitionScreen(
            competition = mockCompetitions[0] // "21 Dias de Foco"
        )
    }
}
