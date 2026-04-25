package com.vinotheque.nativeapp.ui

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val BarBg = Color(0xFF1A0A0A)

@Composable
fun AddWineScreen(viewModel: WineViewModel, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var vintage by remember { mutableStateOf("") }
    var grape by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Red") }
    var dryness by remember { mutableStateOf("Dry") }
    var ratingVal by remember { mutableFloatStateOf(90f) }
    var aroma by remember { mutableStateOf("") }
    var foodPairing by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? -> capturedImage = bitmap }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(BarBg).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text(text = "< Back", color = Gold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Add Wine", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            // Smart Fill button
            TextButton(onClick = {
                val result = viewModel.enrichWine(grape, name)
                type = result.type
                dryness = result.dryness
                if (aroma.isEmpty()) aroma = result.aroma
                if (foodPairing.isEmpty()) foodPairing = result.foodPairing
            }) {
                Text("Smart Fill", color = Gold, fontSize = 14.sp)
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState())
        ) {
            // Camera
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .background(BarBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImage != null) {
                    Image(bitmap = capturedImage!!.asImageBitmap(), contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize())
                } else {
                    Button(onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                        Text("Take Photo", color = Color.Black)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Wine Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = region, onValueChange = { region = it },
                    label = { Text("Region") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = vintage, onValueChange = { vintage = it },
                    label = { Text("Vintage") }, modifier = Modifier.weight(0.5f))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = grape, onValueChange = { grape = it },
                    label = { Text("Grape") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("Price") }, modifier = Modifier.weight(0.5f))
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Type selector
            Text("Type:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Red", "White", "Rose", "Sparkling", "Dessert").forEach { t ->
                    Button(onClick = { type = t },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == t) Gold else BarBg),
                        modifier = Modifier.weight(1f)
                    ) { Text(t, color = if (type == t) Color.Black else Color.White, fontSize = 10.sp) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Dryness selector
            Text("Dryness:", color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Dry", "Off-Dry", "Sweet", "Brut").forEach { d ->
                    Button(onClick = { dryness = d },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dryness == d) Gold else BarBg),
                        modifier = Modifier.weight(1f)
                    ) { Text(d, color = if (dryness == d) Color.Black else Color.White, fontSize = 10.sp) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Rating slider
            Text("Rating: " + ratingVal.toInt().toString() + "/100", color = Color.White, fontSize = 14.sp)
            Slider(value = ratingVal, onValueChange = { ratingVal = it },
                valueRange = 50f..100f, steps = 49,
                colors = SliderDefaults.colors(thumbColor = Gold, activeTrackColor = Gold))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = aroma, onValueChange = { aroma = it },
                label = { Text("Aroma Profile") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(value = foodPairing, onValueChange = { foodPairing = it },
                label = { Text("Food Pairing") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    var base64Img: String? = null
                    if (capturedImage != null) {
                        val baos = ByteArrayOutputStream()
                        capturedImage!!.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        base64Img = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                    }
                    viewModel.saveWine(name, region, vintage, grape,
                        price.toDoubleOrNull() ?: 0.0, type, dryness,
                        ratingVal.toInt(), aroma, foodPairing, base64Img)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)
            ) {
                Text("Save to Cellar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
