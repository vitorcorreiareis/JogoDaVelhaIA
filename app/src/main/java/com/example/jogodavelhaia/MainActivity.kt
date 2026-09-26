package com.example.jogodavelhaia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.jogodavelhaia.ui.theme.JogoDaVelhaIATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JogoDaVelhaIATheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TicTacToeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
