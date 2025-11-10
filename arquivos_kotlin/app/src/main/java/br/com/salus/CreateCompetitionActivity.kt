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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.salus.ui.theme.SalusTheme

class CreateCompetitionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SalusTheme {
                CreateCompetitionScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCompetitionScreen() {
    val context = LocalContext.current as? Activity

    var competitionName by remember { mutableStateOf("") }
    var habitName by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    val isFormValid by remember {
        derivedStateOf { competitionName.isNotBlank() && habitName.isNotBlank() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova Competição") },
                navigationIcon = {
                    IconButton(onClick = { context?.finish() }) { // Fecha a Activity
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
                text = "Iniciar Nova Competição",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            TextField(
                value = competitionName,
                onValueChange = { competitionName = it },
                label = { Text("Nome da Competição*") },
                placeholder = { Text("Ex: 'Desafio 21 Dias'") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            TextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Hábito (meta)*") },
                placeholder = { Text("Ex: 'Meditar 10 min'") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
            )

            TextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duração (em dias)") },
                placeholder = { Text("Ex: '21'") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Convidar Amigos (em breve...)",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    Toast.makeText(context, "Competição '$competitionName' criada!", Toast.LENGTH_SHORT).show()
                    context?.finish()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Criar Competição", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true)
@Composable
fun CreateCompetitionScreenPreview() {
    SalusTheme {
        CreateCompetitionScreen()
    }
}