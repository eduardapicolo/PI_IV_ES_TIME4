@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.salus

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.salus.ui.theme.SalusTheme
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.CircularProgressIndicator
// --- CORREÇÃO AQUI ---
// Importa as funções "oficiais" do seu arquivo de utilitários
import br.com.salus.InputBox
import br.com.salus.PasswordInputBox
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusTheme {
                SignInScreen()
            }
        }
    }
}

@Composable
fun SignInScreen() {
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val isFormValid by remember {
        derivedStateOf {
            email.isNotBlank() && password.isNotBlank()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {

            IconButton(
                onClick = { activity?.finish() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    modifier = Modifier.size(36.dp),
                    contentDescription = "Voltar"
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(R.drawable.salus_green),
                        modifier = Modifier.size(300.dp),
                        contentScale = ContentScale.Crop,
                        contentDescription = "Logo Salus"
                    )

                    Text(
                        text = "Bem vindo(a) de volta!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-20).dp),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 32.dp),
                ) {
                    InputBox(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-mail",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    PasswordInputBox(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {

                            scope.launch {
                                isLoading = true

                                val resposta = NetworkManager.userSignIn(email, password)

                                isLoading = false

                                if (resposta.sucesso) {
                                    Log.d("Login", "Sucesso! ID: ${resposta.userId}")

                                    Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                    mudarTelaFinish(context, MainAppScreen::class.java,resposta.userId)


                                } else {
                                    Toast.makeText(context, resposta.mensagem, Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = isFormValid && !isLoading,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = Color(0xFFC7DAC1),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Entrar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewSignInScreen() {
    SalusTheme {
        SignInScreen()
    }
}