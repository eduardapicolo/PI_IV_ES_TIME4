package br.com.salus

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.NetworkManager.deletarConta
import br.com.salus.ui.theme.SalusTheme
import kotlinx.coroutines.launch

class EditAccountActivity : ComponentActivity() {
    private val USER_ID_KEY = "br.com.salus.USER_ID"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val currentUserId = intent.getStringExtra(USER_ID_KEY) ?: ""

        setContent {
            SalusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    EditAccountScreen(currentUserId)
                }
            }
        }
    }
}

@Composable
fun DialogConfirmacaoExclusao(
    onDismiss: () -> Unit, // O que fazer ao cancelar/clicar fora
    onConfirm: () -> Unit  // O que fazer ao confirmar (Excluir)
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Excluir Conta")
        },
        text = {
            Text("Tem a certeza que deseja excluir a sua conta? Esta ação é irreversível e todos os seus dados serão perdidos.")
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm() }
            ) {
                Text("Sim, Excluir", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
fun EditAccountScreen(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var apelido by remember { mutableStateOf("") }
    var apelidoBanco by remember { mutableStateOf("") }
    var profilePictureId by remember { mutableIntStateOf(1) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            val resposta = NetworkManager.buscarUsuario(currentUserId)
            if (resposta.sucesso && resposta.documentoParticipante != null) {
                apelido = resposta.documentoParticipante.apelidoUsuario
                apelidoBanco = resposta.documentoParticipante.apelidoUsuario
                profilePictureId = resposta.documentoParticipante.idFotoPerfil ?: 1
            } else {
                Log.d("TESTEEDITAR", currentUserId)
                Toast.makeText(context, "Erro ao carregar: ${resposta.mensagem}", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        } else {
            isLoading = false
            Toast.makeText(context, "ID de usuário inválido", Toast.LENGTH_SHORT).show()
            (context as? Activity)?.finish()
        }
    }

    if (showAvatarDialog) {
        ProfilePictureDialog(
            onDismiss = { showAvatarDialog = false },
            onPictureSelected = { id ->
                profilePictureId = id
                showAvatarDialog = false
            }
        )
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                IconButton(
                    onClick = { mudarTelaFinish(context, MainAppScreen::class.java, currentUserId) },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Editar Perfil",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { showAvatarDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getProfilePictureResourceId(profilePictureId)),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Text(
                text = "Toque para alterar a foto",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            InputBox(
                value = apelido,
                onValueChange = { apelido = it },
                label = "Apelido"
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true

                        if (apelido == apelidoBanco) {
                            val resposta = NetworkManager.editarConta(
                                userId = currentUserId,
                                novoApelido = null,
                                novoIdFotoPerfil = profilePictureId
                            )

                            isLoading = false

                            if (resposta.sucesso) {
                                Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                                mudarTelaFinish(context, MainAppScreen::class.java, currentUserId)
                            } else {
                                Toast.makeText(context, "Erro: ${resposta.mensagem}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val resposta = NetworkManager.editarConta(
                                userId = currentUserId,
                                novoApelido = apelido,
                                novoIdFotoPerfil = profilePictureId
                            )

                            isLoading = false

                            if (resposta.sucesso) {
                                Toast.makeText(context, "Perfil atualizado!", Toast.LENGTH_SHORT).show()
                                mudarTelaFinish(context, MainAppScreen::class.java, currentUserId)
                            } else {
                                Toast.makeText(context, "Erro: ${resposta.mensagem}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = apelido.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Salvar Alterações",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    showDeleteDialog = true
                }
            ) {
                Text(
                    text = "Excluir minha conta",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (showDeleteDialog) {
            DialogConfirmacaoExclusao(
                onDismiss = {
                    showDeleteDialog = false
                },
                onConfirm = {
                    showDeleteDialog = false

                    scope.launch {
                        isLoading = true
                        val resposta = NetworkManager.deletarConta(currentUserId)

                        if (resposta.sucesso) {
                            Toast.makeText(context, "Conta excluída.", Toast.LENGTH_LONG).show()
                            mudarTela(context, MainActivity::class.java)
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Erro ao excluir: ${resposta.mensagem}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }
}