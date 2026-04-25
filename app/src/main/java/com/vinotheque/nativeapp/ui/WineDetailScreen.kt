package com.vinotheque.nativeapp.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WineCard
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineGoldDim
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface

@Composable
fun WineDetailScreen(wine: Wine, viewModel: WineViewModel, onBack: () -> Unit, onDelete: () -> Unit) {
    val favoriteRefs by viewModel.favoriteRefs.collectAsState()
    val isFav = favoriteRefs.contains(wine.reference)
    val typeColor = getTypeColor(wine.type)

    val decodedBitmap: ImageBitmap? = remember(wine.image) {
        if (wine.image != null) {
            try { val d = wine.image.substringAfter(","); val b = Base64.decode(d, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(b, 0, b.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else null
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark).verticalScroll(rememberScrollState())) {
        // Hero image area
        Box(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            Box(modifier = Modifier.fillMaxSize().background(WineCard), contentAlignment = Alignment.Center) {
                if (decodedBitmap != null) {
                    Image(bitmap = decodedBitmap, contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
                        contentScale = ContentScale.Fit)
                } else {
                    Icon(Icons.Default.LocalBar, "Wine", tint = WineGoldDim.copy(alpha = 0.2f), modifier = Modifier.size(100.dp))
                }
            }
            // Gradient overlay
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(WineDark.copy(alpha = 0.4f), Color.Transparent, WineDark), startY = 0f)))
            // Top bar
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Row {
                    IconButton(onClick = { viewModel.toggleFavorite(wine.reference) }) {
                        Icon(if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite", tint = if (isFav) Color.Red else Color.White, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                    }
                }
            }
            // Rating badge
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                    .size(64.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(WineGold, WineGoldDim))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(wine.rating.toString(), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("pts", color = Color.Black.copy(alpha = 0.6f), fontSize = 8.sp)
                }
            }
            // Type badge
            Box(
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(typeColor.copy(alpha = 0.9f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(wine.type + " | " + wine.dryness, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Content
        Column(modifier = Modifier.padding(20.dp)) {
            Text(wine.name, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(wine.region, color = TextSecondary, fontSize = 15.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // Price and vintage row
            Row(Modifier.fillMaxWidth()) {
                InfoChip("Price", "\u20AC" + wine.price.toInt().toString(), Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip("Vintage", wine.vintage, Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                InfoChip("Grape", wine.grape, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details
            DetailSection("Reference", wine.reference)
            DetailSection("Grape Variety", wine.grape)

            if (wine.aroma.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
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
            if (wine.peakMaturity.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailSection("Peak Maturity", wine.peakMaturity)
            }
            if (wine.binLocation.isNotEmpty()) {
                DetailSection("Bin Location", wine.binLocation)
            }
            Spacer(modifier = Modifier.height(60.dp))
        }
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
