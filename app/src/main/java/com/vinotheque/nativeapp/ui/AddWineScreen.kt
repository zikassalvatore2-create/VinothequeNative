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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R
import com.vinotheque.nativeapp.ui.theme.DessertColor
import com.vinotheque.nativeapp.ui.theme.RedWineColor
import com.vinotheque.nativeapp.ui.theme.RoseWineColor
import com.vinotheque.nativeapp.ui.theme.SparklingColor
import com.vinotheque.nativeapp.ui.theme.WhiteWineColor
import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R
import java.io.ByteArrayOutputStream

private val fieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
    var tastingNotes by remember { mutableStateOf("") }
    var decanting by remember { mutableStateOf("") }
    var servingTemp by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var ratingSource by remember { mutableStateOf("") }
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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(stringResource(R.string.add_wine), color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                val result = viewModel.enrichWine(grape, name)
                type = result.type; dryness = result.dryness
                glassType = result.glass
                if (aroma.isEmpty()) aroma = result.aroma
                if (foodPairing.isEmpty()) foodPairing = result.foodPairing
                if (decanting.isEmpty()) decanting = result.decanting
                if (servingTemp.isEmpty()) servingTemp = result.servingTemp
                if (keywords.isEmpty()) keywords = result.keywords
            }) {
                Icon(Icons.Default.AutoAwesome, stringResource(R.string.smart_fill), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
        }

        Column(modifier = Modifier.weight(1f).padding(16.dp).verticalScroll(rememberScrollState())) {
            // Camera area
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
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
                                Icon(Icons.Default.CameraAlt, stringResource(R.string.camera), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            }
                            Text(stringResource(R.string.camera), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(Icons.Default.Image, stringResource(R.string.gallery), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            }
                            Text(stringResource(R.string.gallery), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { searchWineImage(context, name) }) {
                                Icon(Icons.Default.Search, stringResource(R.string.search_google), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            }
                            Text(stringResource(R.string.search_google), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.wine_name)) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = region, onValueChange = { region = it },
                    label = { Text(stringResource(R.string.region)) }, modifier = Modifier.weight(1f), colors = fieldColors)
                OutlinedTextField(value = vintage, onValueChange = { vintage = it },
                    label = { Text(stringResource(R.string.vintage)) }, modifier = Modifier.weight(0.5f), colors = fieldColors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = grape, onValueChange = { grape = it },
                    label = { Text(stringResource(R.string.grape)) }, modifier = Modifier.weight(1f), colors = fieldColors)
                OutlinedTextField(value = price, onValueChange = { price = it },
                    label = { Text(stringResource(R.string.price)) }, modifier = Modifier.weight(0.5f), colors = fieldColors)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Type
            Text(getLocalizedLabel("Type"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Red", "White", "Rose", "Sparkling", "Dessert").forEach { t ->
                    Button(onClick = { type = t },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == t) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    ) { Text(getLocalizedType(t), color = if (type == t) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Dryness
            Text(getLocalizedLabel("Dry"), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("Dry", "Off-Dry", "Sweet", "Brut").forEach { d ->
                    Button(onClick = { dryness = d },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dryness == d) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    ) { Text(getLocalizedDryness(d), color = if (dryness == d) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Rating
            Text("Rating: " + ratingVal.toInt().toString() + "/100", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
            Slider(value = ratingVal, onValueChange = { ratingVal = it },
                valueRange = 50f..100f, steps = 49,
                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant))
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(value = aroma, onValueChange = { aroma = it },
                label = { Text(stringResource(R.string.aroma_profile)) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = foodPairing, onValueChange = { foodPairing = it },
                label = { Text(stringResource(R.string.food_pairing)) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = glassType, onValueChange = { glassType = it },
                label = { Text(stringResource(R.string.glass_type)) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = binLocation, onValueChange = { binLocation = it },
                label = { Text(stringResource(R.string.bin_location)) }, placeholder = { Text("e.g. A12, Shelf 3", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = keywords, onValueChange = { keywords = it },
                label = { Text(stringResource(R.string.keywords)) }, placeholder = { Text(stringResource(R.string.keywords_hint), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = decanting, onValueChange = { decanting = it },
                    label = { Text(stringResource(R.string.decanting)) }, modifier = Modifier.weight(1f), colors = fieldColors)
                OutlinedTextField(value = servingTemp, onValueChange = { servingTemp = it },
                    label = { Text(stringResource(R.string.serving_temp)) }, modifier = Modifier.weight(1f), colors = fieldColors)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = tastingNotes, onValueChange = { tastingNotes = it },
                label = { Text(stringResource(R.string.tasting_notes)) }, modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = ratingSource, onValueChange = { ratingSource = it },
                label = { Text(stringResource(R.string.rating_source)) }, placeholder = { Text("e.g. James Suckling, Vivino", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                modifier = Modifier.fillMaxWidth(), colors = fieldColors)
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    var img: String? = null
                    if (capturedImage != null) {
                        img = resizeBitmap(capturedImage!!, viewModel.imageQuality.value)
                    }
                    viewModel.saveWine(name, region, vintage, grape, price.toDoubleOrNull() ?: 0.0,
                        type, dryness, ratingVal.toInt(), aroma, foodPairing, img,
                        binLocation, glassType.ifBlank { null },
                        tastingNotes, decanting, servingTemp, ratingSource, keywords)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.save_to_cellar), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
