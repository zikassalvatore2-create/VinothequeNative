package com.vinotheque.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0d0505) // Dark background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🍷",
            fontSize = 80.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Vinothèque Pro",
            color = Color(0xFFd4a54e), // Gold color
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Native Android Edition",
            color = Color(0xFFc4b4a4),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { /* TODO: Navigate to cellar */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd4a54e))
        ) {
            Text(text = "Enter Cellar", color = Color.Black)
        }
    }
}
