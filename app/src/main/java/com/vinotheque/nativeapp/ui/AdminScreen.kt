package com.vinotheque.nativeapp.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val CardBg = Color(0xFF1A0A0A)
private val BarBg = Color(0xFF1A0A0A)

data class EditableWine(
    val reference: String,
    var name: String, var region: String, var vintage: String,
    var grape: String, var type: String, var dryness: String,
    var price: String, var rating: String,
    var aroma: String, var foodPairing: String
)

fun Wine.toEditable() = EditableWine(
    reference, name, region, vintage, grape, type, dryness,
    price.toString(), rating.toString(), aroma, foodPairing
)

fun EditableWine.toWine(original: Wine) = original.copy(
    name = name, region = region, vintage = vintage, grape = grape,
    type = type, dryness = dryness,
    price = price.toDoubleOrNull() ?: original.price,
    rating = rating.toIntOrNull() ?: original.rating,
    aroma = aroma, foodPairing = foodPairing
)

@Composable
fun AdminScreen(viewModel: WineViewModel, onBack: () -> Unit) {
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val context = LocalContext.current

    // Track edits per wine reference
    val edits = remember { mutableStateMapOf<String, EditableWine>() }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Top bar
        Row(modifier = Modifier.fillMaxWidth().background(BarBg).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("< Back", color = Gold, fontSize = 16.sp) }
            Spacer(modifier = Modifier.weight(1f))
            Text("Admin Table", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(wines.size.toString() + " wines", color = Color.Gray, fontSize = 14.sp,
                modifier = Modifier.padding(end = 8.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
        Text("Edit fields inline. Tap Save to persist.", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))

        if (wines.isEmpty()) {
            Text("No wines to edit", color = Color.Gray, fontSize = 16.sp)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(wines, key = { it.reference }) { wine ->
                    val edit = edits.getOrPut(wine.reference) { wine.toEditable() }
                    AdminRow(
                        edit = edit,
                        onFieldChange = { field, value ->
                            val updated = edits[wine.reference] ?: wine.toEditable()
                            val newEdit = when (field) {
                                "name" -> updated.copy(name = value)
                                "region" -> updated.copy(region = value)
                                "vintage" -> updated.copy(vintage = value)
                                "grape" -> updated.copy(grape = value)
                                "type" -> updated.copy(type = value)
                                "dryness" -> updated.copy(dryness = value)
                                "price" -> updated.copy(price = value)
                                "rating" -> updated.copy(rating = value)
                                "aroma" -> updated.copy(aroma = value)
                                "foodPairing" -> updated.copy(foodPairing = value)
                                else -> updated
                            }
                            edits[wine.reference] = newEdit
                        },
                        onSave = {
                            val e = edits[wine.reference]
                            if (e != null) {
                                viewModel.updateWine(e.toWine(wine))
                                Toast.makeText(context, "Saved: " + e.name, Toast.LENGTH_SHORT).show()
                            }
                        },
                        onDelete = {
                            viewModel.deleteWine(wine.reference)
                            edits.remove(wine.reference)
                            Toast.makeText(context, "Deleted: " + wine.name, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
        }
    }
}

@Composable
fun AdminRow(
    edit: EditableWine,
    onFieldChange: (String, String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Name
            MiniField("Name", edit.name) { onFieldChange("name", it) }
            // Row 2: Region + Vintage
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Region", edit.region, Modifier.weight(1f)) { onFieldChange("region", it) }
                MiniField("Vintage", edit.vintage, Modifier.weight(0.4f)) { onFieldChange("vintage", it) }
            }
            // Row 3: Grape + Type + Dryness
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Grape", edit.grape, Modifier.weight(1f)) { onFieldChange("grape", it) }
                MiniField("Type", edit.type, Modifier.weight(0.5f)) { onFieldChange("type", it) }
                MiniField("Dry", edit.dryness, Modifier.weight(0.5f)) { onFieldChange("dryness", it) }
            }
            // Row 4: Price + Rating
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniField("Price", edit.price, Modifier.weight(1f)) { onFieldChange("price", it) }
                MiniField("Rating", edit.rating, Modifier.weight(1f)) { onFieldChange("rating", it) }
            }
            // Row 5: Aroma
            MiniField("Aroma", edit.aroma) { onFieldChange("aroma", it) }
            // Row 6: Pairing
            MiniField("Pairing", edit.foodPairing) { onFieldChange("foodPairing", it) }

            Spacer(modifier = Modifier.height(8.dp))
            // Buttons
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(onClick = onDelete, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF661111))) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MiniField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, fontSize = 10.sp) },
        modifier = modifier.padding(vertical = 2.dp),
        singleLine = true,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
    )
}
