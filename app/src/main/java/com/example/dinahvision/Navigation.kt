package com.example.dinahvision

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dinahvision.screens.HomeScreen
import com.example.dinahvision.screens.SignInScreen
import com.example.dinahvision.screens.SignUpScreen

@Composable
fun Navigation(modifier: Modifier){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "signIn" ){
        composable("signIn") {
            SignInScreen(
                modifier = modifier,
                navController = navController
            )
        }
        composable("signUp") {
            // Passa só o navController, já que é o único parâmetro
            SignUpScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(modifier = modifier)
        }
    }
}