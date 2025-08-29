package com.example.dinahvision

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dinahvision.screens.HomeScreen
import com.example.dinahvision.screens.SignInScreen
import com.example.dinahvision.screens.SignUpScreen
import com.example.dinahvision.ui.viewmodel.HomeViewModel

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
        composable(route = "home") {
            val homeViewModel: HomeViewModel = viewModel() //cria primeiroa instancia do homeviewmodel painho
            HomeScreen(
                viewModel = homeViewModel,
                modifier = modifier
            )
        }
    }
}