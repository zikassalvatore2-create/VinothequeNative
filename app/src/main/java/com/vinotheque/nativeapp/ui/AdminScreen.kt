package com.vinotheque.nativeapp.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R
import java.io.ByteArrayOutputStream
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine
import androidx.compose.material3.MaterialTheme
import com.vinotheque.nativeapp.ui.theme.WineRed

data class EditableWine(
    val reference: String,
    var name: String, var region: String, var vintage: String,
    var grape: String, var type: String, var dryness: String,
    var price: String, var rating: String,
    var aroma: String, var foodPairing: String,
    var binLocation: String, var sold: String,
    var glassType: String,
    var decanting: String, var servingTemp: String,
    var keywords: String, var tastingNotes: String,
    var ratingSource: String,
    var imageBase64: String?
)

fun Wine.toEditable() = EditableWine(reference, name, region, vintage, grape, type, dryness,
    price.toString(), rating.toString(), aroma, foodPairing, binLocation, sold.toString(), glassType,
    decanting, servingTemp, keywords, tastingNotes, ratingSource, image)

fun EditableWine.toWine(original: Wine) = original.copy(
    name = name, region = region, vintage = vintage, grape = grape, type = type, dryness = dryness,
    price = price.toDoubleOrNull() ?: original.price, rating = rating.toIntOrNull() ?: original.rating,
    aroma = aroma, foodPairing = foodPairing, binLocation = binLocation,
    sold = sold.toIntOrNull() ?: original.sold,
    glassType = glassType,
    decanting = decanting,
    servingTemp = servingTemp,
    keywords = keywords,
    tastingNotes = tastingNotes,
    ratingSource = ratingSource,
    image = imageBase64)

private val miniFieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary, unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
    cursorColor = MaterialTheme.colorScheme.primary, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)

/** Resize bitmap to max 800px on longest side, PNG format for transparency */

