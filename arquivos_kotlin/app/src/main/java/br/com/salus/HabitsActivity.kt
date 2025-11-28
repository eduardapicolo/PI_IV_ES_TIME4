@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun HabitsFabContent(onAddClick: () -> Unit) {
    FloatingActionButton(
        onClick = onAddClick, // Chama a função que abre o dialog
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
fun HabitsContent(
    userId: String,
    refreshTrigger: Int, // Novo parâmetro
    onAddClick: () -> Unit // Novo parâmetro para o texto clicável
) {

    var listaDeHabitos by remember { mutableStateOf<List<DocumentoHabito>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // O LaunchedEffect agora "vigia" o userId E o refreshTrigger
    LaunchedEffect(key1 = userId, key2 = refreshTrigger) {
        isLoading = true
        // Pequeno delay opcional para sensação de refresh se for muito rápido, mas não obrigatório
        val resposta = NetworkManager.getHabitos(userId)

        if (resposta.sucesso) {
            listaDeHabitos = resposta.habitos ?: emptyList()
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize()) {

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
        else if (listaDeHabitos.isEmpty()) {
            // ... (Seu código de imagem vazia continua igual aqui) ...
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                // Mantenha a sua Image aqui (R.drawable.empty_pile)...
                Image(
                    painter = painterResource(R.drawable.empty_pile),
                    contentDescription = "Vazio",
                    modifier = Modifier.width(500.dp).offset(y = (-100).dp)
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.offset(y = (-150).dp)
                ) {
                    Text(
                        text = "Está muito vazio aqui...",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Adicione um novo hábito.",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onAddClick() // AGORA CHAMA O POP-UP
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaDeHabitos) { habito ->
                    HabitoItemCard(habito)
                }
            }
        }
    }
}

@Composable
fun HabitoItemCard(habito: DocumentoHabito) {
    val cardBackgroundColor = Color(0xFFF7F4D9)

    val diasSequencia = habito.sequenciaCheckin ?: 0

    val iconRes = if (diasSequencia < 7) {
        R.drawable.empty_pile
    } else {
        R.drawable.competition_icon3 // ALTERAR PRA FOTO DA PLANTA MAIOR
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // LADO ESQUERDO: Textos
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habito.nome,
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))


                Text(
                    text = buildAnnotatedString {
                        append("Sequência: ")
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$diasSequencia")
                        }
                        append(" dias")
                    },
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            // LADO DIREITO: Ícone da planta dentro do círculo branco
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = "Status do hábito",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    userId: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Variável para guardar o texto digitado
    var habitName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            // Fecha se clicar fora, mas apenas se não estiver carregando
            if (!isLoading) onDismiss()
        },
        title = {
            Text(text = "Novo Hábito", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Qual hábito gostarias de iniciar?")
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de texto para o nome
                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Nome do hábito ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            // Chama o teu NetworkManager existente
                            val resposta = NetworkManager.newHabit(habitName, userId)

                            isLoading = false

                            if (resposta.sucesso) {
                                Toast.makeText(context, "Hábito criado!", Toast.LENGTH_SHORT).show()
                                onSuccess() // Avisa que deu certo para fechar e atualizar
                            } else {
                                Toast.makeText(context, resposta.mensagem, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Digite um nome para o hábito", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Adicionar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}