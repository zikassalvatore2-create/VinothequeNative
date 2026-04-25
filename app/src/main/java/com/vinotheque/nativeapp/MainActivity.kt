package com.vinotheque.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.vinotheque.nativeapp.ui.AddWineScreen
import com.vinotheque.nativeapp.ui.CellarScreen
import com.vinotheque.nativeapp.ui.DashboardScreen
import com.vinotheque.nativeapp.ui.WineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = Color(0xFF0d0505)) {
                    VinothequeApp()
                }
            }
        }
    }
}

@Composable
fun VinothequeApp() {
    val navController = rememberNavController()
    val viewModel: WineViewModel = viewModel()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        containerColor = Color(0xFF0d0505),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1a0a0a),
                contentColor = Color(0xFFd4a54e)
            ) {
                NavigationBarItem(
                    icon = { Text("📊") },
                    label = { Text("Insights") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFd4a54e),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFFd4a54e),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0x33d4a54e)
                    )
                )
                NavigationBarItem(
                    icon = { Text("🍷") },
                    label = { Text("Cellar") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFd4a54e),
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Color(0xFFd4a54e),
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0x33d4a54e)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(navController = navController, startDestination = "tabs") {
                composable("tabs") {
                    if (selectedTab == 1) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CellarScreen(viewModel)
                            // Native FAB
                            FloatingActionButton(
                                onClick = { navController.navigate("add_wine") },
                                containerColor = Color(0xFFd4a54e),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                            ) {
                                Text("+", color = Color.Black, fontSize = 24.sp)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize()) {
                            DashboardScreen(viewModel)
                        }
                    }
                }
                composable("add_wine") {
                    AddWineScreen(viewModel) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}
