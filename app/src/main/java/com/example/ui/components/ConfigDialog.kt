package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.OracleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDialog(
    viewModel: OracleViewModel,
    onDismiss: () -> Unit
) {
    val serverUrl by viewModel.serverUrl.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val ttsPitch by viewModel.ttsPitch.collectAsState()
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()
    val useGeminiLocal by viewModel.useGeminiLocal.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Configuración Avanzada", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                
                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = { viewModel.updateServerUrl(it) },
                    label = { Text("Server URL (Socket.IO)", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = geminiApiKey,
                    onValueChange = { viewModel.updateGeminiApiKey(it) },
                    label = { Text("Gemini API Key", color = Color.White) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Switch(checked = useGeminiLocal, onCheckedChange = { viewModel.toggleGeminiLocal(it) })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Usar IA local en todas las rtas", color = Color.White)
                }
                
                Column {
                    Text("TTS Pitch: ${String.format("%.1f", ttsPitch)}", color = Color.White)
                    Slider(
                        value = ttsPitch,
                        onValueChange = { viewModel.updateTtsPitch(it) },
                        valueRange = 0.1f..2.0f
                    )
                }
                
                Column {
                    Text("TTS Speed: ${String.format("%.1f", ttsSpeed)}", color = Color.White)
                    Slider(
                        value = ttsSpeed,
                        onValueChange = { viewModel.updateTtsSpeed(it) },
                        valueRange = 0.1f..2.0f
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cerrar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { 
                        viewModel.connect()
                        onDismiss()
                    }) {
                        Text("Conectar")
                    }
                }
            }
        }
    }
}
