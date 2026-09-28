package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveWaveform
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
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.viewmodel.RakshaVaaniViewModel

@Composable
fun PreviewScreen(
    viewModel: RakshaVaaniViewModel,
    onNavigateBack: () -> Unit,
    onSendToEngine: () -> Unit
) {
    val activeAudio by viewModel.activeAudio.collectAsState()
    val isPlaying by viewModel.player.isPlaying.collectAsState()
    val progress by viewModel.player.playbackProgress.collectAsState()
    val currentPositionMs by viewModel.player.currentPositionMs.collectAsState()
    val selectedCategory by viewModel.selectedNoiseCategory.collectAsState()
    val noiseIntensity by viewModel.noiseIntensity.collectAsState()
    val snrShort = viewModel.getSnrShort(noiseIntensity)

    var isPlayingNoiseSample by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopTacticalBar(
                title = "Preview Audio",
                subtitle = "ACOUSTIC ANALYSIS & VERIFICATION",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showSettingsButton = false
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
                // Card 1: Sample Audio (Input Speech + Noise)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                        .testTag("preview_sample_audio_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sample Audio",
                                color = TacticalTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val currSec = currentPositionMs / 1000
                            val durSec = (activeAudio?.durationMs ?: 5000) / 1000
                            Text(
                                text = String.format("%02d:%02d / %02d:%02d", currSec / 60, currSec % 60, durSec / 60, durSec % 60),
                                color = TacticalTextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleAudioPlayback() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TacticalOlive)
                                    .testTag("preview_play_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = TacticalTextPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            InteractiveWaveform(
                                amplitudes = activeAudio?.amplitudes ?: emptyList(),
                                progress = progress,
                                isPlaying = isPlaying,
                                onSeek = { pos -> viewModel.player.seekTo(pos) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Card 2: Noise Profile Preview matching Screen 4
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                        .testTag("preview_noise_profile_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Noise Preview",
                                color = TacticalTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedCategory,
                                color = TacticalAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    isPlayingNoiseSample = !isPlayingNoiseSample
                                    viewModel.toggleAudioPlayback()
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(TacticalNavyCardLight)
                                    .border(1.dp, TacticalBorder, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Noise",
                                    tint = TacticalCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Secondary Waveform for acoustic profile
                            InteractiveWaveform(
                                amplitudes = List(36) { i ->
                                    0.2f + 0.5f * kotlin.math.abs(kotlin.math.sin(i * 0.7f).toFloat())
                                },
                                progress = progress,
                                isPlaying = isPlaying,
                                onSeek = { pos -> viewModel.player.seekTo(pos) },
                                barColor = TacticalAmber.copy(alpha = 0.5f),
                                playedBarColor = TacticalAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Info Callout matching Screen 4
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TacticalNavyCardLight)
                        .border(1.dp, TacticalBorderLight, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TacticalCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "You can listen to the preview before sending it to the engine.",
                            color = TacticalTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Telemetry Specification Chips
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "TRANSMISSION PARAMETERS",
                            color = TacticalTextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sample Rate", color = TacticalTextSecondary, fontSize = 12.sp)
                            Text("16.0 kHz", color = TacticalCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Noise Intensity", color = TacticalTextSecondary, fontSize = 12.sp)
                            Text("$noiseIntensity% ($snrShort)", color = TacticalAmber, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Target ML Architecture", color = TacticalTextSecondary, fontSize = 12.sp)
                            Text("GTCRN (PyTorch/GPU)", color = TacticalGreen, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Send to Engine Button matching Screen 4
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onSendToEngine,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalOlive
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("send_to_engine_button")
                ) {
                    Text(
                        text = "Send to Engine",
                        color = TacticalTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
