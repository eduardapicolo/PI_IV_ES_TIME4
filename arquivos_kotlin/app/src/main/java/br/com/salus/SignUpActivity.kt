@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.salus.ui.theme.SalusTheme

// --- CORREÇÃO: Imports do SignUpUtilities.kt ---
// Adicionamos estas linhas para que o arquivo encontre as funções
import br.com.salus.InputBox
import br.com.salus.PasswordInputBox
import br.com.salus.PasswordRequirement
import br.com.salus.isEmailValid
import br.com.salus.ProfilePictureDialog
import br.com.salus.getProfilePictureResourceId
import kotlinx.coroutines.launch

class SignUpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusTheme {
                SignUpFlow()
            }
        }
    }
}

@Preview
@Composable
fun PreviewSignUp(){
    SalusTheme {
        SignUpFlow()
    }
}

@Composable
fun SignUpFlow(){
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "userData") {

        composable("userData") { UserDataScreen(navController) }

        composable(
            route = "userProfile/{name}/{email}/{password}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("password") { type = NavType.StringType }
            )
        ) { entry ->
            val name = entry.arguments?.getString("name") ?: ""
            val email = entry.arguments?.getString("email") ?: ""
            val password = entry.arguments?.getString("password") ?: ""

            UserProfileScreen(navController, name, email, password)
        }
    }
}

@Composable
fun UserDataScreen(navController: NavController) {
    val activity = LocalContext.current as? Activity

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmedPassword by remember { mutableStateOf("") }

    val hasMinLength by remember { derivedStateOf { password.length >= 8 } }
    val hasLowercase by remember { derivedStateOf { password.any { it.isLowerCase() } } }
    val hasUppercase by remember { derivedStateOf { password.any { it.isUpperCase() } } }
    val hasNumber by remember { derivedStateOf { password.any { it.isDigit() } } }
    val hasSpecialCharacter by remember { derivedStateOf { password.any { it in "!@#$%" } } }

    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() &&
                    isEmailValid(email) &&
                    hasMinLength && hasLowercase && hasUppercase && hasNumber && hasSpecialCharacter &&
                    password == confirmedPassword
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                IconButton(
                    onClick = { activity?.finish() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        modifier = Modifier.size(36.dp),
                        contentDescription = "Voltar"
                    )
                }

                Image(
                    painter = painterResource(R.drawable.salus_green),
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Logo Salus"
                )
            }

            Text(
                text = "Cadastro",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 24.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                InputBox(name, { newName -> name = newName}, "Nome")

                Spacer(modifier = Modifier.height(16.dp))

                InputBox(email, { newEmail -> email = newEmail}, "E-mail")

                Spacer(modifier = Modifier.height(16.dp))

                PasswordInputBox(password, { newPassword -> password = newPassword}, "Senha")

                Spacer(modifier = Modifier.height(8.dp))

                if (password != "") {
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        PasswordRequirement("Mínimo de 8 caracteres", hasMinLength)
                        PasswordRequirement("1 letra minúscula (a-z)", hasLowercase)
                        PasswordRequirement("1 letra maiúscula (A-Z)", hasUppercase)
                        PasswordRequirement("1 número (0-9)", hasNumber)
                        PasswordRequirement("1 caractere especial (!@#$%)", hasSpecialCharacter)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                PasswordInputBox(confirmedPassword, { newConfirmedPassword -> confirmedPassword = newConfirmedPassword}, "Confirmar senha")
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "Ao criar uma conta, você concorda com nossos\nTermos de Uso e Política de Privacidade.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .padding(bottom = 16.dp)
            )

            Button(
                onClick = { navController.navigate("userProfile/$name/$email/$password") },
                enabled = isFormValid,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun UserProfileScreen(navController: NavController, name: String, email: String, password: String) {
    var profilePicture by remember { mutableIntStateOf(1) }
    var username by remember { mutableStateOf(name) }

    var showDialog by remember { mutableStateOf(false) }

    var coroutineScope = rememberCoroutineScope()
    var context = LocalContext.current
    var isCreatingAccount by remember { mutableStateOf(false) }

    if (showDialog) {
        ProfilePictureDialog(
            onDismiss = {showDialog = false},
            onPictureSelected = { id ->
                profilePicture = id
                showDialog = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        modifier = Modifier.size(36.dp),
                        contentDescription = "Voltar"
                    )
                }

                Image(
                    painter = painterResource(R.drawable.salus_green),
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Logo Salus"
                )
            }

            Text(
                text = "Personalize seu\nperfil",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 24.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Escolha como você aparecerá para seus amigos",
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(64.dp))

            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { showDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = getProfilePictureResourceId(profilePicture)),
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
                        contentDescription = "Editar foto de perfil",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                InputBox(username, { newUsername -> username = newUsername}, "Seu apelido")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isCreatingAccount = true

                    coroutineScope.launch {
                        val resposta = NetworkManager.userSignUp(
                            nome = name,
                            email = email,
                            senha = password,
                            apelido = username,
                            idFotoPerfil = profilePicture
                        )

                        if (resposta.sucesso) {
                            Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                            isCreatingAccount = false
                            mudarTelaFinish(context, MainAppScreen::class.java,resposta.userId)
                        }
                        else {
                            Toast.makeText(context, resposta.mensagem, Toast.LENGTH_SHORT).show()
                            isCreatingAccount = false
                        }
                        isCreatingAccount = false
                    }

                },
                enabled = username.isNotBlank() && !isCreatingAccount,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Concluir",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}