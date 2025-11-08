package br.com.salus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme

/**
 * Representa os dados de um competidor.
 * No futuro, pegar do servidor o competidor.
 */
data class Competidor(
    val id: Int,
    val nome: String,
    val streak: Int,
    val isVoce: Boolean = false
)

/**
 * @param modifier O Modifier deve ser passado pelo Scaffold principal
 * (para aplicar o padding interno e evitar que o conteúdo fique debaixo da BottomBar).
 */

@Composable
fun CompetidoresContent(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    //pegar do bd
    val codigoCompeticao = "ABC123"

    //lista de exemplo apenas
    val listaDeCompetidores = remember {
        listOf(
            Competidor(id = 1, nome = "Você", streak = 3, isVoce = true),
            Competidor(id = 2, nome = "Usuário1", streak = 7),
            Competidor(id = 3, nome = "Usuário2", streak = 4),
            Competidor(id = 4, nome = "Usuário3", streak = 1),
            Competidor(id = 5, nome = "Usuário4", streak = 12),
            Competidor(id = 6, nome = "Usuário5", streak = 0)
        )
    }


    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item{
                CodigoCompeticao(
                    codigo = codigoCompeticao,
                    onCopiarClick = {
                        val clip = ClipData.newPlainText("Código", codigoCompeticao)
                        clipboardManager.setPrimaryClip(clip)
                        Toast.makeText(context, "Código copiado!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            items(listaDeCompetidores) { competidor ->
                CompetidorItem(
                    nome = competidor.nome,
                    streak = competidor.streak,
                    isVoce = competidor.isVoce
                    // Passa o avatar
                )
            }
        }

    }
}

@Composable
fun CodigoCompeticao(codigo: String, onCopiarClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
            .clickable { onCopiarClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Código da competição:",
            fontSize = 26.sp,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = codigo,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            textDecoration = TextDecoration.Underline
        )

        Text(
            text = "Clique para copiar",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun CompetidorItem (
    nome: String,
    streak: Int,
    isVoce: Boolean,
    icone: ImageVector = Icons.Outlined.Person //no futuro subistuir
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icone,
            contentDescription = "foto de perfil",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = nome,
            modifier = Modifier.weight(1f),
            fontSize = 18.sp,
            fontWeight = if (isVoce) FontWeight.Bold else FontWeight.Normal
        )

        Text(
            text = "Dias em sequência: $streak",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCompetidoresContent() {
    SalusTheme {
        // Passamos um Modifier.padding() simples para simular o espaço
        // que o Scaffold principal ocuparia.
        CompetidoresContent(modifier = Modifier.padding(top = 64.dp, bottom = 64.dp))
    }
}