@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

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

@Composable
fun HabitsFabContent(userId: String) {
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
fun HabitsContent(userId: String) {

    var listaDeHabitos by remember { mutableStateOf<List<DocumentoHabito>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(key1 = userId) {
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
                    painter = painterResource(R.drawable.empty_pile),
                    contentDescription = "Vazio",
                    modifier = Modifier
                        .width(500.dp)
                        .offset(y = (-100).dp)
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
                            // TODO: Aqui você colocará a navegação para a tela de criar hábito
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