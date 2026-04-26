package com.vinotheque.nativeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.AddWineScreen
import com.vinotheque.nativeapp.ui.AdminScreen
import com.vinotheque.nativeapp.ui.CellarScreen
import com.vinotheque.nativeapp.ui.DashboardScreen
import com.vinotheque.nativeapp.ui.FavoritesScreen
import com.vinotheque.nativeapp.ui.PairingScreen
import com.vinotheque.nativeapp.ui.SettingsScreen
import com.vinotheque.nativeapp.ui.WineDetailScreen
import com.vinotheque.nativeapp.ui.WineViewModel
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private val VinothequeColorScheme = darkColorScheme(
    primary = WineGold,
    secondary = WineRed,
    background = WineDark,
    surface = WineSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = VinothequeColorScheme) {
                Surface(color = WineDark) {
                    var showSplash by remember { mutableStateOf(true) }
                    LaunchedEffect(Unit) { delay(2200); showSplash = false }

                    AnimatedVisibility(visible = showSplash, exit = fadeOut()) { SplashScreen() }
                    AnimatedVisibility(visible = !showSplash, enter = fadeIn()) { VinothequeApp() }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WineDark, WineRed.copy(alpha = 0.3f), WineDark)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.LocalBar,
                contentDescription = "Logo",
                tint = WineGold,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "VINOTHEQUE",
                color = WineGold,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 8.sp
            )
            Text(
                text = "PRO",
                color = WineGold.copy(alpha = 0.6f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 12.sp
            )
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Developed by",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
            Text(
                text = "Zakariae BOUZIDI-IDRISSI",
                color = WineGold.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun VinothequeApp() {
    val viewModel: WineViewModel = viewModel()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddWine by remember { mutableStateOf(false) }
    var selectedWine by remember { mutableStateOf<Wine?>(null) }
    var showAdmin by remember { mutableStateOf(false) }

    // Back button: navigate back through overlay screens, then to Home tab, then do nothing
    BackHandler {
        when {
            showAdmin -> showAdmin = false
            selectedWine != null -> selectedWine = null
            showAddWine -> showAddWine = false
            selectedTab != 0 -> selectedTab = 0
            // On home tab, do nothing (don't exit)
        }
    }

    if (showAdmin) {
        AdminScreen(viewModel = viewModel, onBack = { showAdmin = false })
        return
    }
    if (selectedWine != null) {
        val wine = selectedWine
        if (wine != null) {
            WineDetailScreen(wine = wine, viewModel = viewModel,
                onBack = { selectedWine = null },
                onDelete = {
                    viewModel.deleteWine(wine.reference)
                    Toast.makeText(context, "Wine removed from cellar", Toast.LENGTH_SHORT).show()
                    selectedWine = null
                })
        }
        return
    }
    if (showAddWine) {
        AddWineScreen(viewModel = viewModel) {
            showAddWine = false
            Toast.makeText(context, "Wine added to cellar", Toast.LENGTH_SHORT).show()
        }
        return
    }

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Cellar", Icons.Default.LocalBar),
        NavItem("Pairing", Icons.Default.Restaurant),
        NavItem("Favorites", Icons.Default.Favorite),
        NavItem("More", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = WineDark,
        bottomBar = {
            NavigationBar(
                containerColor = WineSurface,
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(22.dp)) },
                        label = { Text(item.label, fontSize = 10.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WineGold,
                            unselectedIconColor = TextSecondary,
                            selectedTextColor = WineGold,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = WineGold.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddWine = true },
                    containerColor = WineGold,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Wine", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Crossfade(targetState = selectedTab, animationSpec = tween(300), label = "tabFade") { tab ->
                when (tab) {
                    0 -> DashboardScreen(viewModel)
                    1 -> CellarScreen(viewModel = viewModel,
                        onWineClick = { selectedWine = it },
                        onDeleteWine = { viewModel.deleteWine(it.reference)
                            Toast.makeText(context, "Wine removed", Toast.LENGTH_SHORT).show() })
                    2 -> PairingScreen(viewModel = viewModel, onWineClick = { selectedWine = it })
                    3 -> FavoritesScreen(viewModel = viewModel, onWineClick = { selectedWine = it })
                    4 -> SettingsScreen(viewModel = viewModel, onOpenAdmin = { showAdmin = true })
                }
            }
        }
    }
}
