package com.example.dinahvision.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dinahvision.repository.UserDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DinahVision",
            fontSize = 32.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Campo de usuário
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Usuário") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de senha
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de confirmação de senha
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirmar Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        // Mensagem de erro
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }

        // Botão de cadastro
        Button(
            onClick = {
                // Validações básicas
                when {
                    username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                        errorMessage = "Preencha todos os campos"
                    }
                    password != confirmPassword -> {
                        errorMessage = "As senhas não coincidem"
                    }
                    else -> {
                        isLoading = true
                        scope.launch {
                            try {
                                val userDAO = UserDAO()

                                // Verifica se o usuário já existe
                                val existingUser = withContext(Dispatchers.IO) {
                                    userDAO.getUserByUsername(username)
                                }

                                if (existingUser != null) {
                                    errorMessage = "Usuário já existe"
                                } else {
                                    // Cria novo usuário
                                    withContext(Dispatchers.IO) {
                                        userDAO.createUser(
                                            com.example.dinahvision.models.User(
                                                username = username,
                                                password = password
                                            )
                                        )
                                    }

                                    // Redireciona para login após cadastro bem-sucedido
                                    navController.navigate("signIn") {
                                        popUpTo("signUp") { inclusive = true }
                                    }
                                }
                            } catch (e: Exception) {
                                errorMessage = "Erro ao cadastrar: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Criando conta..." else "Cadastrar")
        }

        // Link para login
        TextButton(
            onClick = {
                navController.navigate("signIn") {
                    popUpTo("signUp") { inclusive = true }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                buildAnnotatedString {
                    append("Já tem uma conta? ")
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append("Faça login")
                    }
                }
            )
        }
    }
}