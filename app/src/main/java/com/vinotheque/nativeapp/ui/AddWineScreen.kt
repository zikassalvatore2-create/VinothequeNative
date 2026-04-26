package com.vinotheque.nativeapp.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WineCard
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineSurface
import java.io.ByteArrayOutputStream

private val fieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WineGold,
    unfocusedBorderColor = WineSurface,
    focusedLabelColor = WineGold,
    unfocusedLabelColor = TextTertiary,
    cursorColor = WineGold,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

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
    var binLocation by remember { mutableStateOf("") }
    var glassType by remember { mutableStateOf("") }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? -> capturedImage = bitmap }

    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                capturedImage = bitmap
            } catch (e: Exception) { /* ignore */ }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(WineSurface).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Add Wine", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                val result = viewModel.enrichWine(grape, name)
                type = result.type; dryness = result.dryness
                glassType = result.glass
                if (aroma.isEmpty()) aroma = result.aroma
                if (foodPairing.isEmpty()) foodPairing = result.foodPairing
            }) {
                Icon(Icons.Default.AutoAwesome, "Smart Fill", tint = WineGold, modifier = Modifier.size(24.dp))
            }
        }

        Column(modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState())) {
            // Camera area
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WineSurface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImage != null) {
                    Image(bitmap = capturedImage!!.asImageBitmap(), contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { cameraLauncher.launch(null) }) {
                                Icon(Icons.Default.CameraAlt, "Camera", tint = WineGold, modifier = Modifier.size(36.dp))
                            }
                            Text("Camera", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Image, "Gallery", tint = WineGold, modifier = Modifier.size(36.dp))
                            }
                            Text("Gallery", color = TextSecondary, fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { searchWineImage(context, name) }) {
                                Icon(Icons.Default.Search, "Search", tint = WineGold, modifier = Modifier.size(36.dp))
                            }
                            Text("Search", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text("Wine Name") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = region, onValueChange = { region = it },
                    label = { Text("Region") }, modifier = Modifier.weight(1f), colors = fieldColors)
                OutlinedTextField(value = vintage, onValueChange = { vintage = it },
                    label = { Text("Vintage") }, modifier = Modifier.weight(0.5f), colors = fieldColors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = grape, onValueChange = { grape = it },
                    label = { Text("Grape") }, modifier = Modifier.weight(1f), colors = fieldColors)
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text("Price") }, modifier = Modifier.weight(0.5f), colors = fieldColors)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Type
            Text("Type", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Red", "White", "Rose", "Sparkling", "Dessert").forEach { t ->
                    Button(onClick = { type = t },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == t) WineGold else WineSurface),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    ) { Text(t, color = if (type == t) Color.Black else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Dryness
            Text("Dryness", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Dry", "Off-Dry", "Sweet", "Brut").forEach { d ->
                    Button(onClick = { dryness = d },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dryness == d) WineGold else WineSurface),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    ) { Text(d, color = if (dryness == d) Color.Black else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Rating
            Text("Rating: " + ratingVal.toInt().toString() + "/100", color = TextSecondary, fontSize = 13.sp)
            Slider(value = ratingVal, onValueChange = { ratingVal = it },
                valueRange = 50f..100f, steps = 49,
                colors = SliderDefaults.colors(thumbColor = WineGold, activeTrackColor = WineGold,
                    inactiveTrackColor = WineSurface))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = aroma, onValueChange = { aroma = it },
                label = { Text("Aroma Profile") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = foodPairing, onValueChange = { foodPairing = it },
                label = { Text("Food Pairing") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = glassType, onValueChange = { glassType = it },
                label = { Text("Recommended Glass") }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = binLocation, onValueChange = { binLocation = it },
                    label = { Text("Bin Location") }, placeholder = { Text("e.g. A12, Shelf 3", color = TextTertiary) },
                    modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    var img: String? = null
                    if (capturedImage != null) {
                        img = resizeBitmap(capturedImage!!)
                    }
                    viewModel.saveWine(name, region, vintage, grape, price.toDoubleOrNull() ?: 0.0,
                        type, dryness, ratingVal.toInt(), aroma, foodPairing, img,
                        binLocation, glassType.ifBlank { null })
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WineGold)
            ) {
                Text("Save to Cellar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
