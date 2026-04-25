package com.vinotheque.nativeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vinotheque.nativeapp.ui.AddWineScreen
import com.vinotheque.nativeapp.ui.CellarScreen
import com.vinotheque.nativeapp.ui.DashboardScreen
import com.vinotheque.nativeapp.ui.SettingsScreen
import com.vinotheque.nativeapp.ui.WineViewModel

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val BarBg = Color(0xFF1A0A0A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = DarkBg) {
                    VinothequeApp()
                }
            }
        }
    }
}

@Composable
fun VinothequeApp() {
    val viewModel: WineViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddWine by remember { mutableStateOf(false) }

    if (showAddWine) {
        AddWineScreen(viewModel = viewModel) {
            showAddWine = false
        }
        return
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BarBg)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vinotheque Pro",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = BarBg,
                contentColor = Gold
            ) {
                NavigationBarItem(
                    icon = { Text("D") },
                    label = { Text("Insights") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Gold,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Gold.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Text("C") },
                    label = { Text("Cellar") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Gold,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Gold.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    icon = { Text("S") },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold,
                        unselectedIconColor = Color.Gray,
                        selectedTextColor = Gold,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Gold.copy(alpha = 0.2f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddWine = true },
                    containerColor = Gold
                ) {
                    Text(text = "+", color = Color.Black, fontSize = 24.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> CellarScreen(
                    viewModel = viewModel,
                    onWineClick = { },
                    onDeleteWine = { wine -> viewModel.deleteWine(wine.reference) }
                )
                2 -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
