const fs = require('fs');
const path = require('path');

const rootDir = "C:\\Users\\zakar\\Downloads\\VinothequeNative";
const pkgDir = path.join(rootDir, 'app', 'src', 'main', 'java', 'com', 'vinotheque', 'nativeapp');

// 1. Update Wine.kt (add image: String?)
const wineKt = `package com.vinotheque.nativeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wines")
data class Wine(
    @PrimaryKey val reference: String,
    val name: String,
    val region: String,
    val vintage: String,
    val grape: String,
    val type: String,
    val price: Double,
    val rating: Int,
    val peakMaturity: String,
    val binLocation: String,
    val body: Int,
    val tannin: Int,
    val acidity: Int,
    val sweetness: Int,
    val image: String? = null // Base64 Native Image
)
`;
fs.writeFileSync(path.join(pkgDir, 'data', 'Wine.kt'), wineKt);

// 2. AddWineScreen.kt
const addWineKt = `package com.vinotheque.nativeapp.ui

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
`;
fs.writeFileSync(path.join(pkgDir, 'ui', 'AddWineScreen.kt'), addWineKt);

// 3. Update WineViewModel.kt to add saveWine function
let vmKt = fs.readFileSync(path.join(pkgDir, 'ui', 'WineViewModel.kt'), 'utf8');
const saveWineFun = `
    fun saveWine(name: String, region: String, vintage: String, grape: String, price: Double, type: String, image: String?) {
        viewModelScope.launch {
            dao.insertWine(
                Wine(
                    reference = "REF\${System.currentTimeMillis()}",
                    name = name.ifEmpty { "Unknown Wine" },
                    region = region,
                    vintage = vintage,
                    grape = grape,
                    type = type,
                    price = price,
                    rating = 90,
                    peakMaturity = "",
                    binLocation = "",
                    body = 3,
                    tannin = 3,
                    acidity = 3,
                    sweetness = 1,
                    image = image
                )
            )
        }
    }
`;
vmKt = vmKt.replace('fun addSampleWine() {', saveWineFun + '\n    fun addSampleWine() {');
fs.writeFileSync(path.join(pkgDir, 'ui', 'WineViewModel.kt'), vmKt);

// 4. Update MainActivity.kt Navigation Graph
let mainKt = fs.readFileSync(path.join(pkgDir, 'MainActivity.kt'), 'utf8');
mainKt = mainKt.replace('import androidx.navigation.compose.*', 'import androidx.navigation.compose.*\nimport com.vinotheque.nativeapp.ui.AddWineScreen');

const oldScaffoldContent = `    ) { innerPadding ->
        if (selectedTab == 1) {
            Modifier.padding(innerPadding)
            CellarScreen(viewModel)
        } else {
            // Dashboard placeholder
            Text(
                "Dashboard Coming Soon",
                color = Color.White,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }`;

const newScaffoldContent = `    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            NavHost(navController = navController, startDestination = "tabs") {
                composable("tabs") {
                    if (selectedTab == 1) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CellarScreen(viewModel)
                            // Native FAB
                            FloatingActionButton(
                                onClick = { navController.navigate("add_wine") },
                                containerColor = Color(0xFFd4a54e),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                            ) {
                                Text("+", color = Color.Black, fontSize = 24.sp)
                            }
                        }
                    } else {
                        Text("Dashboard Coming Soon", color = Color.White, modifier = Modifier.padding(16.dp))
                    }
                }
                composable("add_wine") {
                    AddWineScreen(viewModel) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }`;
mainKt = mainKt.replace(oldScaffoldContent, newScaffoldContent);
fs.writeFileSync(path.join(pkgDir, 'MainActivity.kt'), mainKt);

// 5. Update CellarScreen.kt to show Image using Coil (Base64 approach requires custom decoder or simple ImageBitmap)
let cellarKt = fs.readFileSync(path.join(pkgDir, 'ui', 'CellarScreen.kt'), 'utf8');
cellarKt = cellarKt.replace('import androidx.compose.ui.text.font.FontWeight', 'import androidx.compose.ui.text.font.FontWeight\nimport android.graphics.BitmapFactory\nimport android.util.Base64\nimport androidx.compose.foundation.Image\nimport androidx.compose.ui.graphics.asImageBitmap\nimport androidx.compose.ui.layout.ContentScale');

const oldWineCardImg = `        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),`;

const newWineCardImg = `        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black)) {
                if (wine.image != null) {
                    try {
                        val base64String = wine.image.substringAfter(",")
                        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Wine Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } catch (e: Exception) {
                        Text("🍷", fontSize = 40.sp, modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    Text("🍷", fontSize = 40.sp, modifier = Modifier.align(Alignment.Center))
                }
            }
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {`;

cellarKt = cellarKt.replace(oldWineCardImg, newWineCardImg);
cellarKt = cellarKt.replace('Text(wine.name, color = Color(0xFFd4a54e)', 'Text(wine.name, color = Color(0xFFd4a54e)');
// We need to fix the closing brace for the extra Column
cellarKt = cellarKt.replace('Text("★ ${wine.rating}", color = Color(0xFFd4a54e))\n            }\n        }\n    }\n}', 'Text("★ ${wine.rating}", color = Color(0xFFd4a54e))\n            }\n        }\n    }\n    }\n}');
fs.writeFileSync(path.join(pkgDir, 'ui', 'CellarScreen.kt'), cellarKt);

console.log("Phase 2 Kotlin files created!");
