package com.vinotheque.nativeapp.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinotheque.nativeapp.ui.theme.BurgundyRed
import com.vinotheque.nativeapp.ui.theme.WineRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.ui.res.stringResource
import com.vinotheque.nativeapp.R

@Composable
fun SettingsScreen(viewModel: WineViewModel, onOpenAdmin: () -> Unit = {}, onShowPinDialog: () -> Unit = {}, onShowNamePrompt: () -> Unit = {}, onShowShiftSummary: () -> Unit = {}) {
    val context = LocalContext.current
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }
    val progress by viewModel.restoreProgress.collectAsState()

    // All file operations run on Dispatchers.IO to prevent UI thread crash
    val jsonSave = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            isBusy = true
            scope.launch(Dispatchers.IO) {
                try {
                    val json = viewModel.getBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.bufferedWriter().use { writer -> writer.write(json) }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup saved!", Toast.LENGTH_SHORT).show()
                        isBusy = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Backup failed: " + e.message, Toast.LENGTH_SHORT).show()
                        isBusy = false
                    }
                }
            }
        }
    }

    val jsonRestore = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        viewModel.restoreFromJson(json)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val csvSave = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val csv = viewModel.exportCsv()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.bufferedWriter().use { writer -> writer.write(csv) }
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "CSV exported!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val csvImport = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            isBusy = true
            scope.launch(Dispatchers.IO) {
                var count = 0
                for (uri in uris) {
                    try {
                        val c = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                        if (c != null) { viewModel.importCsv(c); count++ }
                    } catch (e: Exception) { /* skip failed file */ }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, count.toString() + " CSV file(s) imported!", Toast.LENGTH_SHORT).show()
                    isBusy = false
                }
            }
        }
    }

    // Clear all confirmation
    if (showClearDialog) {
        AlertDialog(onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Permanently delete all " + wines.size.toString() + " wines?") },
            confirmButton = { TextButton(onClick = { viewModel.clearAll(); showClearDialog = false
                Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
            }) { Text("Delete All", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } })
    }

    // Change PIN dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = { Text("Change Admin PIN") },
            text = {
                OutlinedTextField(value = newPin, onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                    label = { Text("New 4-digit PIN") }, singleLine = true)
            },
            confirmButton = { TextButton(onClick = {
                if (newPin.length == 4) { viewModel.setAdminPin(newPin); showChangePinDialog = false
                    Toast.makeText(context, "PIN changed", Toast.LENGTH_SHORT).show() }
            }) { Text("Save", color = MaterialTheme.colorScheme.primary) } },
            dismissButton = { TextButton(onClick = { showChangePinDialog = false }) { Text("Cancel") } }
        )
    }



    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text(stringResource(R.string.more), color = MaterialTheme.colorScheme.onBackground, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // Profile
        SettingsCard("Profile", Icons.Default.Person) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Current Sommelier", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 12.sp)
                    Text(currentUser, color = MaterialTheme.colorScheme.primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsButton("Change Name", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onShowNamePrompt() }
                SettingsButton(stringResource(R.string.shift_summary), MaterialTheme.colorScheme.surfaceVariant, Modifier.weight(1f)) { onShowShiftSummary() }
            }
        }


        // Language Selector
        val currentLang by viewModel.selectedLanguage.collectAsState()
        SettingsCard("Language / Sprache / Langue", Icons.Default.LocalBar) {
            val languages = listOf("en" to "English", "de" to "Deutsch", "fr" to "Français")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                languages.forEach { (code, label) ->
                    val isSelected = currentLang == code
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.setLanguage(code) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Theme Selector
        val currentTheme by viewModel.selectedTheme.collectAsState()
        SettingsCard("Aesthetics & Theme", Icons.Default.LocalBar) {
            val themes = listOf("Midnight", "Burgundy", "Emerald", "Ocean")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                themes.forEach { theme ->
                    val isSelected = currentTheme == theme
                    val themeColor = when(theme) {
                        "Midnight" -> Color(0xFFD4AF6A)
                        "Burgundy" -> Color(0xFF800020)
                        "Emerald" -> Color(0xFF004D40)
                        "Ocean" -> Color(0xFF0D47A1)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) themeColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewModel.setTheme(theme) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = theme,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Image Sharpness Slider
        val currentQuality by viewModel.imageQuality.collectAsState()
        SettingsCard("Image Quality & Performance", Icons.Default.CameraAlt) {
            Text("Sharpness: ${currentQuality}%", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
            Text("Higher sharpness increases backup size", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Slider(
                value = currentQuality.toFloat(),
                onValueChange = { viewModel.setImageQuality(it.toInt()) },
                valueRange = 10f..100f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // Backup (includes images)
        SettingsCard("Backup & Restore", Icons.Default.CloudUpload) {
            Text(wines.size.toString() + " bottles in cellar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
            Text("Includes wine photos in backup", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
            // Auto-backup status
            val lastBackup = viewModel.getLastBackupTime()
            if (lastBackup > 0) {
                val ago = (System.currentTimeMillis() - lastBackup) / 1000
                val timeText = when {
                    ago < 60 -> "just now"
                    ago < 3600 -> (ago / 60).toString() + "m ago"
                    ago < 86400 -> (ago / 3600).toString() + "h ago"
                    else -> (ago / 86400).toString() + "d ago"
                }
                Text("\u2705 Auto-backup: $timeText", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isBusy) {
                Text("Processing... please wait", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsButton("Export JSON", if (isBusy) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary, Modifier.weight(1f)) {
                    if (!isBusy) jsonSave.launch("vinotheque_backup.json")
                }
                SettingsButton("Import JSON", MaterialTheme.colorScheme.surfaceVariant, Modifier.weight(1f)) {
                    if (!isBusy) jsonRestore.launch("application/json")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Restore from auto-backup
            SettingsButton("Restore from Auto-Backup", MaterialTheme.colorScheme.surfaceVariant) {
                if (!isBusy) {
                    isBusy = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            val file = java.io.File(context.filesDir, "auto_backup/vinotheque_auto.json")
                            if (file.exists()) {
                                val json = file.readText()
                                viewModel.restoreFromJson(json)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Restored from auto-backup!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "No auto-backup found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                        withContext(Dispatchers.Main) { isBusy = false }
                    }
                }
            }
        }

        // CSV
        SettingsCard("CSV Import & Export", Icons.Default.CloudDownload) {
            Text("Select one or multiple CSV files to import", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsButton("Export CSV", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { csvSave.launch("vinotheque_wines.csv") }
                SettingsButton("Import CSV(s)", MaterialTheme.colorScheme.surfaceVariant, Modifier.weight(1f)) { csvImport.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "application/csv")) }
            }
        }

        // Sample Data
        SettingsCard("Sample Collection", Icons.Default.LocalBar) {
            SettingsButton("Load 10 Premium Wines", MaterialTheme.colorScheme.primary) {
                viewModel.loadSampleData(); Toast.makeText(context, "Sample wines loaded!", Toast.LENGTH_SHORT).show() }
        }

        // Admin Panel
        SettingsCard("Admin Panel", Icons.Default.Lock) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Admin Mode", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                Text(if (isAdmin) "Active" else "Inactive", color = if (isAdmin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isAdmin) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsButton("Admin Panel", MaterialTheme.colorScheme.primary, Modifier.weight(1f)) { onOpenAdmin() }
                    SettingsButton("Lock Admin", BurgundyRed, Modifier.weight(1f)) { viewModel.setAdmin(false) }
                }
                Spacer(modifier = Modifier.height(6.6.dp))
                SettingsButton("Change Admin PIN", MaterialTheme.colorScheme.surfaceVariant) { showChangePinDialog = true }
            } else {
                SettingsButton("Unlock Admin", MaterialTheme.colorScheme.primary) { onShowPinDialog() }
            }
        }

        // Danger
        if (isAdmin) {
            SettingsCard("Danger Zone", Icons.Default.DeleteForever) {
                SettingsButton("Clear All Data", BurgundyRed) { showClearDialog = true }
            }
        }

        // About
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVinothequeLogo(modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("VINOTHEQUE PRO", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("Native Android Edition v2.0", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Developed by", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 11.sp)
                Text("Zakariae BOUZIDI-IDRISSI", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }

    // Progress Overlay
    if (progress.isRestoring) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        progress = progress.progress.toFloat() / progress.max.toFloat(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "${progress.progress}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        progress.message,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Do not close the app",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }

    if (progress.isComplete) {
        LaunchedEffect(Unit) {
            Toast.makeText(context, progress.message, Toast.LENGTH_SHORT).show()
            viewModel.resetRestoreState()
        }
    }

    if (progress.error != null) {
        LaunchedEffect(progress.error) {
            Toast.makeText(context, "Error: ${progress.error}", Toast.LENGTH_LONG).show()
            viewModel.resetRestoreState()
        }
    }
}

@Composable
fun SettingsCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsButton(text: String, color: Color, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    val isPrimary = color == MaterialTheme.colorScheme.primary
    Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(text, color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
