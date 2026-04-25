package com.vinotheque.nativeapp.ui

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWineScreen(viewModel: WineViewModel, onNavigateBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var vintage by remember { mutableStateOf("") }
    var grape by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Red") }
    
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        capturedImage = bitmap
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0d0505))
    ) {
        TopAppBar(
            title = { Text("Add New Wine", color = Color(0xFFd4a54e)) },
            navigationIcon = {
                Button(onClick = onNavigateBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("←", color = Color(0xFFd4a54e), fontSize = 20.sp)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1a0a0a))
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            // Native Camera Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0x332d1212), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImage != null) {
                    Image(
                        bitmap = capturedImage!!.asImageBitmap(),
                        contentDescription = "Wine Photo",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd4a54e))
                    ) {
                        Text("📸 Take Native Photo", color = Color.Black)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Wine Name", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, focusedBorderColor = Color(0xFFd4a54e))
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = region, onValueChange = { region = it },
                label = { Text("Region", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, focusedBorderColor = Color(0xFFd4a54e))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vintage, onValueChange = { vintage = it },
                    label = { Text("Vintage", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, focusedBorderColor = Color(0xFFd4a54e))
                )
                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Price (€)", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, focusedBorderColor = Color(0xFFd4a54e))
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    // Convert native bitmap to base64
                    var base64Img: String? = null
                    capturedImage?.let { bmp ->
                        val baos = ByteArrayOutputStream()
                        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        base64Img = "data:image/jpeg;base64," + Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                    }
                    
                    viewModel.saveWine(
                        name = name,
                        region = region,
                        vintage = vintage,
                        grape = grape,
                        price = price.toDoubleOrNull() ?: 0.0,
                        type = type,
                        image = base64Img
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFd4a54e))
            ) {
                Text("💾 Save to Cellar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
