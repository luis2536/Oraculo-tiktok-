package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
                OracleViewModel(prefs, geminiService)
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
                
                if (showConfig) {
                    ConfigDialog(viewModel, onDismiss = { showConfig = false })
                }
                
                if (showManualAsk) {
                    ManualAskDialog(viewModel, onDismiss = { showManualAsk = false })
                }
            
            // Main layout
            // Center (Oracle - Top half with stable size to prevent overlapping and squishing)
            Box(modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                val infiniteTransition = rememberInfiniteTransition(label = "oracle")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isOracleTalking) 1.15f else 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(if (isOracleTalking) 400 else 3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "scale"
                )
                val offsetY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = if (isOracleTalking) 0f else -6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ), label = "offsetY"
                )

                MysticOracleVisualizer(
                    isOracleTalking = isOracleTalking,
                    scale = scale,
                    offsetY = offsetY,
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Panels (Queue, History & Ranking - Middle half, automatically occupies remaining space)
            Row(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                AnimatedVisibility(
                    visible = showHistory,
                    modifier = Modifier.weight(1f),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    // History Panel
                    Column {
                        Text("HISTORIAL", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(history) { index, item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(item.first, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(item.second, color = Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
                
                AnimatedVisibility(
                    visible = !showHistory,
                    modifier = Modifier.weight(1f),
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    // Left Panel (Queue)
                    Column {
                        Text("TURNOS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(queue) { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${index + 1}.", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                
                // Right Panel (Ranking)
                Column(modifier = Modifier.weight(1f)) {
                    Text("RANKING", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(contributors) { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text("${index + 1}.", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
                                    Text(item.name, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text(item.score.toString(), color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Bottom Response ("Pancarta" layout, beautifully integrated vertically to prevent overlaps)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1E1E2E).copy(alpha = 0.75f),
                                Color(0xFF1E1E2E).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth()
                ) {
                    if (currentResponse != null) {
                        Text(
                            text = currentResponse!!.first.uppercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Dynamic audio / voice wave visualizer
                        AudioWaveform(
                            isPlaying = isOracleTalking,
                            modifier = Modifier.height(28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "\"${currentResponse!!.second}\"",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 24.sp
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Low-intensity idle signal waveform
                            AudioWaveform(
                                isPlaying = false,
                                modifier = Modifier.height(20.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ENLAZANDO CON EL NÚCLEO NEURONAL...",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.labelSmall,
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
