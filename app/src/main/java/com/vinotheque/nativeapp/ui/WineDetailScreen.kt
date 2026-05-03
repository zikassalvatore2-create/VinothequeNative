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
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineDetailScreen(wine: Wine, viewModel: WineViewModel, isAdmin: Boolean, onBack: () -> Unit, onDelete: () -> Unit) {
    val favoriteRefs by viewModel.favoriteRefs.collectAsState()
    val isFav = favoriteRefs.contains(wine.reference)
    val typeColor = getTypeColor(wine.type)
    val context = LocalContext.current
    var showQuantityPicker by remember { mutableStateOf(false) }
    var showPresentation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var lastSaleId by remember { mutableStateOf<Long?>(null) }
    var lastSaleQty by remember { mutableStateOf(1) }

    // Keep screen on while viewing wine detail
    val activity = context as? android.app.Activity
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Quantity picker dialog
    if (showQuantityPicker) {
        Dialog(onDismissRequest = { showQuantityPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Quantity", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    val quantities = listOf(1, 2, 3, 4, 6, 12)
                    for (row in quantities.chunked(3)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (qty in row) {
                                Button(
                                    onClick = {
                                        showQuantityPicker = false
                                        viewModel.sellWine(wine, qty) { id -> lastSaleId = id }
                                        lastSaleQty = qty
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Sold ${qty}× ${wine.name}")
                                        }
                                    },
                                    modifier = Modifier.size(72.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = WineGold)
                                ) {
                                    Text(qty.toString(), color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Presentation mode
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
                Button(
                    onClick = {
                        viewModel.sellWine(wine, 1) { id -> lastSaleId = id }
                        lastSaleQty = 1
                        scope.launch { snackbarHostState.showSnackbar("Sold 1× ${wine.name}") }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                        .combinedClickable(
                            onClick = {
                                viewModel.sellWine(wine, 1) { id -> lastSaleId = id }
                                lastSaleQty = 1
                                scope.launch { snackbarHostState.showSnackbar("Sold 1× ${wine.name}") }
                            },
                            onLongClick = { showQuantityPicker = true }
                        ),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WineGold)
                ) {
                    Icon(Icons.Default.ShoppingCart, "Sell", tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Record Sale", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

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

        // Undo Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        ) { data ->
            Snackbar(
                containerColor = WineSurface,
                contentColor = Color.White,
                action = {
                    TextButton(onClick = {
                        lastSaleId?.let { id ->
                            viewModel.undoSale(id, wine, lastSaleQty)
                            Toast.makeText(context, "Sale undone", Toast.LENGTH_SHORT).show()
                        }
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }) { Text("Undo", color = WineGold, fontWeight = FontWeight.Bold) }
                }
            ) { Text(data.visuals.message) }
        }
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

        Column(modifier = Modifier.padding(20.dp)) {
            Text(wine.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
            Text("${wine.region} | ${wine.vintage}", color = TextSecondary, fontSize = 16.sp)
            if (wine.keywords.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(wine.keywords, color = WineGold, fontSize = 20.sp, fontStyle = FontStyle.Italic)
            }

            Spacer(modifier = Modifier.height(20.dp))

            DetailSection("Reference", wine.reference)
            DetailSection("Grape", wine.grape)
            DetailSection("Type", "${wine.type} | ${wine.dryness}")
            DetailSection("Price", "\u20AC${wine.price.toInt()}")
            DetailSection("Rating", "${wine.rating}/100")
            if (wine.ratingSource.isNotEmpty()) DetailSection("Rating Source", wine.ratingSource)
            DetailSection("Bin Location", wine.binLocation)
            DetailSection("Glass", wine.glassType)
            DetailSection("Decanting", wine.decanting)
            DetailSection("Serving Temp", wine.servingTemp)
            DetailSection("Peak Maturity", wine.peakMaturity)
            DetailSection("Sold", "${wine.sold} bottles")

            if (wine.aroma.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aroma", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(wine.aroma, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            }
            if (wine.tastingNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tasting Notes", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(wine.tastingNotes, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            }
            if (wine.foodPairing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Food Pairing", color = WineGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(wine.foodPairing, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
