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
fun SignUpScreen(modifier: Modifier, onClickRegisterCallBack: () -> Unit){
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Tela cadastro")
            Button(onClick = onClickRegisterCallBack) {
                Text(text = "Registrar")
            }
        }
    }


}