package com.example.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HearingDisabled
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ChatMessageCard
import com.example.ui.components.CyberOrbVisualizer
import com.example.ui.components.GeminiKeyDialog
import com.example.ui.components.OwnerDialog
import com.example.ui.components.QuickControlGrid
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UmangHomeScreen(
    viewModel: UmangViewModel,
    activity: Activity? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll list to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GlassBackground)
    ) {
        // Background Ambient Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Indigo Top Left Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x3D6366F1), Color.Transparent),
                    center = Offset(0f, 0f),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(0f, 0f)
            )
            // Purple Center Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x24A855F7), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.4f),
                    radius = size.width * 0.7f
                ),
                radius = size.width * 0.7f,
                center = Offset(size.width * 0.5f, size.height * 0.4f)
            )
            // Pink Bottom Right Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28EC4899), Color.Transparent),
                    center = Offset(size.width, size.height),
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = Offset(size.width, size.height)
            )
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(IndigoPrimary, PurpleAccent)))
                                    .border(1.dp, Color(0x40FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Umang AI",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "By Umang Rai",
                                    fontSize = 10.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { viewModel.openOwnerDialog() }
                                )
                            }
                        }
                    },
                    actions = {
                        // Wake word toggle
                        IconButton(
                            onClick = { viewModel.toggleWakeWordMode() },
                            modifier = Modifier.testTag("btn_wake_word")
                        ) {
                            Icon(
                                imageVector = if (uiState.isWakeWordActive) Icons.Default.Hearing else Icons.Default.HearingDisabled,
                                contentDescription = "Hands-free Wake Word",
                                tint = if (uiState.isWakeWordActive) EmeraldActive else TextMuted
                            )
                        }

                        // Speech output toggle
                        IconButton(
                            onClick = { viewModel.toggleTts() },
                            modifier = Modifier.testTag("btn_tts_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.ttsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Voice Output Toggle",
                                tint = if (uiState.ttsEnabled) PinkAccent else TextMuted
                            )
                        }

                        // Key dialog toggle
                        IconButton(
                            onClick = { viewModel.openApiKeyDialog() },
                            modifier = Modifier.testTag("btn_key_config")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Gemini API Key",
                                tint = if (uiState.apiKey.isNotBlank()) CyanAccent else Color(0xFFEF4444)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Top Status Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Privacy pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldActive)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PRIVACY SECURE • LOCAL ONLY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = EmeraldActive
                        )
                    }

                    // Engine Active pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x14FFFFFF))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GEMINI ENGINE ACTIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Visualizer & Orb Header Glass Card
                Surface(
                    color = Color(0x1F1E293B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .border(1.dp, Color(0x2BFFFFFF), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        CyberOrbVisualizer(
                            isListening = uiState.isListening,
                            isProcessing = uiState.isProcessing,
                            isSpeaking = uiState.isSpeaking,
                            audioLevel = uiState.rmsAudioLevel,
                            onClick = {
                                if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Umang AI Core",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = "\"Processing your request, Umang Rai...\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = uiState.statusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                uiState.isListening -> CyanAccent
                                uiState.isProcessing -> PurpleAccent
                                uiState.isSpeaking -> PinkAccent
                                else -> IndigoPrimary
                            }
                        )

                        if (uiState.isWakeWordActive) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldActive)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Say 'Hey Umang' anytime to command",
                                    fontSize = 11.sp,
                                    color = EmeraldActive,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Active Command Glass Banner
                val activeMessage = uiState.messages.lastOrNull { it.sender == ChatMessage.Sender.USER }?.text
                if (!activeMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0x14FFFFFF),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ACTIVE COMMAND",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.2.sp,
                                    color = IndigoPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"$activeMessage\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Quick System Controls Grid
                QuickControlGrid(
                    onSendWhatsAppRohit = {
                        viewModel.processSpokenText("Send Rohit message in WhatsApp", activity)
                    },
                    onSplitScreenYoutubeInsta = {
                        viewModel.processSpokenText("Open YouTube and open Instagram", activity)
                    },
                    onBrightnessUp = {
                        viewModel.processSpokenText("Increase brightness", activity)
                    },
                    onVolumeUp = {
                        viewModel.processSpokenText("Increase volume", activity)
                    },
                    onLockPhone = {
                        viewModel.processSpokenText("Lock phone", activity)
                    },
                    onOwnerClick = {
                        viewModel.openOwnerDialog()
                    }
                )

                // Conversation / Action Feed
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { msg ->
                        ChatMessageCard(message = msg)
                    }
                }

                // Bottom Input Bar & Voice Mic Trigger
                Surface(
                    color = Color(0x3308090F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WindowInsets.navigationBars.asPaddingValues())
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Ask Umang AI or type command...", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = Color(0x26FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0x1AFFFFFF),
                                unfocusedContainerColor = Color(0x14FFFFFF)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_command")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (textInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.processSpokenText(textInput, activity)
                                    textInput = ""
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(IndigoPrimary)
                                    .testTag("btn_send_command")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Command",
                                    tint = Color.White
                                )
                            }
                        } else {
                            FloatingActionButton(
                                onClick = {
                                    if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
                                },
                                containerColor = if (uiState.isListening) CyanAccent else IndigoPrimary,
                                contentColor = Color.White,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("fab_mic")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Mic",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialogs
        if (uiState.showApiKeyDialog) {
            GeminiKeyDialog(
                currentKey = uiState.apiKey,
                onSaveKey = { key -> viewModel.setGeminiApiKey(key) },
                onDismiss = { viewModel.closeApiKeyDialog() }
            )
        }

        if (uiState.showOwnerDialog) {
            OwnerDialog(onDismiss = { viewModel.closeOwnerDialog() })
        }
    }
}

