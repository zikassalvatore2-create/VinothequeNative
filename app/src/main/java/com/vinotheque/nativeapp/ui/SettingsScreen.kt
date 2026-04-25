package com.vinotheque.nativeapp.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    var showClearDialog by remember { mutableStateOf(false) }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val json = viewModel.getBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                Toast.makeText(context, "Backup saved!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                if (json != null) {
                    viewModel.restoreFromJson(json)
                    Toast.makeText(context, "Restore complete!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Clear all dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete ALL " + wines.size.toString() + " wines. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                    Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                }) { Text("Delete All", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
            .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Settings", color = Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // Backup card
        SettingsCard(title = "Backup & Restore") {
            Text(wines.size.toString() + " bottles in cellar", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { saveLauncher.launch("vinotheque_backup.json") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                Text("Export Backup (JSON)", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { restoreLauncher.launch("application/json") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333))) {
                Text("Restore from File", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Sample Data card
        SettingsCard(title = "Sample Data") {
            Text("Load 10 curated premium wines", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                viewModel.loadSampleData()
                Toast.makeText(context, "10 sample wines loaded!", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Gold)) {
                Text("Load Sample Collection", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Danger Zone
        SettingsCard(title = "Danger Zone") {
            Button(onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF661111))) {
                Text("Clear All Data", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // About card
        SettingsCard(title = "About") {
            Text("Vinotheque Pro", color = Gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Native Android Edition v1.0", color = Color.Gray, fontSize = 14.sp)
            Text("100% Kotlin + Jetpack Compose", color = Color.Gray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, Gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
