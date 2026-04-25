package com.vinotheque.nativeapp.ui

import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.data.Wine
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface
import java.io.ByteArrayOutputStream

data class EditableWine(
    val reference: String,
    var name: String, var region: String, var vintage: String,
    var grape: String, var type: String, var dryness: String,
    var price: String, var rating: String,
    var aroma: String, var foodPairing: String,
    var imageBase64: String?
)

fun Wine.toEditable() = EditableWine(reference, name, region, vintage, grape, type, dryness,
    price.toString(), rating.toString(), aroma, foodPairing, image)

fun EditableWine.toWine(original: Wine) = original.copy(
    name = name, region = region, vintage = vintage, grape = grape, type = type, dryness = dryness,
    price = price.toDoubleOrNull() ?: original.price, rating = rating.toIntOrNull() ?: original.rating,
    aroma = aroma, foodPairing = foodPairing, image = imageBase64)

private val miniFieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WineGold, unfocusedBorderColor = WineSurface,
    focusedLabelColor = WineGold, unfocusedLabelColor = TextTertiary,
    cursorColor = WineGold, focusedTextColor = Color.White, unfocusedTextColor = Color.White)

@Composable
fun AdminScreen(viewModel: WineViewModel, onBack: () -> Unit) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val context = LocalContext.current
    val edits = remember { mutableStateMapOf<String, EditableWine>() }
    // Track which wine ref is waiting for a photo
    var photoTargetRef by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null && photoTargetRef != null) {
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val b64 = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            val ref = photoTargetRef!!
            val current = edits[ref]
            if (current != null) { edits[ref] = current.copy(imageBase64 = b64) }
            photoTargetRef = null
            Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && photoTargetRef != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    val b64 = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                    val ref = photoTargetRef!!
                    val current = edits[ref]
                    if (current != null) { edits[ref] = current.copy(imageBase64 = b64) }
                    Toast.makeText(context, "Photo loaded!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show() }
            photoTargetRef = null
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark)) {
        Row(modifier = Modifier.fillMaxWidth().background(WineSurface).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Admin Table", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(wines.size.toString() + " wines  ", color = TextSecondary, fontSize = 13.sp)
        }

        if (wines.isEmpty()) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocalBar, "Empty", tint = TextTertiary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No wines to edit", color = TextSecondary, fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wines, key = { it.reference }) { wine ->
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
                                else -> u
                            }
                        },
                        onTakePhoto = {
                            photoTargetRef = wine.reference
                            cameraLauncher.launch(null)
                        },
                        onPickGallery = {
                            photoTargetRef = wine.reference
                            galleryLauncher.launch("image/*")
                        },
                        onSave = {
                            edits[wine.reference]?.let {
                                viewModel.updateWine(it.toWine(wine))
                                Toast.makeText(context, "Saved: " + it.name, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            viewModel.deleteWine(wine.reference); edits.remove(wine.reference)
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminRow(edit: EditableWine, onFieldChange: (String, String) -> Unit,
             onTakePhoto: () -> Unit, onPickGallery: () -> Unit, onSave: () -> Unit, onDelete: () -> Unit) {
    // Decode thumbnail
    val thumb = remember(edit.imageBase64) {
        if (edit.imageBase64 != null) {
            try {
                val d = edit.imageBase64!!.substringAfter(",")
                val bytes = Base64.decode(d, Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) { null }
        } else null
    }

    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Photo row
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(60.dp).background(WineDark, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (thumb != null) {
                        Image(bitmap = thumb, contentDescription = "Wine photo",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.LocalBar, "No photo", tint = TextTertiary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onTakePhoto) {
                    Icon(Icons.Default.CameraAlt, "Camera", tint = WineGold, modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onPickGallery) {
                    Icon(Icons.Default.Image, "Gallery", tint = WineGold, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(edit.reference, color = TextTertiary, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))

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
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WineGold)) {
                    Icon(Icons.Default.Save, "Save", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Text(" Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WineRed)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                    Text(" Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MiniField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label, fontSize = 10.sp) },
        modifier = modifier.padding(vertical = 2.dp), singleLine = true, colors = miniFieldColors,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp))
}
