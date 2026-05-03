package com.vinotheque.nativeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.AddWineScreen
import com.vinotheque.nativeapp.ui.AdminScreen
import com.vinotheque.nativeapp.ui.CellarScreen
import com.vinotheque.nativeapp.ui.DashboardScreen
import com.vinotheque.nativeapp.ui.FavoritesScreen
import com.vinotheque.nativeapp.ui.PairingScreen
import com.vinotheque.nativeapp.ui.SettingsScreen
import com.vinotheque.nativeapp.ui.SalesAnalyticsScreen
import com.vinotheque.nativeapp.ui.WineDetailScreen
import com.vinotheque.nativeapp.ui.WineViewModel
import com.vinotheque.nativeapp.ui.theme.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VinothequeAppContainer()
        }
    }
}

@Composable
fun VinothequeAppContainer() {
    val viewModel: WineViewModel = viewModel()
    val theme by viewModel.selectedTheme.collectAsState()

    ProvideVinothequeTheme(themeName = theme) {
        val colorScheme = MaterialTheme.colorScheme
        Surface(color = colorScheme.background) {
            var showSplash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) { delay(2200); showSplash = false }
            AnimatedVisibility(visible = showSplash, exit = fadeOut()) { SplashScreen() }
            AnimatedVisibility(visible = !showSplash, enter = fadeIn()) { VinothequeApp(viewModel) }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            com.vinotheque.nativeapp.ui.AnimatedVinothequeLogo(modifier = Modifier.size(140.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("VINOTHEQUE", color = MaterialTheme.colorScheme.primary, fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
            Text("PRO", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), fontSize = 18.sp, fontWeight = FontWeight.Light, letterSpacing = 12.sp)
            Spacer(modifier = Modifier.height(60.dp))
            Text("Developed by", color = TextSecondary.copy(alpha = 0.5f), fontSize = 12.sp)
            Text("Zakariae BOUZIDI-IDRISSI", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** First-launch name prompt */
@Composable
fun NamePromptScreen(onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { delay(300); focusRequester.requestFocus() }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(WineDark, WineRed.copy(alpha = 0.15f), WineDark))),
        contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
            Icon(Icons.Default.LocalBar, "Logo", tint = WineGold, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(32.dp))
            Text("Welcome", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Who's serving tonight?", color = TextSecondary, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("Your name", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                singleLine = true, shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WineGold, unfocusedBorderColor = WineGold.copy(alpha = 0.3f),
                    focusedContainerColor = WineSurface, unfocusedContainerColor = WineSurface,
                    cursorColor = WineGold, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onConfirm(name.trim()) },
                enabled = name.trim().length >= 2,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WineGold, disabledContainerColor = WineGold.copy(alpha = 0.3f))
            ) {
                Text("Begin", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

/** 4-digit PIN dialog with numeric keypad */
@Composable
fun AdminPinDialog(onCorrect: () -> Unit, onDismiss: () -> Unit, viewModel: WineViewModel) {
    var pin by remember { mutableStateOf("") }
    var shake by remember { mutableStateOf(false) }
    val shakeOffset by animateFloatAsState(if (shake) 10f else 0f, tween(50), label = "shake",
        finishedListener = { if (shake) shake = false })

    LaunchedEffect(shake) { if (shake) { delay(100); shake = false } }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = WineSurface),
            modifier = Modifier.width(280.dp)) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Admin Access", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                // PIN dots
                Row(modifier = Modifier.offset(x = shakeOffset.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until 4) {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape)
                            .background(if (i < pin.length) WineGold else WineGold.copy(alpha = 0.2f)))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Numeric keypad
                val keys = listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("⌫","0","✓"))
                for (row in keys) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (key in row) {
                            Button(
                                onClick = {
                                    when (key) {
                                        "⌫" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                        "✓" -> {
                                            if (viewModel.checkPin(pin)) { onCorrect(); onDismiss() }
                                            else { shake = true; pin = "" }
                                        }
                                        else -> if (pin.length < 4) pin += key
                                    }
                                },
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = WineDark)
                            ) {
                                Text(key, color = Color.White, fontSize = if (key == "⌫" || key == "✓") 18.sp else 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun VinothequeApp(viewModel: WineViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddWine by remember { mutableStateOf(false) }
    var selectedWine by remember { mutableStateOf<Wine?>(null) }
    var showAdmin by remember { mutableStateOf(false) }
    var showSales by remember { mutableStateOf(false) }
    var showShiftSummary by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    val isAdmin by viewModel.isAdmin.collectAsState()
    var showNamePrompt by remember { mutableStateOf(viewModel.isFirstLaunch()) }

    // Check admin timeout periodically
    LaunchedEffect(isAdmin) {
        while (isAdmin) { delay(60_000); viewModel.checkAdminTimeout() }
    }

    // First launch name prompt
    if (showNamePrompt) {
        NamePromptScreen { name ->
            viewModel.setUser(name)
            viewModel.setNameSet()
            showNamePrompt = false
        }
        return
    }

    // PIN dialog
    if (showPinDialog) {
        AdminPinDialog(
            onCorrect = {
                viewModel.setAdmin(true)
                Toast.makeText(context, "Admin mode enabled", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showPinDialog = false },
            viewModel = viewModel
        )
    }

    BackHandler {
        when {
            showSales -> showSales = false
            showShiftSummary -> showShiftSummary = false
            showAdmin -> showAdmin = false
            selectedWine != null -> selectedWine = null
            showAddWine -> showAddWine = false
            selectedTab != 0 -> selectedTab = 0
        }
    }

    if (showSales) { SalesAnalyticsScreen(viewModel = viewModel, onBack = { showSales = false }); return }
    if (showShiftSummary) { com.vinotheque.nativeapp.ui.ShiftSummaryScreen(viewModel = viewModel, onBack = { showShiftSummary = false }); return }
    if (showAdmin) { AdminScreen(viewModel = viewModel, onBack = { showAdmin = false }, onOpenSales = { showSales = true }); return }
    if (selectedWine != null) {
        val wine = selectedWine!!
        WineDetailScreen(wine = wine, viewModel = viewModel, isAdmin = isAdmin,
            onBack = { selectedWine = null },
            onDelete = {
                if (isAdmin) { viewModel.deleteWine(wine.reference); Toast.makeText(context, "Wine removed", Toast.LENGTH_SHORT).show(); selectedWine = null }
            })
        return
    }
    if (showAddWine) { AddWineScreen(viewModel = viewModel) { showAddWine = false; Toast.makeText(context, "Wine added", Toast.LENGTH_SHORT).show() }; return }

    val navItems = listOf(
        NavItem("Home", Icons.Default.Home), NavItem("Cellar", Icons.Default.LocalBar),
        NavItem("Pairing", Icons.Default.Restaurant), NavItem("Favorites", Icons.Default.Favorite),
        NavItem("More", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = WineDark,
        bottomBar = {
            NavigationBar(containerColor = WineSurface, tonalElevation = 0.dp) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, item.label, modifier = Modifier.size(22.dp)) },
                        label = { Text(item.label, fontSize = 10.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                        selected = selectedTab == index, onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WineGold, unselectedIconColor = TextSecondary,
                            selectedTextColor = WineGold, unselectedTextColor = TextSecondary,
                            indicatorColor = WineGold.copy(alpha = 0.15f))
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 1 && isAdmin) {
                FloatingActionButton(onClick = { showAddWine = true }, containerColor = WineGold, contentColor = Color.Black) {
                    Icon(Icons.Default.Add, "Add Wine", modifier = Modifier.size(28.dp))
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Crossfade(targetState = selectedTab, animationSpec = tween(150), label = "tabFade") { tab ->
                when (tab) {
                    0 -> DashboardScreen(viewModel)
                    1 -> CellarScreen(viewModel = viewModel, isAdmin = isAdmin,
                        onWineClick = { selectedWine = it },
                        onDeleteWine = { if (isAdmin) { viewModel.deleteWine(it.reference); Toast.makeText(context, "Wine removed", Toast.LENGTH_SHORT).show() } })
                    2 -> PairingScreen(viewModel = viewModel, onWineClick = { selectedWine = it })
                    3 -> FavoritesScreen(viewModel = viewModel, onWineClick = { selectedWine = it })
                    4 -> SettingsScreen(viewModel = viewModel,
                        onOpenAdmin = { showAdmin = true },
                        onShowPinDialog = { showPinDialog = true },
                        onShowNamePrompt = { showNamePrompt = true },
                        onShowShiftSummary = { showShiftSummary = true })
                }
            }

            // Admin lock icon overlay (top-right)
            if (selectedTab == 0 || selectedTab == 1) {
                IconButton(
                    onClick = { if (isAdmin) viewModel.setAdmin(false) else showPinDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(
                        if (isAdmin) Icons.Default.LockOpen else Icons.Default.Lock,
                        "Admin", tint = WineGold.copy(alpha = if (isAdmin) 1f else 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Gold indicator line when admin active
            if (isAdmin) {
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(WineGold.copy(alpha = 0.6f)).align(Alignment.TopCenter))
            }
        }
    }
}
