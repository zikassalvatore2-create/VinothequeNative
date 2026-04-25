package com.vinotheque.nativeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.AddWineScreen
import com.vinotheque.nativeapp.ui.CellarScreen
import com.vinotheque.nativeapp.ui.DashboardScreen
import com.vinotheque.nativeapp.ui.FavoritesScreen
import com.vinotheque.nativeapp.ui.PairingScreen
import com.vinotheque.nativeapp.ui.SettingsScreen
import com.vinotheque.nativeapp.ui.WineDetailScreen
import com.vinotheque.nativeapp.ui.WineViewModel

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val BarBg = Color(0xFF1A0A0A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = DarkBg) { VinothequeApp() }
            }
        }
    }
}

@Composable
fun VinothequeApp() {
    val viewModel: WineViewModel = viewModel()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddWine by remember { mutableStateOf(false) }
    var selectedWine by remember { mutableStateOf<Wine?>(null) }

    // Detail screen
    if (selectedWine != null) {
        WineDetailScreen(wine = selectedWine!!, viewModel = viewModel,
            onBack = { selectedWine = null },
            onDelete = {
                viewModel.deleteWine(selectedWine!!.reference)
                Toast.makeText(context, "Wine deleted", Toast.LENGTH_SHORT).show()
                selectedWine = null
            })
        return
    }

    // Add screen
    if (showAddWine) {
        AddWineScreen(viewModel = viewModel) {
            showAddWine = false
            Toast.makeText(context, "Wine saved!", Toast.LENGTH_SHORT).show()
        }
        return
    }

    val tabs = listOf("Home", "Cellar", "Pair", "Favs", "More")

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().background(BarBg).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Vinotheque Pro", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = BarBg, contentColor = Gold) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = { Text(label.take(1), fontSize = 14.sp) },
                        label = { Text(label, fontSize = 10.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold, unselectedIconColor = Color.Gray,
                            selectedTextColor = Gold, unselectedTextColor = Color.Gray,
                            indicatorColor = Gold.copy(alpha = 0.2f))
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(onClick = { showAddWine = true }, containerColor = Gold) {
                    Text("+", color = Color.Black, fontSize = 24.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                0 -> DashboardScreen(viewModel)
                1 -> CellarScreen(viewModel = viewModel,
                    onWineClick = { wine -> selectedWine = wine },
                    onDeleteWine = { wine ->
                        viewModel.deleteWine(wine.reference)
                        Toast.makeText(context, "Wine deleted", Toast.LENGTH_SHORT).show()
                    })
                2 -> PairingScreen(viewModel = viewModel, onWineClick = { wine -> selectedWine = wine })
                3 -> FavoritesScreen(viewModel = viewModel, onWineClick = { wine -> selectedWine = wine })
                4 -> SettingsScreen(viewModel)
            }
        }
    }
}