@Composable
fun AdminScreen(viewModel: WineViewModel, onBack: () -> Unit, onOpenSales: () -> Unit = {}) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val context = LocalContext.current
    val edits = remember { mutableStateMapOf<String, EditableWine>() }
    var photoTargetRef by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isRepairing by remember { mutableStateOf(false) }

    // Filter wines by search
    val filteredWines = if (searchQuery.isBlank()) wines else {
        val q = searchQuery.lowercase()
        wines.filter {
            it.reference.lowercase().contains(q) || it.name.lowercase().contains(q) ||
            it.grape.lowercase().contains(q) || it.region.lowercase().contains(q)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null && photoTargetRef != null) {
            val b64 = resizeBitmap(bitmap, viewModel.imageQuality.value)
            val ref = photoTargetRef!!
            val current = edits[ref]
            if (current != null) { edits[ref] = current.copy(imageBase64 = b64) }
            photoTargetRef = null
            Toast.makeText(context, context.getString(R.string.toast_photo_captured), Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && photoTargetRef != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val b64 = resizeBitmap(bitmap, viewModel.imageQuality.value)
                    val ref = photoTargetRef!!
                    val current = edits[ref]
                    if (current != null) { edits[ref] = current.copy(imageBase64 = b64) }
                    Toast.makeText(context, context.getString(R.string.toast_photo_loaded), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { Toast.makeText(context, context.getString(R.string.toast_photo_failed), Toast.LENGTH_SHORT).show() }
            photoTargetRef = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(1f))
            Text(stringResource(R.string.admin_table), color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            if (isRepairing) {
                Text(stringResource(R.string.auto_saved, ""), color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            } else {
                IconButton(onClick = onOpenSales) {
                    Icon(Icons.Default.TrendingUp, stringResource(R.string.sales), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = {
                    isRepairing = true
                    viewModel.repairAllImages { count ->
                        isRepairing = false
                        Toast.makeText(context, context.getString(R.string.toast_repaired, count), Toast.LENGTH_LONG).show()
                    }
                }) {
                    Icon(Icons.Default.LocalBar, stringResource(R.string.repair), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            Text(filteredWines.size.toString() + "/" + wines.size.toString() + "  ", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, stringResource(R.string.search_hint), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary, focusedTextColor = MaterialTheme.colorScheme.onSurface, unfocusedTextColor = MaterialTheme.colorScheme.onSurface)
        )

        if (filteredWines.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalBar, "Empty", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(if (searchQuery.isNotBlank()) stringResource(R.string.no_wines_match, searchQuery) else stringResource(R.string.no_wines_edit),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredWines, key = { it.reference }) { wine ->
                    val edit = edits.getOrPut(wine.reference) { wine.toEditable() }
                    AdminRow(
                        edit = edit,
                        onFieldChange = { f, v ->
                            val u = edits[wine.reference] ?: wine.toEditable()
                            edits[wine.reference] = when (f) {
                                "name" -> u.copy(name = v); "region" -> u.copy(region = v)
                                "vintage" -> u.copy(vintage = v); "grape" -> u.copy(grape = v)
                                "type" -> u.copy(type = v); "dryness" -> u.copy(dryness = v)
                                "price" -> u.copy(price = v); "rating" -> u.copy(rating = v)
                                "aroma" -> u.copy(aroma = v); "foodPairing" -> u.copy(foodPairing = v)
                                "binLocation" -> u.copy(binLocation = v)
                                "sold" -> u.copy(sold = v)
                                "decanting" -> u.copy(decanting = v)
                                "servingTemp" -> u.copy(servingTemp = v)
                                "keywords" -> u.copy(keywords = v)
                                "tastingNotes" -> u.copy(tastingNotes = v)
                                "ratingSource" -> u.copy(ratingSource = v)
                                "glassType" -> u.copy(glassType = v)
                                else -> u
                            }
                        },
                        onTakePhoto = { photoTargetRef = wine.reference; cameraLauncher.launch(null) },
                        onPickGallery = { photoTargetRef = wine.reference; galleryLauncher.launch("image/*") },
                        onSave = {
                            edits[wine.reference]?.let {
                                viewModel.updateWine(it.toWine(wine))
                                Toast.makeText(context, context.getString(R.string.toast_saved_wine, it.name), Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            viewModel.deleteWine(wine.reference); edits.remove(wine.reference)
                            Toast.makeText(context, context.getString(R.string.toast_deleted), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun AdminRow(edit: EditableWine, onFieldChange: (String, String) -> Unit,
             onTakePhoto: () -> Unit, onPickGallery: () -> Unit, onSave: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Photo + reference + actions row
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Thumbnail - properly clipped and sized
                Box(
                    modifier = Modifier.size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncWineImage(
                        imageData = edit.imageBase64,
                        contentDescription = "Wine",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)).padding(2.dp),
                        contentScale = ContentScale.Fit,
                        placeholder = {
                            Icon(Icons.Default.LocalBar, "No photo", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.size(24.dp))
                        }
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Camera & gallery buttons
                Column {
                    Row {
                        IconButton(onClick = onTakePhoto, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.CameraAlt, stringResource(R.string.camera), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = onPickGallery, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Image, stringResource(R.string.gallery), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { searchWineImage(context, edit.name) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Search, stringResource(R.string.search_google), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                // Reference badge
                Box(modifier = Modifier.background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("#" + edit.reference, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            MiniField("Name", edit.name) { onFieldChange("name", it) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Region", edit.region, Modifier.weight(1f)) { onFieldChange("region", it) }
                MiniField("Vintage", edit.vintage, Modifier.weight(0.4f)) { onFieldChange("vintage", it) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Grape", edit.grape, Modifier.weight(1f)) { onFieldChange("grape", it) }
                MiniField("Type", edit.type, Modifier.weight(0.5f)) { onFieldChange("type", it) }
                MiniField("Dry", edit.dryness, Modifier.weight(0.5f)) { onFieldChange("dryness", it) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Price", edit.price, Modifier.weight(1f)) { onFieldChange("price", it) }
                MiniField("Rating", edit.rating, Modifier.weight(1f)) { onFieldChange("rating", it) }
            }
            MiniField("Aroma", edit.aroma) { onFieldChange("aroma", it) }
            MiniField("Pairing", edit.foodPairing) { onFieldChange("foodPairing", it) }
            MiniField("Recommended Glass", edit.glassType) { onFieldChange("glassType", it) }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Bin/Location", edit.binLocation, Modifier.weight(1f)) { onFieldChange("binLocation", it) }
                MiniField("Sold", edit.sold, Modifier.weight(0.4f)) { onFieldChange("sold", it) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Decant", edit.decanting, Modifier.weight(1f)) { onFieldChange("decanting", it) }
                MiniField("Temp", edit.servingTemp, Modifier.weight(1f)) { onFieldChange("servingTemp", it) }
            }
            MiniField("Keywords", edit.keywords) { onFieldChange("keywords", it) }
            MiniField("Tasting Notes", edit.tastingNotes) { onFieldChange("tastingNotes", it) }
            MiniField("Rating Source", edit.ratingSource) { onFieldChange("ratingSource", it) }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Icon(Icons.Default.Save, stringResource(R.string.save), tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                    Text(" " + stringResource(R.string.save), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WineRed)) {
                    Icon(Icons.Default.Delete, stringResource(R.string.delete), tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(" " + stringResource(R.string.delete), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MiniField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(getLocalizedLabel(label), fontSize = 10.sp) },
        modifier = modifier.padding(vertical = 2.dp), singleLine = true, colors = miniFieldColors,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp))
}


