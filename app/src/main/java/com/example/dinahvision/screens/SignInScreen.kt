package com.example.dinahvision.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SignInScreen(modifier: Modifier, onClickLoginCallBack: () -> Unit, onClickRegisterCallBack: () -> Unit){
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Tela Login")
            Button(onClick = onClickLoginCallBack) {
                Text(text = "Entrar")
            }
            Button(onClick = onClickRegisterCallBack) {
                Text(text = "Cadastre-se")
            }
        }
    }

}