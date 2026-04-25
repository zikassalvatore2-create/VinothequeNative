package com.vinotheque.nativeapp.ui

import android.graphics.BitmapFactory
import android.util.Base64
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.theme.RedWineColor
import com.vinotheque.nativeapp.ui.theme.RoseWineColor
import com.vinotheque.nativeapp.ui.theme.SparklingColor
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WhiteWineColor
import com.vinotheque.nativeapp.ui.theme.WineCard
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineGoldDim
import com.vinotheque.nativeapp.ui.theme.WineSurface

fun getTypeColor(type: String): Color = when (type.lowercase()) {
    "red" -> RedWineColor
    "white" -> WhiteWineColor
    "rose" -> RoseWineColor
    "sparkling" -> SparklingColor
    else -> WineGold
}

@Composable
fun CellarScreen(viewModel: WineViewModel, onWineClick: (Wine) -> Unit, onDeleteWine: (Wine) -> Unit) {
    val wines by viewModel.wines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    var wineToDelete by remember { mutableStateOf<Wine?>(null) }

    if (wineToDelete != null) {
        AlertDialog(onDismissRequest = { wineToDelete = null },
            title = { Text("Remove Wine") },
            text = { Text("Remove " + (wineToDelete?.name ?: "") + " from your cellar?") },
            confirmButton = { TextButton(onClick = { wineToDelete?.let { onDeleteWine(it) }; wineToDelete = null }) {
                Text("Remove", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { wineToDelete = null }) { Text("Cancel") } })
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search wines...", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Default.Search, "Search", tint = TextSecondary) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WineGold,
                unfocusedBorderColor = WineSurface,
                focusedContainerColor = WineSurface,
                unfocusedContainerColor = WineSurface,
                cursorColor = WineGold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        // Filter chips - scrollable row
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("" to "All", "Red" to "Red", "White" to "White", "Rose" to "Rose",
                "Sparkling" to "Spark", "Dessert" to "Dessert").forEach { (value, label) ->
                val isSelected = typeFilter == value
                Button(
                    onClick = { viewModel.typeFilter.value = value },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) WineGold else WineSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Text(label, color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Results count
        Text(wines.size.toString() + " wines", color = TextTertiary, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp))
        Spacer(modifier = Modifier.height(8.dp))

        if (wines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalBar, "Empty", tint = TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cellar is empty", color = TextSecondary, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Add wines or load sample data from Settings", color = TextTertiary, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wines) { wine ->
                    WineCard(wine, onClick = { onWineClick(wine) }, onLongClick = { wineToDelete = wine })
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineCard(wine: Wine, onClick: () -> Unit, onLongClick: () -> Unit) {
    val decodedBitmap: ImageBitmap? = remember(wine.image) {
        if (wine.image != null) {
            try { val d = wine.image.substringAfter(","); val b = Base64.decode(d, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else null
    }
    val typeColor = getTypeColor(wine.type)

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.62f)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WineSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Image / placeholder — surface-colored bg, no dark contrast
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                        .background(Brush.verticalGradient(listOf(WineSurface, WineSurface))),
                    contentAlignment = Alignment.Center
                ) {
                    if (decodedBitmap != null) {
                        Image(bitmap = decodedBitmap, contentDescription = "Wine",
                            modifier = Modifier.fillMaxSize().padding(6.dp),
                            contentScale = ContentScale.Fit)
                    } else {
                        Icon(Icons.Default.LocalBar, "Wine", tint = WineGoldDim.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp))
                    }
                }
                // Info
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Text(wine.name, color = Color.White, fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, maxLines = 2, lineHeight = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(wine.region, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
                    if (wine.binLocation.isNotEmpty()) {
                        Text("\uD83D\uDCCD " + wine.binLocation, color = WineGold.copy(alpha = 0.8f), fontSize = 9.sp, maxLines = 1)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("\u20AC" + wine.price.toInt().toString(), color = WineGold,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(wine.vintage, color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
            // Rating badge (top right)
            Box(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    .size(36.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(WineGold, WineGoldDim))),
                contentAlignment = Alignment.Center
            ) {
                Text(wine.rating.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            // Type badge (top left)
            Box(
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(wine.type, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            // Stock badge (bottom right)
            if (wine.quantity > 0) {
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WineSurface.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("x" + wine.quantity.toString(), color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
