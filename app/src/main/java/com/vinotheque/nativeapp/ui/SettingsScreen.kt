package com.vinotheque.nativeapp.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.vinotheque.nativeapp.ui.theme.TextSecondary
import com.vinotheque.nativeapp.ui.theme.TextTertiary
import com.vinotheque.nativeapp.ui.theme.WineDark
import com.vinotheque.nativeapp.ui.theme.WineGold
import com.vinotheque.nativeapp.ui.theme.WineRed
import com.vinotheque.nativeapp.ui.theme.WineSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(viewModel: WineViewModel, onOpenAdmin: () -> Unit = {}) {
    val context = LocalContext.current
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showAdminLogin by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }
    var adminUser by remember { mutableStateOf("") }
    var adminPass by remember { mutableStateOf("") }
    var adminError by remember { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }

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
            isBusy = true
            scope.launch(Dispatchers.IO) {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    if (json != null) {
                        viewModel.restoreFromJson(json)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Restored!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Restore failed: " + e.message, Toast.LENGTH_SHORT).show()
                    }
                }
                withContext(Dispatchers.Main) { isBusy = false }
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
            }) { Text("Delete All", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } })
    }

    // Switch user dialog
    if (showUserDialog) {
        AlertDialog(onDismissRequest = { showUserDialog = false },
            title = { Text("Switch Profile") },
            text = { OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") }) },
            confirmButton = { TextButton(onClick = { if (newUsername.isNotBlank()) { viewModel.setUser(newUsername); showUserDialog = false }
            }) { Text("Switch", color = WineGold) } },
            dismissButton = { TextButton(onClick = { showUserDialog = false }) { Text("Cancel") } })
    }

    // Admin login dialog
    if (showAdminLogin) {
        AlertDialog(
            onDismissRequest = { showAdminLogin = false; adminError = false },
            title = { Text("Admin Login", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (adminError) {
                        Text("Invalid credentials", color = Color.Red, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(value = adminUser, onValueChange = { adminUser = it; adminError = false },
                        label = { Text("Username") }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = adminPass, onValueChange = { adminPass = it; adminError = false },
                        label = { Text("Password") }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (adminUser == "admin" && adminPass == "admin") {
                        showAdminLogin = false; adminError = false
                        adminUser = ""; adminPass = ""
                        onOpenAdmin()
                    } else { adminError = true }
                }) { Text("Login", color = WineGold, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showAdminLogin = false; adminError = false; adminUser = ""; adminPass = "" }) { Text("Cancel") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(WineDark)
        .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text("Settings", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // Profile
        SettingsCard("Profile", Icons.Default.Person) {
            Text("Signed in as " + currentUser, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsButton("Switch Profile", WineGold) { newUsername = currentUser; showUserDialog = true }
        }

        // Backup (includes images)
        SettingsCard("Backup & Restore", Icons.Default.CloudUpload) {
            Text(wines.size.toString() + " bottles in cellar", color = TextTertiary, fontSize = 13.sp)
            Text("Includes wine photos in backup", color = TextTertiary, fontSize = 11.sp)
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
                Text("\u2705 Auto-backup: $timeText", color = WineGold.copy(alpha = 0.7f), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isBusy) {
                Text("Processing... please wait", color = WineGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsButton("Export JSON", if (isBusy) WineSurface else WineGold, Modifier.weight(1f)) {
                    if (!isBusy) jsonSave.launch("vinotheque_backup.json")
                }
                SettingsButton("Import JSON", WineSurface, Modifier.weight(1f)) {
                    if (!isBusy) jsonRestore.launch("application/json")
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Restore from auto-backup
            SettingsButton("Restore from Auto-Backup", WineSurface) {
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
            Text("Select one or multiple CSV files to import", color = TextTertiary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsButton("Export CSV", WineGold, Modifier.weight(1f)) { csvSave.launch("vinotheque_wines.csv") }
                SettingsButton("Import CSV(s)", WineSurface, Modifier.weight(1f)) { csvImport.launch(arrayOf("text/*", "text/csv", "text/comma-separated-values", "application/csv")) }
            }
        }

        // Sample Data
        SettingsCard("Sample Collection", Icons.Default.LocalBar) {
            SettingsButton("Load 10 Premium Wines", WineGold) {
                viewModel.loadSampleData(); Toast.makeText(context, "Sample wines loaded!", Toast.LENGTH_SHORT).show() }
        }

        // Admin (requires login)
        SettingsCard("Admin Panel", Icons.Default.Lock) {
            Text("Manage all wines, edit fields, add photos", color = TextTertiary, fontSize = 13.sp)
            Text("Requires admin credentials", color = TextTertiary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            SettingsButton("Admin Login", WineGold) { showAdminLogin = true }
        }

        // Danger
        SettingsCard("Danger Zone", Icons.Default.DeleteForever) {
            SettingsButton("Clear All Data", WineRed) { showClearDialog = true }
        }

        // About
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("VINOTHEQUE PRO", color = WineGold, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                Text("Native Android Edition v1.0", color = TextTertiary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Developed by", color = TextTertiary, fontSize = 11.sp)
                Text("Zakariae BOUZIDI-IDRISSI", color = WineGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WineSurface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, title, tint = WineGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsButton(text: String, color: Color, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(text, color = if (color == WineGold) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
