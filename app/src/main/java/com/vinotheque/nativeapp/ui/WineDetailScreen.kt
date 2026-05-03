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
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vinotheque.nativeapp.data.Wine
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R
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
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
            // ===== SERVICE ACTION BAR — ALWAYS VISIBLE, NO SCROLLING NEEDED =====
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
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
                    Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp))
                }
                Row {
                    IconButton(onClick = { viewModel.toggleFavorite(wine.reference) }) {
                        Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            stringResource(R.string.favorites), tint = if (isFav) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(28.dp))
                    }
                    if (isAdmin) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // Wine image
            Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                    AsyncWineImage(
                        imageData = wine.image, contentDescription = stringResource(R.string.wines_stat),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = { Icon(Icons.Default.LocalBar, stringResource(R.string.wines_stat), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.size(80.dp)) }
                    )
                }
                // Rating badge
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(56.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))), contentAlignment = Alignment.Center) {
                    Text(wine.rating.toString(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                // Type badge
                Box(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp).clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.9f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                    Text(getLocalizedType(wine.type) + " | " + getLocalizedDryness(wine.dryness), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ===== ABOVE THE FOLD CONTENT =====
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                // Wine name
                Text(wine.name, color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                // Region | Vintage
                Text(
                    (if (wine.region.isNotEmpty()) wine.region else stringResource(R.string.unknown_region)) + " | " +
                    (if (wine.vintage.isNotEmpty()) wine.vintage else stringResource(R.string.nv)),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 15.sp
                )
                // Three selling keywords
                if (wine.keywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(wine.keywords, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Light)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price & info chips
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(stringResource(R.string.price), "\u20AC" + wine.price.toInt().toString(), Modifier.weight(1f))
                    InfoChip(stringResource(R.string.vintage), wine.vintage.ifEmpty { stringResource(R.string.nv) }, Modifier.weight(1f))
                    InfoChip(stringResource(R.string.sold), wine.sold.toString() + " " + stringResource(R.string.btl), Modifier.weight(1f))
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Search, stringResource(R.string.search_google), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.search_google), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { showPresentation = true },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Visibility, stringResource(R.string.present), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.present), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }

                // ===== BELOW THE FOLD =====
                Spacer(modifier = Modifier.height(20.dp))
                DetailSection(stringResource(R.string.reference), wine.reference)
                DetailSection(stringResource(R.string.grape), wine.grape)

                if (wine.tastingNotes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.tasting_notes), color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(wine.tastingNotes, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                }
                if (wine.aroma.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.aroma_profile), color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(wine.aroma, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                }
                if (wine.foodPairing.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.food_pairing), color = MaterialTheme.colorScheme.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(wine.foodPairing, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
                    }
                }
                if (wine.peakMaturity.isNotEmpty()) { Spacer(modifier = Modifier.height(12.dp)); DetailSection(stringResource(R.string.peak_maturity), wine.peakMaturity) }
                if (wine.ratingSource.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection(stringResource(R.string.rating_source), wine.ratingSource) }
                if (wine.glassType.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection(stringResource(R.string.glass), wine.glassType) }
                if (wine.decanting.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection(stringResource(R.string.decanting), wine.decanting) }
                if (wine.servingTemp.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); DetailSection(stringResource(R.string.serving_temp), wine.servingTemp) }

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
        Text(text, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
        }
    }
}

@Composable
fun DetailSection(label: String, value: String) {
    if (value.isEmpty()) return
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        // Close button
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "Close", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
            }
        }

        // Service bar
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))) {
            Row(modifier = Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ServiceTag("\uD83D\uDCCD", wine.binLocation.ifEmpty { "—" })
                ServiceTag("\uD83C\uDF77", wine.glassType.ifEmpty { "—" })
                ServiceTag("⏳", wine.decanting.ifEmpty { "—" })
                ServiceTag("\uD83C\uDF21\uFE0F", wine.servingTemp.ifEmpty { "—" })
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartSoldButton(
    wine: Wine,
    viewModel: WineViewModel,
    context: android.content.Context
) {
    var showQuantityPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Box {
        // Main button using Surface + combinedClickable for better gesture handling
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = {
                        // Single tap = 1 bottle
                        viewModel.sellWine(wine, quantity = 1)
                        val msg = if (1 == 1) stringResource(R.string.one_bottle_sold) else stringResource(R.string.bottles_sold, 1)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "$msg: ${wine.name}",
                                actionLabel = stringResource(R.string.undo),
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.undoLastSale()
                            }
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        showQuantityPicker = true
                    }
                ),
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = stringResource(R.string.record_sale),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.record_sale),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        stringResource(R.string.hold_for_quantity),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        fontSize = 9.sp
                    )
                }
            }
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
                        val msg = stringResource(R.string.bottles_sold, quantity)
                        val result = snackbarHostState.showSnackbar(
                            message = "$msg: ${wine.name}",
                            actionLabel = stringResource(R.string.undo),
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
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    stringResource(R.string.select_quantity),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
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
                                        containerColor = if (qty == 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                         else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        qty.toString(),
                                        color = MaterialTheme.colorScheme.primary,
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
                        placeholder = { Text(stringResource(R.string.custom), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cancel button
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                }
            }
        }
    }
}
