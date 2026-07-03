package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.ParticleBackground
import com.example.ui.components.ConfigDialog
import com.example.ui.components.ManualAskDialog
import com.example.ui.components.HolographicRings
import com.example.ui.components.AudioWaveform
import com.example.ui.components.MysticOracleVisualizer
import com.example.ui.components.FlyingTarotCardDisplay
import androidx.compose.ui.draw.blur

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = PreferencesManager(this)
        val geminiService = GeminiService()
        val factory = viewModelFactory {
            initializer {
                OracleViewModel(this@MainActivity.application)
            }
        }
        setContent {
            MyApplicationTheme {
                val viewModel: OracleViewModel = viewModel(factory = factory)
                LaunchedEffect(Unit) {
                    viewModel.initTts(this@MainActivity)
                }
                OracleApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OracleApp(viewModel: OracleViewModel) {
    val isConnected by viewModel.isConnected.collectAsState()
    val isConnecting by viewModel.isConnecting.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    
    val queue by viewModel.queue.collectAsState()
    val contributors by viewModel.contributors.collectAsState()
    val currentResponse by viewModel.currentResponse.collectAsState()
    val isOracleTalking by viewModel.isOracleTalking.collectAsState()
    val useGeminiLocal by viewModel.useGeminiLocal.collectAsState()
    val history by viewModel.history.collectAsState()
    val errorFlow by viewModel.errorFlow.collectAsState()
    
    val currentTarotCard by viewModel.currentTarotCard.collectAsState()
    val isTarotFlipped by viewModel.isTarotFlipped.collectAsState()
    
    var showConfig by remember { mutableStateOf(false) }
    var showManualAsk by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(errorFlow) {
        errorFlow?.let { errorMsg ->
            snackbarHostState.showSnackbar(errorMsg, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated Particle Background
            ParticleBackground()
            
            // Subtle overall overlay
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header (Glassmorphism style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusColor = if (isConnected) Color(0xFF00E676) else if (isConnecting) Color(0xFFFFEA00) else Color(0xFFFF1744)
                        Box(modifier = Modifier
                            .size(14.dp)
                            .shadow(8.dp, CircleShape, ambientColor = statusColor, spotColor = statusColor)
                            .clip(CircleShape)
                            .background(statusColor))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isConnected) "Conectado" else if (isConnecting) "Conectando..." else "Desconectado", 
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { showHistory = !showHistory },
                            modifier = Modifier.background(if (showHistory) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent, CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.List, contentDescription = "Historial", tint = Color.White)
                        }
                        IconButton(onClick = { showManualAsk = true }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Preguntar", tint = Color.White)
                        }
                        IconButton(onClick = { showConfig = true }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Configuración", tint = Color.White)
                        }
                    }
                }
                
                val tiktokUser by viewModel.tiktokUser.collectAsState()
                if (isConnected) {
                    com.example.ui.components.TikTokScraperWebView(
                        username = tiktokUser,
                        onComment = { user, text -> viewModel.onNewCommentReceived(user, text) },
                        onLog = { log -> viewModel.addLog(log) }
                    )
                }

                if (showConfig) {
                    ConfigDialog(viewModel, onDismiss = { showConfig = false })
                }
                
                if (showManualAsk) {
                    ManualAskDialog(viewModel, onDismiss = { showManualAsk = false })
                }
            
            // Collect connection logs for real-time live chat parsing
            val connectionLogs by viewModel.connectionLogs.collectAsState()
            
            // Redesigned Top Banner for Live Streaming
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f)) // Gold border
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✦ ESCRIBE TU NOMBRE + PREGUNTA ✦",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Lecturas sagradas en vivo por orden de llegada",
                        color = Color(0xFFD4AF37), // Pure gold/amber
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        letterSpacing = 0.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Center (Oracle - Beautifully sized to fit on phones without squishing)
            Box(modifier = Modifier
                .height(190.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "oracle")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isOracleTalking) 1.12f else 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(if (isOracleTalking) 500 else 4000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ), label = "scale"
                )

                MysticOracleVisualizer(
                    isOracleTalking = isOracleTalking,
                    scale = scale,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Split Panels: Left: Turnos (Queue), Right: Chat en Vivo (Real-time Comments)
            Row(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Panel: TURNOS (Fila de espera)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "✦ TURNOS ✦",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        if (queue.isEmpty() && currentResponse == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cola vacía\nEsperando preguntas...",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // 1. Active reading highlight
                                currentResponse?.let { active ->
                                    item {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFD81B60).copy(alpha = 0.25f)),
                                            border = BorderStroke(1.dp, Color(0xFFFF4081)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(10.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${active.first} leyendo...",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // 2. Queue items
                                itemsIndexed(queue) { index, item ->
                                    val isReading = currentResponse?.first == item.name
                                    if (!isReading) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.White.copy(alpha = 0.05f))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${index + 1}.",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                modifier = Modifier.width(18.dp)
                                            )
                                            Text(
                                                text = item.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Right Panel: CHAT EN VIVO (Live Comment Scraper Visual Feed)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = "💬 CHAT EN VIVO",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        // Dynamically extract real comments from connection logs
                        val liveComments = remember(connectionLogs) {
                            connectionLogs
                                .filter { it.contains("✔ COMENTARIO LEÍDO -> ") }
                                .map { log ->
                                    val logBody = log.substringAfter("✔ COMENTARIO LEÍDO -> @")
                                    val parts = logBody.split(": ", limit = 2)
                                    val user = parts.getOrNull(0) ?: "Anónimo"
                                    val comment = parts.getOrNull(1) ?: ""
                                    user to comment
                                }
                                .take(6) // Take top 6 latest
                        }
                        
                        if (liveComments.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Esperando comentarios\ndesde TikTok Live...",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(liveComments) { chat ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.05f))
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "@${chat.first}",
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = chat.second,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bottom Response Card ("Pancarta" layout, perfectly suited for vertical streams)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161622).copy(alpha = 0.85f)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, Color(0xFFD4AF37).copy(alpha = 0.4f)) // Glowing Gold border
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    if (currentResponse != null) {
                        // User Name Title Plate
                        Text(
                            text = "🔮 LECTURA PARA: ${currentResponse!!.first.uppercase()}",
                            color = Color(0xFFD4AF37), // Bright Gold
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "El oráculo revela tu destino...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Audio waveform sync
                        AudioWaveform(
                            isPlaying = isOracleTalking,
                            modifier = Modifier.height(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Prophecy Text
                        Text(
                            text = "\"${currentResponse!!.second}\"",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        // Idle Listening Mode
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            AudioWaveform(
                                isPlaying = false,
                                modifier = Modifier.height(18.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(11.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ORÁCULO CANALIZANDO ENERGÍAS...",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3D Flying Tarot Card Reveal Overlay
            FlyingTarotCardDisplay(
                card = currentTarotCard,
                isFlipped = isTarotFlipped,
                onDismiss = { viewModel.dismissTarotCard() }
            )
        }
    }
}
}
