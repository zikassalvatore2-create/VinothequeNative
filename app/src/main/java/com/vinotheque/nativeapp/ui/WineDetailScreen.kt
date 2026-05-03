package com.vinotheque.nativeapp.ui

import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineGoldDim
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineDetailScreen(wine: Wine, viewModel: WineViewModel, isAdmin: Boolean, onBack: () -> Unit, onDelete: () -> Unit) {
    val favoriteRefs by viewModel.favoriteRefs.collectAsState()
    val isFav = favoriteRefs.contains(wine.reference)
    val typeColor = getTypeColor(wine.type)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Keep screen on while viewing wine detail
    val activity = context as? android.app.Activity
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Presentation mode
    var showPresentation by remember { mutableStateOf(false) }
    if (showPresentation) {
        PresentationMode(wine = wine, onClose = { showPresentation = false })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(WineDark).verticalScroll(rememberScrollState())) {
            // ===== SERVICE ACTION BAR — ALWAYS VISIBLE, NO SCROLLING NEEDED =====
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WineGold.copy(alpha = 0.10f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ServiceTag("\uD83D\uDCCD", if (wine.binLocation.isNotEmpty()) wine.binLocation else "—")
                    ServiceTag("\uD83C\uDF77", if (wine.glassType.isNotEmpty()) wine.glassType else "—")
                    ServiceTag("⏳", if (wine.decanting.isNotEmpty()) wine.decanting else "—")
                    ServiceTag("\uD83C\uDF21\uFE0F", if (wine.servingTemp.isNotEmpty()) wine.servingTemp else "—")
                }
            }

            // Top bar with back/fav/delete
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Row {
                    IconButton(onClick = { viewModel.toggleFavorite(wine.reference) }) {
                        Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite", tint = if (isFav) Color.Red else Color.White, modifier = Modifier.size(28.dp))
                    }
                    if (isAdmin) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Wine image
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(WineSurface), contentAlignment = Alignment.Center) {
                    AsyncWineImage(
                        imageData = wine.image, contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = { Icon(Icons.Default.LocalBar, "Wine", tint = WineGoldDim.copy(alpha = 0.2f), modifier = Modifier.size(80.dp)) }
                    )
                }
                // Rating badge
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(56.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(WineGold, WineGoldDim))), contentAlignment = Alignment.Center) {
                    Text(wine.rating.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                // Type badge
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp).clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.9f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                    Text(wine.type + " | " + wine.dryness, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ===== ABOVE THE FOLD CONTENT =====
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Wine name
                Text(wine.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                // Region | Vintage
                Text(
                    (if (wine.region.isNotEmpty()) wine.region else "Unknown Region") + " | " +
                    (if (wine.vintage.isNotEmpty()) wine.vintage else "NV"),
                    color = TextSecondary, fontSize = 15.sp
                )
                // Three selling keywords
                if (wine.keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(wine.keywords, color = WineGold, fontSize = 18.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price & info chips
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip("Price", "\u20AC" + wine.price.toInt().toString(), Modifier.weight(1f))
                    InfoChip("Vintage", wine.vintage.ifEmpty { "NV" }, Modifier.weight(1f))
                    InfoChip("Sold", wine.sold.toString() + " btl", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ===== ACTION BUTTONS =====
                // Smart Sold button — tap=1, long press=quantity
                SmartSoldButton(
                    wine = wine,
                    viewModel = viewModel,
                    context = context
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Google Search + Present buttons
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val q = "${wine.name} ${wine.vintage} wine".trim()
                            val url = "https://www.google.com/search?q=" + Uri.encode(q)
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WineGold)
                    ) {
                        Icon(Icons.Default.Search, "Google", tint = WineGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Search Google", color = WineGold, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showPresentation = true },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WineGold)
                    ) {
                        Icon(Icons.Default.Visibility, "Present", tint = WineGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Present", color = WineGold, fontSize = 12.sp)
                    }
                }

                // ===== BELOW THE FOLD =====
                Spacer(modifier = Modifier.height(20.dp))
                DetailSection("Reference", wine.reference)
                DetailSection("Grape", wine.grape)

                if (wine.tastingNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tasting Notes", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
                        Text(wine.tastingNotes, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                }
                if (wine.aroma.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aroma Profile", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
                        Text(wine.aroma, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                }
                if (wine.foodPairing.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Food Pairing", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
                        Text(wine.foodPairing, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                }
                if (wine.peakMaturity.isNotEmpty()) { Spacer(modifier = Modifier.height(12.dp)); DetailSection("Peak Maturity", wine.peakMaturity) }
                if (wine.ratingSource.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection("Rating Source", wine.ratingSource) }
                if (wine.glassType.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection("Glass", wine.glassType) }
                if (wine.decanting.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection("Decanting", wine.decanting) }
                if (wine.servingTemp.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection("Serving Temp", wine.servingTemp) }

                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // Undo Snackbar is now handled within SmartSoldButton
    }
}

@Composable
fun ServiceTag(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = WineSurface)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextTertiary, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
fun DetailSection(label: String, value: String) {
    if (value.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/** Presentation Mode — clean read-only view of ALL wine information */
@Composable
fun PresentationMode(wine: Wine, onClose: () -> Unit) {
    val typeColor = getTypeColor(wine.type)

    // Keep screen on in presentation mode too
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose { activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark).verticalScroll(rememberScrollState())) {
        // Close button
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        // Service bar
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = WineGold.copy(alpha = 0.10f))) {
            Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ServiceTag("\uD83D\uDCCD", wine.binLocation.ifEmpty { "—" })
                ServiceTag("\uD83C\uDF77", wine.glassType.ifEmpty { "—" })
                ServiceTag("⏳", wine.decanting.ifEmpty { "—" })
                ServiceTag("\uD83C\uDF21\uFE0F", wine.servingTemp.ifEmpty { "—" })
            }
        }

        }
    }
}

@Composable
fun SmartSoldButton(
    wine: Wine,
    viewModel: WineViewModel,
    context: android.content.Context
) {
    var showQuantityPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Box {
        // Main button
        Button(
            onClick = {
                // Single tap = 1 bottle
                viewModel.sellWine(wine, quantity = 1)
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = "1× ${wine.name} sold",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastSale()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            showQuantityPicker = true
                        }
                    )
                },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WineGold)
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Sell",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Record Sale",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Quantity picker popover
        if (showQuantityPicker) {
            QuantityPickerDialog(
                onDismiss = { showQuantityPicker = false },
                onConfirm = { quantity ->
                    viewModel.sellWine(wine, quantity = quantity)
                    showQuantityPicker = false
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = "$quantity× ${wine.name} sold",
                            actionLabel = "Undo",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoLastSale()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun QuantityPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var customQuantity by remember { mutableStateOf("") }
    val presets = listOf(1, 2, 3, 4, 6, 12)

    // Semi-transparent backdrop — tap to dismiss
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        // Popover card — tap inside does NOT dismiss
        Card(
            modifier = Modifier
                .width(300.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WineSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    "Select Quantity",
                    style = MaterialTheme.typography.titleMedium,
                    color = WineGold,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Preset grid: 3 columns × 2 rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0..1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                val qty = presets[index]
                                Button(
                                    onClick = { onConfirm(qty) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (qty == 1) WineGold.copy(alpha = 0.2f)
                                                         else WineSurface
                                    ),
                                    border = BorderStroke(1.dp, WineGold.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        qty.toString(),
                                        color = WineGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom quantity input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customQuantity,
                        onValueChange = { newValue ->
                            // Only allow digits
                            if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                                customQuantity = newValue
                            }
                        },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Custom", color = TextSecondary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WineGold,
                            unfocusedBorderColor = WineGold.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = WineGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val qty = customQuantity.toIntOrNull()
                            if (qty != null && qty > 0) {
                                onConfirm(qty)
                            }
                        },
                        modifier = Modifier.height(52.dp),
                        enabled = customQuantity.isNotEmpty() && customQuantity.toIntOrNull()?.let { it > 0 } == true,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WineGold)
                    ) {
                        Text("✓", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel button
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}
