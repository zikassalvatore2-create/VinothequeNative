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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun WineDetailScreen(wine: Wine, onBack: () -> Unit, onDelete: () -> Unit) {
    val decodedBitmap: ImageBitmap? = remember(wine.image) {
        if (wine.image != null) {
            try {
                val data = wine.image.substringAfter(",")
                val bytes = Base64.decode(data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else null
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(BarBg).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("< Back", color = Gold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Wine Details", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDelete) {
                Text("Delete", color = Color.Red, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            // Image
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp)
                    .background(Color.Black, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (decodedBitmap != null) {
                    Image(bitmap = decodedBitmap, contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text("W", fontSize = 60.sp, color = Gold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Name and rating
            Text(wine.name, color = Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(wine.rating.toString() + "/100", color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("E" + wine.price.toInt().toString(), color = Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(color = Gold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))

            // Details grid
            DetailRow("Region", wine.region)
            DetailRow("Vintage", wine.vintage)
            DetailRow("Grape", wine.grape)
            DetailRow("Type", wine.type)
            DetailRow("Dryness", wine.dryness)
            DetailRow("Reference", wine.reference)

            if (wine.aroma.isNotEmpty()) {
                HorizontalDivider(color = Gold.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 12.dp))
                Text("Aroma Profile", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(wine.aroma, color = Color.White, fontSize = 14.sp)
            }

            if (wine.foodPairing.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Food Pairing", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(wine.foodPairing, color = Color.White, fontSize = 14.sp)
            }

            if (wine.peakMaturity.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow("Peak Maturity", wine.peakMaturity)
            }
            if (wine.binLocation.isNotEmpty()) {
                DetailRow("Bin Location", wine.binLocation)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isEmpty()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
