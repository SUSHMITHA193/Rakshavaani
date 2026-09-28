package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.InteractiveWaveform
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
import java.io.File
import java.io.FileOutputStream

@Composable
fun HomeScreen(
    viewModel: RakshaVaaniViewModel,
    onNavigateToNoiseSettings: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val context = LocalContext.current
    val activeAudio by viewModel.activeAudio.collectAsState()
    val isRecording by viewModel.recorder.isRecording.collectAsState()
    val recordedDurationSec by viewModel.recorder.recordedDurationSeconds.collectAsState()
    val isPlaying by viewModel.player.isPlaying.collectAsState()
    val playbackProgress by viewModel.player.playbackProgress.collectAsState()
    val currentPositionMs by viewModel.player.currentPositionMs.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val selectedCategory by viewModel.selectedNoiseCategory.collectAsState()

    // File picker launcher for selecting audio from device storage
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val tempFile = File(context.cacheDir, "imported_${System.currentTimeMillis()}.wav")
                FileOutputStream(tempFile).use { output ->
                    inputStream?.copyTo(output)
                }
                viewModel.setActiveAudioFromFile(tempFile, "selected_audio.wav")
                Toast.makeText(context, "Audio file loaded into acoustic pipeline", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load audio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission launcher for microphone recording
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            Toast.makeText(context, "Microphone permission required for field recording", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopTacticalBar(
                title = "RAKSHA VAANI",
                subtitle = "TACTICAL SPEECH ENHANCEMENT",
                showBackButton = false,
                showSettingsButton = true,
                onSettingsClick = onNavigateToSettings,
                isOnline = settings.isOnline
            )
        },
        bottomBar = {
            TacticalBottomBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "history" -> onNavigateToHistory()
                        "settings" -> onNavigateToSettings()
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
                Spacer(modifier = Modifier.height(4.dp))
                // Hero Banner matching reference image screen 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(TacticalNavyCard, TacticalNavyCardLight)
                            )
                        )
                        .border(1.dp, TacticalBorder, RoundedCornerShape(16.dp))
                        .padding(18.dp)
                        .testTag("home_hero_banner")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enhance Speech.",
                                color = TacticalTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Across Any Acoustic Environment.",
                                color = TacticalAmber,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Recover voice signals at up to -20 dB SNR using GTCRN deep learning.",
                                color = TacticalTextMuted,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Graphic waveform visualization bars
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barHeights = listOf(12, 22, 34, 18, 40, 28, 46, 32, 20, 38, 24, 14)
                            barHeights.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(h.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(TacticalOlive)
                                )
                            }
                        }
                    }
                }
            }

            // Primary Action 1: Record Audio
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(
                            1.dp,
                            if (isRecording) TacticalRed else TacticalBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            if (isRecording) {
                                viewModel.stopRecording()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .padding(16.dp)
                        .testTag("record_audio_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isRecording) TacticalRed.copy(alpha = 0.2f) else TacticalOlive.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    if (isRecording) TacticalRed else TacticalOlive,
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Record",
                                tint = if (isRecording) TacticalRed else TacticalGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isRecording) "Recording Voice Input..." else "Record Audio",
                                color = if (isRecording) TacticalRed else TacticalTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isRecording) "Elapsed: ${String.format("%02d:%02d", recordedDurationSec / 60, recordedDurationSec % 60)} (Tap to Stop)" else "Capture your voice at 16 kHz PCM",
                                color = TacticalTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(TacticalRed)
                            )
                        }
                    }
                }
            }

            // Primary Action 2: Select Audio File
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .clickable {
                            audioPickerLauncher.launch("audio/*")
                        }
                        .padding(16.dp)
                        .testTag("select_audio_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TacticalNavyCardLight)
                                .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Select File",
                                tint = TacticalCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Select Audio File",
                                color = TacticalTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose WAV or PCM from device storage",
                                color = TacticalTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Active / Loaded Audio Preview Card
            if (activeAudio != null && !isRecording) {
                item {
                    val audio = activeAudio!!
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(TacticalNavyCard)
                            .border(1.dp, TacticalBorderLight, RoundedCornerShape(14.dp))
                            .padding(16.dp)
                            .testTag("active_audio_card")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "LOADED AUDIO INPUT",
                                        color = TacticalTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = audio.fileName,
                                        color = TacticalTextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.clearActiveAudio() },
                                    modifier = Modifier.size(32.dp).testTag("clear_audio_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = TacticalTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Waveform
                            InteractiveWaveform(
                                amplitudes = audio.amplitudes,
                                progress = playbackProgress,
                                isPlaying = isPlaying,
                                onSeek = { pos -> viewModel.player.seekTo(pos) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Controls and Timing
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.toggleAudioPlayback() },
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(TacticalOlive)
                                            .testTag("play_pause_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Pause" else "Play",
                                            tint = TacticalTextPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    val currSec = currentPositionMs / 1000
                                    val formattedCurr = String.format("%02d:%02d", currSec / 60, currSec % 60)
                                    Text(
                                        text = "$formattedCurr / ${audio.durationFormatted}",
                                        color = TacticalTextSecondary,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TacticalNavyCardLight)
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text("16.0 kHz", color = TacticalCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(TacticalNavyCardLight)
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(audio.fileSizeFormatted, color = TacticalTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action button: Proceed to Noise Configuration & Enhancement
                            Button(
                                onClick = onNavigateToNoiseSettings,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TacticalOlive
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("proceed_noise_settings_button")
                            ) {
                                Text(
                                    text = "SELECT NOISE PROFILE & ENHANCE",
                                    color = TacticalTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = TacticalTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Access Section (Matching reference screen 2)
            item {
                Column {
                    Text(
                        text = "Quick Access Acoustic Profiles",
                        color = TacticalTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickProfileItem(
                            title = "UAV",
                            icon = Icons.Default.Flight,
                            isSelected = selectedCategory == "UAV / Drone",
                            onClick = {
                                viewModel.setNoiseCategory("UAV / Drone")
                                viewModel.loadDemoSample("UAV / Drone")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuickProfileItem(
                            title = "Vehicle",
                            icon = Icons.Default.DirectionsCar,
                            isSelected = selectedCategory == "Vehicle",
                            onClick = {
                                viewModel.setNoiseCategory("Vehicle")
                                viewModel.loadDemoSample("Vehicle")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuickProfileItem(
                            title = "Wind",
                            icon = Icons.Default.Air,
                            isSelected = selectedCategory == "Wind",
                            onClick = {
                                viewModel.setNoiseCategory("Wind")
                                viewModel.loadDemoSample("Wind")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        QuickProfileItem(
                            title = "Gunfire",
                            icon = Icons.Default.GpsFixed,
                            isSelected = selectedCategory == "Gunfire / Impulsive",
                            onClick = {
                                viewModel.setNoiseCategory("Gunfire / Impulsive")
                                viewModel.loadDemoSample("Gunfire / Impulsive")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickProfileItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) TacticalNavyCardLight else TacticalNavyCard)
            .border(
                1.dp,
                if (isSelected) TacticalGreen else TacticalBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp)
            .testTag("quick_profile_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) TacticalGreen else TacticalTextSecondary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = if (isSelected) TacticalTextPrimary else TacticalTextSecondary,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
