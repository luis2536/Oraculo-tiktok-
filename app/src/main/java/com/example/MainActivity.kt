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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: OracleViewModel = viewModel()
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
    
    var showConfig by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
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
                
                IconButton(onClick = { showConfig = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                }
            }
            
            if (showConfig) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { viewModel.updateServerUrl(it) },
                            label = { Text("Server URL (http://IP:PORT)", color = Color.White) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { showConfig = false }) { Text("Cerrar") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { 
                                viewModel.connect()
                                showConfig = false
                            }) {
                                Text("Conectar")
                            }
                        }
                    }
                }
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

                // Panels (Queue & Ranking - Bottom half)
                Row(modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()) {
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
