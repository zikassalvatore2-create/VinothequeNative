package com.vinotheque.nativeapp.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.theme.DessertColor
import com.vinotheque.nativeapp.ui.theme.RedWineColor
import com.vinotheque.nativeapp.ui.theme.RoseWineColor
import com.vinotheque.nativeapp.ui.theme.SparklingColor
import com.vinotheque.nativeapp.ui.theme.WhiteWineColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R

fun getTypeColor(type: String): Color = when (type.lowercase().replace("é", "e")) {
    "red" -> RedWineColor
    "white" -> WhiteWineColor
    "rose" -> RoseWineColor
    "sparkling" -> SparklingColor
    "dessert" -> DessertColor
    else -> Color(0xFFD4AF6A) // Fallback gold
}

@Composable
fun CellarScreen(viewModel: WineViewModel, isAdmin: Boolean, onWineClick: (Wine) -> Unit, onDeleteWine: (Wine) -> Unit) {
    val wines by viewModel.wines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    var wineToDelete by remember { mutableStateOf<Wine?>(null) }
    var isListView by remember { mutableStateOf(false) }

    if (wineToDelete != null) {
        AlertDialog(onDismissRequest = { wineToDelete = null },
            title = { Text(stringResource(R.string.remove)) },
            text = { Text(stringResource(R.string.remove) + " " + (wineToDelete?.name ?: "") + "?") },
            confirmButton = { TextButton(onClick = { wineToDelete?.let { onDeleteWine(it) }; wineToDelete = null }) {
                Text(stringResource(R.string.remove), color = Color.Red) } },
            dismissButton = { TextButton(onClick = { wineToDelete = null }) { Text(stringResource(R.string.cancel)) } })
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text(stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, stringResource(R.string.search_hint), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            if (isAdmin) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { isListView = !isListView }) {
                    Icon(if (isListView) Icons.Default.GridView else Icons.Default.ViewList, "Toggle View", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Filter chips
        val filterOptions = listOf(
            "" to stringResource(R.string.type_all),
            "Red" to stringResource(R.string.type_red),
            "White" to stringResource(R.string.type_white),
            "Rose" to stringResource(R.string.type_rose),
            "Sparkling" to stringResource(R.string.type_sparkling),
            "Dessert" to stringResource(R.string.type_dessert)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterOptions.forEach { (value, label) ->
                val isSelected = typeFilter == value
                Button(
                    onClick = { viewModel.typeFilter.value = value },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(label, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results
        Text(wines.size.toString() + " " + stringResource(R.string.wines_stat), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))

        if (wines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalBar, "Empty", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.cellar_empty), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 18.sp, fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(stringResource(R.string.cellar_empty_hint), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 14.sp)
                }
            }
        } else {
            if (isListView) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    lazyColumnItems(wines, key = { it.reference }) { wine ->
                        WineListRow(wine, onClick = { onWineClick(wine) }, onLongClick = { if (isAdmin) wineToDelete = wine })
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(wines, key = { it.reference }) { wine ->
                        WineCard(wine, onClick = { onWineClick(wine) }, onLongClick = { if (isAdmin) wineToDelete = wine })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineCard(wine: Wine, onClick: () -> Unit, onLongClick: () -> Unit) {
    val typeColor = getTypeColor(wine.type)

    // Fade-in animation
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(400), label = "cardAlpha")
    LaunchedEffect(Unit) { visible = true }

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.62f).alpha(alpha)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Image  elegant gradient background
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .background(Brush.verticalGradient(listOf(
                            MaterialTheme.colorScheme.surface,
                            typeColor.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        ))),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncWineImage(
                        imageData = wine.image,
                        contentDescription = wine.name,
                        modifier = Modifier.fillMaxSize().padding(6.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = {
                            Icon(Icons.Default.LocalBar, wine.name,
                                tint = typeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp))
                        }
                    )
                }                // Info section
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(wine.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp, maxLines = 2, lineHeight = 15.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(wine.region, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp, maxLines = 1)
                    if (wine.binLocation.isNotEmpty()) {
                        Text("📍 " + wine.binLocation, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontSize = 9.sp, maxLines = 1)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("\u20AC" + wine.price.toInt().toString(), color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(wine.vintage, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                }
            }
            // Rating badge — elegant gold circle
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .size(34.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))),
                contentAlignment = Alignment.Center
            ) {
                Text(wine.rating.toString(), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            // Type badge — frosted glass effect
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(typeColor.copy(alpha = 0.8f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(getLocalizedType(wine.type), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            // Sold badge
            if (wine.sold > 0) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(wine.sold.toString() + " " + stringResource(R.string.sold), color = MaterialTheme.colorScheme.primary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineListRow(wine: Wine, onClick: () -> Unit, onLongClick: () -> Unit) {
    val typeColor = getTypeColor(wine.type)

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(typeColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncWineImage(
                    imageData = wine.image,
                    contentDescription = wine.name,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = {
                        Icon(Icons.Default.LocalBar, wine.name, tint = typeColor.copy(alpha = 0.3f), modifier = Modifier.size(28.dp))
                    }
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(wine.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (wine.region.isEmpty()) stringResource(R.string.no_region) else wine.region, color = if (wine.region.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp, maxLines = 1)
                    Text(" | ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 11.sp)
                    Text(if (wine.vintage.isEmpty()) stringResource(R.string.no_vintage) else wine.vintage, color = if (wine.vintage.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(if (wine.grape.isEmpty()) stringResource(R.string.no_grape) else wine.grape, color = if (wine.grape.isEmpty()) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 10.sp)
                if (wine.rating == 0) {
                    Text(stringResource(R.string.no_rating), color = Color.Red, fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("\u20AC" + wine.price.toInt().toString(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (wine.binLocation.isEmpty()) {
                    Text(stringResource(R.string.no_bin), color = Color.Red, fontSize = 10.sp)
                } else {
                    Text(stringResource(R.string.bin_location) + " " + wine.binLocation, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        }
    }
}


