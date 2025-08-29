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
import com.example.dinahvision.viewmodel.SignInViewModel
import com.example.dinahvision.viewmodel.SignUpViewModel
import com.example.dinahvision.viewmodel.HomeViewModel

@Composable
fun Navigation(modifier: Modifier){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "signIn" ){
        composable("signIn") {
            val signInViewModel: SignInViewModel = viewModel()
            SignInScreen(
                modifier = modifier,
                navController = navController,
                viewModel = signInViewModel
            )
        }
        composable("signUp") {
            val signUpViewModel: SignUpViewModel = viewModel()
            SignUpScreen(
                navController = navController,
                viewModel = signUpViewModel
            )
        }
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                modifier = modifier,
                viewModel = homeViewModel
            )
        }
    }
}