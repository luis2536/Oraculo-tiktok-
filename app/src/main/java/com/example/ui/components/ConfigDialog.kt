package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.OracleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDialog(
    viewModel: OracleViewModel,
    onDismiss: () -> Unit
) {
    val tiktokUser by viewModel.tiktokUser.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val systemPrompt by viewModel.systemPrompt.collectAsState()
    val ttsPitch by viewModel.ttsPitch.collectAsState()
    val ttsSpeed by viewModel.ttsSpeed.collectAsState()
    val connectionLogs by viewModel.connectionLogs.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(24.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF14142B) // Deep space background
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
            ) {
                Text(
                    text = "CENTRAL DE DIAGNÓSTICO",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Configuración Cyber-Oráculo",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Diagnostics status board
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DiagnosticStep(
                                label = "1. INICIALIZAR MOTOR NATIVO",
                                status = "DISPONIBLE",
                                color = Color(0xFF00B0FF)
                            )
                            DiagnosticStep(
                                label = "2. ENLACE WEBVIEW SCRAPER",
                                status = if (isConnected) "ESTABLECIDO" else if (isConnecting) "NEGOCIANDO" else "INACTIVO",
                                color = if (isConnected) Color(0xFF00E676) else if (isConnecting) Color(0xFFFFEA00) else Color.Gray
                            )
                            DiagnosticStep(
                                label = "3. ESCUCHA DE CANAL TIKTOK (@$tiktokUser)",
                                status = if (isConnected) "SINCRONIZADO" else "REQUERIDA",
                                color = if (isConnected) Color(0xFF00E676) else Color.Gray
                            )
                        }
                    }

                    Text("PARÁMETROS DEL SISTEMA", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = tiktokUser,
                            onValueChange = { viewModel.updateTiktokUser(it) },
                            label = { Text("Usuario de TikTok Live", color = Color.White.copy(alpha = 0.7f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                            ),
                            placeholder = { Text("Ejemplo: mi_usuario", color = Color.White.copy(alpha = 0.3f)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        val cleanUser = tiktokUser.replace("@", "").trim()
                        val previewUrl = "tiktok.com/@$cleanUser/live"
                        Text(
                            text = "🔗 Enlace de Escucha: $previewUrl",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                        Text(
                            text = "Nota: Ingresa tu nombre de usuario de TikTok sin símbolos ni espacios.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { viewModel.updateGeminiApiKey(it) },
                        label = { Text("Gemini AI API Key (v1beta)", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        placeholder = { Text("Pega tu API Key de Google AI Studio", color = Color.White.copy(alpha = 0.3f)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = systemPrompt,
                        onValueChange = { viewModel.updateSystemPrompt(it) },
                        label = { Text("System Prompt (Personalidad de Oráculo)", color = Color.White.copy(alpha = 0.7f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3, maxLines = 5
                    )
                    
                    // TTS settings
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.02f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text("Tono de Voz", color = Color.White, fontSize = 12.sp)
                        Slider(value = ttsPitch, onValueChange = { viewModel.updateTtsPitch(it) }, valueRange = 0.3f..1.8f)
                        
                        Text("Velocidad", color = Color.White, fontSize = 12.sp)
                        Slider(value = ttsSpeed, onValueChange = { viewModel.updateTtsSpeed(it) }, valueRange = 0.3f..1.8f)
                    }
                    
                    // Logs Monitor Area
                    Text("VISOR DE CONEXIÓN (LOGS)", color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(8.dp)
                    ) {
                        androidx.compose.foundation.lazy.LazyColumn(reverseLayout = true) {
                            items(connectionLogs.size) { index ->
                                Text(
                                    text = connectionLogs[index],
                                    color = if (connectionLogs[index].contains("Error")) Color(0xFFFF5252) else Color(0xFF00E676),
                                    fontSize = 9.sp, fontFamily = FontFamily.Monospace, lineHeight = 12.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cerrar") }
                    Button(onClick = { viewModel.connect() }, modifier = Modifier.weight(1f)) { Text("CONECTAR") }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticStep(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(status, color = color, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black)
    }
}
