@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.salus.ui.theme.SalusTheme

@Composable
fun HabitsFabContent() {
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
                modifier = Modifier.clickable { /* Ação de clicar */ }
            )
        }
    }
}

@Preview(showBackground = true, name = "Conteúdo - Tela de Hábitos")
@Composable
fun HabitsContentPreview() {
    SalusTheme {
        HabitsContent()
    }
}