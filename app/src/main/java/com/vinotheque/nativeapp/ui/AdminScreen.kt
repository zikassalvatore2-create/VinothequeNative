package com.vinotheque.nativeapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

data class EditableWine(
    val reference: String,
    var name: String, var region: String, var vintage: String,
    var grape: String, var type: String, var dryness: String,
    var price: String, var rating: String,
    var aroma: String, var foodPairing: String
)

fun Wine.toEditable() = EditableWine(reference, name, region, vintage, grape, type, dryness,
    price.toString(), rating.toString(), aroma, foodPairing)

fun EditableWine.toWine(original: Wine) = original.copy(
    name = name, region = region, vintage = vintage, grape = grape, type = type, dryness = dryness,
    price = price.toDoubleOrNull() ?: original.price, rating = rating.toIntOrNull() ?: original.rating,
    aroma = aroma, foodPairing = foodPairing)

private val miniFieldColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = WineGold, unfocusedBorderColor = WineSurface,
    focusedLabelColor = WineGold, unfocusedLabelColor = TextTertiary,
    cursorColor = WineGold, focusedTextColor = Color.White, unfocusedTextColor = Color.White)

@Composable
fun AdminScreen(viewModel: WineViewModel, onBack: () -> Unit) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val context = LocalContext.current
    val edits = remember { mutableStateMapOf<String, EditableWine>() }

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
                Text("No wines to edit", color = TextSecondary, fontSize = 16.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wines, key = { it.reference }) { wine ->
                    val edit = edits.getOrPut(wine.reference) { wine.toEditable() }
                    AdminRow(edit = edit,
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
                        onSave = { edits[wine.reference]?.let {
                            viewModel.updateWine(it.toWine(wine))
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show() } },
                        onDelete = { viewModel.deleteWine(wine.reference); edits.remove(wine.reference)
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show() })
                }
            }
        }
    }
}

@Composable
fun AdminRow(edit: EditableWine, onFieldChange: (String, String) -> Unit, onSave: () -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
        Column(modifier = Modifier.padding(12.dp)) {
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
