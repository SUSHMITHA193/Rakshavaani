package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PipelineStatusCard
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
import com.example.ui.viewmodel.PipelineStatus
import com.example.ui.viewmodel.RakshaVaaniViewModel

@Composable
fun TransmittingScreen(
    viewModel: RakshaVaaniViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val activeAudio by viewModel.activeAudio.collectAsState()
    val pipelineStage by viewModel.pipelineStage.collectAsState()
    val pipelineStatus by viewModel.pipelineStatus.collectAsState()
    val progress by viewModel.transmissionProgress.collectAsState()
    val statusText by viewModel.transmissionStatusText.collectAsState()
    val backendOfflineMessage by viewModel.backendOfflineMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Trigger sending process when entering this screen
    LaunchedEffect(Unit) {
        viewModel.sendToEngine(
            onComplete = {
                onNavigateToResult()
            }
        )
    }

    Scaffold(
        topBar = {
            TopTacticalBar(
                title = if (pipelineStatus == PipelineStatus.BACKEND_OFFLINE) "Backend Offline" else "Transmitting Audio",
                subtitle = "SECURE LOCAL TELEMETRY LINK",
                showBackButton = true,
                onBackClick = {
                    viewModel.cancelTransmission()
                    onNavigateBack()
                },
                showSettingsButton = false
            )
        },
        containerColor = TacticalNavyBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Circular Progress Graphic matching Screen 5 & 9
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .testTag("circular_progress_container"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Background track
                        drawCircle(
                            color = TacticalNavyCardLight,
                            radius = size.minDimension / 2f - 8.dp.toPx(),
                            style = Stroke(width = 8.dp.toPx())
                        )

                        // Progress arc
                        val sweepAngle = progress.coerceIn(0.05f, 1f) * 360f
                        val arcColor = when (pipelineStatus) {
                            PipelineStatus.BACKEND_OFFLINE, PipelineStatus.ERROR -> TacticalRed
                            PipelineStatus.COMPLETE -> TacticalGreen
                            else -> TacticalCyan
                        }

                        drawArc(
                            color = arcColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Center Icon
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(TacticalNavyCard)
                            .border(1.dp, TacticalBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (pipelineStatus) {
                            PipelineStatus.BACKEND_OFFLINE, PipelineStatus.ERROR -> Icons.Default.Warning
                            PipelineStatus.COMPLETE -> Icons.Default.CheckCircle
                            else -> Icons.Default.ArrowUpward
                        }
                        val tint = when (pipelineStatus) {
                            PipelineStatus.BACKEND_OFFLINE, PipelineStatus.ERROR -> TacticalRed
                            PipelineStatus.COMPLETE -> TacticalGreen
                            else -> TacticalCyan
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Status Headline & Description matching Screen 5
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (pipelineStatus) {
                            PipelineStatus.BACKEND_OFFLINE -> "Backend Offline"
                            PipelineStatus.ERROR -> "Processing Error"
                            PipelineStatus.COMPLETE -> "Complete"
                            PipelineStatus.ANALYZING -> "Analyzing Noise..."
                            else -> "Uploading..."
                        },
                        color = TacticalTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (pipelineStatus == PipelineStatus.BACKEND_OFFLINE) {
                            "Python RakshaVaani engine unreachable at ${settings.serverIp}:${settings.serverPort}."
                        } else {
                            "Please keep the app open and stay connected to Wi-Fi."
                        },
                        color = TacticalTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress percentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = TacticalTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(TacticalNavyCardLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(
                                    if (pipelineStatus == PipelineStatus.BACKEND_OFFLINE) TacticalRed
                                    else TacticalCyan
                                )
                        )
                    }
                }
            }

            // Connection Details Card matching Screen 5
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("transmission_details_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("File Size", color = TacticalTextSecondary, fontSize = 13.sp)
                            Text(
                                activeAudio?.fileSizeFormatted ?: "2.4 MB",
                                color = TacticalTextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Connection", color = TacticalTextSecondary, fontSize = 13.sp)
                            Text(
                                "Wi-Fi (${settings.serverIp}:${settings.serverPort})",
                                color = TacticalCyan,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimated Time", color = TacticalTextSecondary, fontSize = 13.sp)
                            Text(
                                "~ 5 seconds",
                                color = TacticalTextPrimary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Pipeline Visualizer
            item {
                PipelineStatusCard(
                    currentStage = pipelineStage,
                    currentStatus = pipelineStatus,
                    progress = progress,
                    statusText = statusText
                )
            }

            // Backend Offline Diagnostic & Options
            if (pipelineStatus == PipelineStatus.BACKEND_OFFLINE) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TacticalNavyCard)
                            .border(1.dp, TacticalRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = TacticalRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "BACKEND OFFLINE",
                                    color = TacticalRed,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "The Python RakshaVaani ML server is not responding at http://${settings.serverIp}:${settings.serverPort}. In DRDO/SIH evaluation mode, you can retry connection or run the local diagnostic pipeline.",
                                color = TacticalTextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.sendToEngine(onComplete = onNavigateToResult)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry Link", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        viewModel.runOfflineDiagnosticEnhancement(onComplete = onNavigateToResult)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TacticalOlive),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1.3f).testTag("run_diagnostic_button")
                                ) {
                                    Text("Run Diagnostic", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Cancel Button matching Screen 5
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.cancelTransmission()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cancel_transmission_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = TacticalTextPrimary,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
