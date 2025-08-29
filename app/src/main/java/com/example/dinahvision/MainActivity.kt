package com.example.dinahvision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.dinahvision.repository.SessionManager
import com.example.dinahvision.ui.theme.DinahVisionTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val sessionManager: SessionManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var initialScreen by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                initialScreen = if (sessionManager.isSessionActive()) "home" else "signIn"
            }

            DinahVisionTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (initialScreen != null) {
                        Navigation(
                            modifier = Modifier.padding(innerPadding),
                            initialScreen = initialScreen!!
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}
