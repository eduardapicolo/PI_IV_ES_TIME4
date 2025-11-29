@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
        onClick = onAddClick, 
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
    refreshTrigger: Int, 
    onAddClick: () -> Unit 
) {

    var listaDeHabitos by remember { mutableStateOf<List<DocumentoHabito>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = userId, key2 = refreshTrigger) {
        isLoading = true
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                Image(
                    painter = painterResource(R.drawable.estagio_1),
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
                            onAddClick()
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

    val context = LocalContext.current
    val diasSequencia = habito.sequenciaCheckin ?: 0

    val imagemPlantaRes = getPlantaDrawableId(
        context = context,
        sequenciaCheckin = diasSequencia,
        idPlantaFinal = habito.idFotoPlanta
    )

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

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imagemPlantaRes),
                    contentDescription = "Status do hábito",
                    modifier = Modifier.size(200.dp)
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


    var habitName by remember { mutableStateOf("") }
    var selectedPlantId by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }

    val opcoesDePlantas = listOf(1,2,3,4,5,6)

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(text = "Novo Hábito", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Qual hábito você quer começar?")
                Spacer(modifier = Modifier.height(8.dp))


                OutlinedTextField(
                    value = habitName,
                    onValueChange = { habitName = it },
                    label = { Text("Nome do hábito ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Escolha sua planta companheira:")

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(opcoesDePlantas) { idPlanta ->
                        val resourceId = when (idPlanta) {
                            1 -> R.drawable.planta_final_1
                            2 -> R.drawable.planta_final_2
                            3 -> R.drawable.planta_final_3
                            4 -> R.drawable.planta_final_4
                            5 -> R.drawable.planta_final_5
                            6 -> R.drawable.planta_final_6
                            else -> R.drawable.planta_final_1
                        }

                        val finalResId = if (resourceId != 0) resourceId else R.drawable.planta_final_1

                        val isSelected = (selectedPlantId == idPlanta)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedPlantId = idPlanta }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = finalResId),
                                contentDescription = "Opção de planta $idPlanta",
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitName.isNotBlank()) {
                        scope.launch {
                            isLoading = true

                            val resposta = NetworkManager.newHabit(habitName, userId, selectedPlantId)

                            isLoading = false

                            if (resposta.sucesso) {
                                Toast.makeText(context, "Hábito criado!", Toast.LENGTH_SHORT).show()
                                onSuccess() 
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
