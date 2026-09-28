package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TacticalBottomBar
import com.example.ui.components.TopTacticalBar
import com.example.ui.theme.TacticalAmber
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalBorderLight
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalGreen
import com.example.ui.theme.TacticalNavyBg
import com.example.ui.theme.TacticalNavyCard
import com.example.ui.theme.TacticalNavyCardLight
import com.example.ui.theme.TacticalNavyDark
import com.example.ui.theme.TacticalOlive
import com.example.ui.theme.TacticalRed
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.viewmodel.RakshaVaaniViewModel

@Composable
fun SettingsScreen(
    viewModel: RakshaVaaniViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var ipInput by remember(settings.serverIp) { mutableStateOf(settings.serverIp) }
    var portInput by remember(settings.serverPort) { mutableStateOf(settings.serverPort.toString()) }

    Scaffold(
        topBar = {
            TopTacticalBar(
                title = "Tactical Settings",
                subtitle = "NETWORK & PIPELINE CONFIGURATION",
                showBackButton = false,
                showSettingsButton = false
            )
        },
        bottomBar = {
            TacticalBottomBar(
                currentRoute = "settings",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateHome()
                        "history" -> onNavigateToHistory()
                    }
                }
            )
        },
        containerColor = TacticalNavyBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // Section 1: Connection matching Screen 8
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                        .testTag("settings_connection_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Connection",
                                color = TacticalTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (settings.isOnline) TacticalGreen.copy(alpha = 0.15f) else TacticalRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (settings.isOnline) "ONLINE" else "OFFLINE",
                                    color = if (settings.isOnline) TacticalGreen else TacticalRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Server IP Input
                        Text("Server IP Address", color = TacticalTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = {
                                ipInput = it
                                viewModel.updateServerIp(it)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("server_ip_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TacticalTextPrimary,
                                unfocusedTextColor = TacticalTextPrimary,
                                focusedBorderColor = TacticalOlive,
                                unfocusedBorderColor = TacticalBorder,
                                focusedContainerColor = TacticalNavyDark,
                                unfocusedContainerColor = TacticalNavyDark
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Server Port Input
                        Text("Server Port", color = TacticalTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = {
                                portInput = it
                                it.toIntOrNull()?.let { p -> viewModel.updateServerPort(p) }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("server_port_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TacticalTextPrimary,
                                unfocusedTextColor = TacticalTextPrimary,
                                focusedBorderColor = TacticalOlive,
                                unfocusedBorderColor = TacticalBorder,
                                focusedContainerColor = TacticalNavyDark,
                                unfocusedContainerColor = TacticalNavyDark
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.checkBackendHealth() },
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalOlive),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("test_connection_button")
                        ) {
                            if (settings.isCheckingConnection) {
                                CircularProgressIndicator(
                                    color = TacticalTextPrimary,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pinging Backend...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ping Python Engine / Test Link", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (settings.lastPingMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = settings.lastPingMessage!!,
                                color = if (settings.isOnline) TacticalGreen else TacticalAmber,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Section 2: Audio Configuration matching Screen 8
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Audio Pipeline",
                            color = TacticalTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Sample Rate", color = TacticalTextPrimary, fontSize = 13.sp)
                                Text("GTCRN Defence Acoustic Standard", color = TacticalTextMuted, fontSize = 11.sp)
                            }
                            Text("16 kHz (Mono)", color = TacticalCyan, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Audio Quality", color = TacticalTextPrimary, fontSize = 13.sp)
                                Text("Linear PCM encoding", color = TacticalTextMuted, fontSize = 11.sp)
                            }
                            Text("High (16-bit)", color = TacticalTextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Auto Play", color = TacticalTextPrimary, fontSize = 13.sp)
                                Text("Play enhanced audio on receipt", color = TacticalTextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = settings.autoPlay,
                                onCheckedChange = { viewModel.toggleAutoPlay(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TacticalTextPrimary,
                                    checkedTrackColor = TacticalOlive,
                                    uncheckedThumbColor = TacticalTextMuted,
                                    uncheckedTrackColor = TacticalNavyDark
                                )
                            )
                        }
                    }
                }
            }

            // Section 3: Processing Mode matching Screen 8
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Processing Mode",
                            color = TacticalTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Remote Prototype Mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TacticalNavyCardLight)
                                .border(1.dp, TacticalGreen, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = TacticalGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Remote Prototype", color = TacticalTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Python Laptop Backend (RTX GPU Acceleration)", color = TacticalTextSecondary, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // On-Device Inference Mode (Planned)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TacticalNavyDark)
                                .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(TacticalNavyCardLight)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("On-Device Inference", color = TacticalTextMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TacticalAmber.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("PLANNED / ONNX MOBILE", color = TacticalAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text("Roadmap milestone for self-contained battlefield radio handsets", color = TacticalTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Section 4: System Information
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("System Identity", color = TacticalTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("RakshaVaani Tactical Speech Enhancement System", color = TacticalTextSecondary, fontSize = 12.sp)
                        Text("Smart India Hackathon (SIH) / DRDO Defense Acoustic Prototype", color = TacticalTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.loadDemoSample("UAV / Drone")
                                Toast.makeText(context, "Tactical demo acoustic test pack reloaded", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TacticalNavyCardLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reload Tactical Audio Demo Pack", color = TacticalCyan, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
