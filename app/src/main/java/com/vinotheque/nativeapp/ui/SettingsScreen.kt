package com.vinotheque.nativeapp.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Gold = Color(0xFFD4A54E)
private val DarkBg = Color(0xFF0D0505)
private val CardBg = Color(0xFF1A0A0A)

@Composable
fun SettingsScreen(viewModel: WineViewModel) {
    val context = LocalContext.current
    val wines by viewModel.allWinesUnfiltered.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var newUsername by remember { mutableStateOf("") }

    val jsonSaveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try { context.contentResolver.openOutputStream(uri)?.use { it.write(viewModel.getBackupJson().toByteArray()) }
                Toast.makeText(context, "JSON backup saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show() }
        }
    }
    val jsonRestoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try { val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (json != null) { viewModel.restoreFromJson(json); Toast.makeText(context, "JSON restored!", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) { Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show() }
        }
    }
    val csvSaveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            try { context.contentResolver.openOutputStream(uri)?.use { it.write(viewModel.exportCsv().toByteArray()) }
                Toast.makeText(context, "CSV exported!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show() }
        }
    }
    val csvImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try { val csv = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (csv != null) { viewModel.importCsv(csv); Toast.makeText(context, "CSV imported!", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) { Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show() }
        }
    }

    if (showClearDialog) {
        AlertDialog(onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Delete ALL " + wines.size.toString() + " wines? This cannot be undone.") },
            confirmButton = { TextButton(onClick = {
                viewModel.clearAll(); showClearDialog = false
                Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
            }) { Text("Delete All", color = Color.Red) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } })
    }
    if (showUserDialog) {
        AlertDialog(onDismissRequest = { showUserDialog = false },
            title = { Text("Switch User") },
            text = { OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") }) },
            confirmButton = { TextButton(onClick = {
                if (newUsername.isNotBlank()) { viewModel.setUser(newUsername); showUserDialog = false }
            }) { Text("Switch", color = Gold) } },
            dismissButton = { TextButton(onClick = { showUserDialog = false }) { Text("Cancel") } })
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text("Settings", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // User profile
        SettingsCard("User Profile") {
            Text("Current user: " + currentUser, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { newUsername = currentUser; showUserDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                Text("Switch User", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // JSON Backup
        SettingsCard("JSON Backup & Restore") {
            Text(wines.size.toString() + " bottles in cellar", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { jsonSaveLauncher.launch("vinotheque_backup.json") },
                    modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                    Text("Export", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(onClick = { jsonRestoreLauncher.launch("application/json") },
                    modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                    Text("Import", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // CSV
        SettingsCard("CSV Import & Export") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { csvSaveLauncher.launch("vinotheque_wines.csv") },
                    modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                    Text("Export CSV", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Button(onClick = { csvImportLauncher.launch("text/*") },
                    modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                    Text("Import CSV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Sample data
        SettingsCard("Sample Data") {
            Button(onClick = { viewModel.loadSampleData()
                Toast.makeText(context, "10 sample wines loaded!", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                Text("Load Sample Collection", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Danger zone
        SettingsCard("Danger Zone") {
            Button(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF661111))) {
                Text("Clear All Data", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        SettingsCard("About") {
            Text("Vinotheque Pro", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Native Android Edition v1.0", color = Color.Gray, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
