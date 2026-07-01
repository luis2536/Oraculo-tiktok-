package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.components.ParticleBackground
import com.example.ui.components.ConfigDialog
import com.example.ui.components.ManualAskDialog
import androidx.compose.ui.draw.blur
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Add

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
    
    var showConfig by remember { mutableStateOf(false) }
    var showManualAsk by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated Particle Background
            ParticleBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (isConnected) Color.Green else Color.Red))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isConnected) "Conectado" else if (isConnecting) "Conectando..." else "Desconectado", color = Color.White)
                    }
                    
                    Row {
                        IconButton(onClick = { showHistory = !showHistory }) {
                            Icon(Icons.Default.List, contentDescription = "Historial", tint = Color.White)
                        }
                        IconButton(onClick = { showManualAsk = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Preguntar", tint = Color.White)
                        }
                        IconButton(onClick = { showConfig = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
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
            Column(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {
                
                // Center (Oracle - Top half)
                Box(modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val infiniteTransition = rememberInfiniteTransition(label = "oracle")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = if (isOracleTalking) 1.15f else 1.03f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(if (isOracleTalking) 300 else 2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "scale"
                    )
                    val offsetY by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = if (isOracleTalking) 0f else -15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ), label = "offsetY"
                    )
                    
                    Image(
                        painter = painterResource(R.drawable.img_oracle_character),
                        contentDescription = "Oracle",
                        modifier = Modifier
                            .fillMaxHeight(0.9f)
                            .aspectRatio(1f)
                            .offset(y = offsetY.dp)
                            .scale(scale)
                            .clip(RoundedCornerShape(50)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Panels (Queue, History & Ranking - Bottom half)
                Row(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
                    
                    if (showHistory) {
                        // History Panel
                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)) {
                            Text("HISTORIAL", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn {
                                itemsIndexed(history) { index, item ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(item.first, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                                            Text(item.second, color = Color.White, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Left Panel (Queue)
                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)) {
                            Text("TURNOS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn {
                                itemsIndexed(queue) { index, item ->
                                    Text("${index + 1}. ${item.name}", color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                        }
                    }
                    
                    // Right Panel (Ranking)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)) {
                        Text("RANKING", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn {
                            itemsIndexed(contributors) { index, item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${index + 1}. ${item.name}", color = Color.White, maxLines = 1)
                                        Text(item.score.toString(), color = MaterialTheme.colorScheme.tertiary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Bottom Response
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    if (currentResponse != null) {
                        Text(
                            text = currentResponse!!.first.uppercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentResponse!!.second,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text("Esperando pregunta...", color = Color.Gray)
                    }
                }
            }
        }
    }
}
}
