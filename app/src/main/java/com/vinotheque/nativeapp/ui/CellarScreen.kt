package com.vinotheque.nativeapp.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val BarBg = Color(0xFF1A0A0A)

@Composable
fun CellarScreen(
    viewModel: WineViewModel,
    onWineClick: (Wine) -> Unit,
    onDeleteWine: (Wine) -> Unit
) {
    val wines by viewModel.wines.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    var wineToDelete by remember { mutableStateOf<Wine?>(null) }

    // Delete dialog
    if (wineToDelete != null) {
        AlertDialog(
            onDismissRequest = { wineToDelete = null },
            title = { Text("Delete Wine") },
            text = { Text("Remove " + (wineToDelete?.name ?: "") + "?") },
            confirmButton = {
                TextButton(onClick = {
                    wineToDelete?.let { onDeleteWine(it) }
                    wineToDelete = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { wineToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            label = { Text("Search wines...") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        // Type filter chips
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("", "Red", "White", "Rose", "Sparkling", "Dessert").forEach { t ->
                val label = if (t.isEmpty()) "All" else t
                Button(
                    onClick = { viewModel.typeFilter.value = t },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (typeFilter == t) Gold else BarBg
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                ) {
                    Text(label, color = if (typeFilter == t) Color.Black else Color.White, fontSize = 9.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (wines.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No wines found", color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wines) { wine ->
                    WineCard(
                        wine = wine,
                        onClick = { onWineClick(wine) },
                        onLongClick = { wineToDelete = wine }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WineCard(wine: Wine, onClick: () -> Unit, onLongClick: () -> Unit) {
    val goldBorder = Brush.linearGradient(listOf(Gold, Color(0xFF9A7B3A)))

    val decodedBitmap: ImageBitmap? = remember(wine.image) {
        if (wine.image != null) {
            try {
                val data = wine.image.substringAfter(",")
                val bytes = Base64.decode(data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .border(1.dp, goldBorder, RoundedCornerShape(16.dp))
            .combinedClickable(onClick = { onClick() }, onLongClick = { onLongClick() }),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BarBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (decodedBitmap != null) {
                    Image(bitmap = decodedBitmap, contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text("W", fontSize = 40.sp, color = Gold)
                }
            }
            Column(
                modifier = Modifier.padding(10.dp).weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(wine.name, color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2)
                Text(wine.region, color = Color.LightGray, fontSize = 10.sp, maxLines = 1)
                Spacer(modifier = Modifier.weight(1f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(wine.vintage, color = Color.Gray, fontSize = 10.sp)
                    Text(wine.type, color = Color.Gray, fontSize = 10.sp)
                }
                HorizontalDivider(color = Gold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("E" + wine.price.toInt().toString(), color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(wine.rating.toString() + "pt", color = Gold, fontSize = 13.sp)
                }
            }
        }
    }
}
