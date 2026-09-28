package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioEnhancementRecord
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
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.ui.viewmodel.RakshaVaaniViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: RakshaVaaniViewModel,
    onNavigateHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToResult: () -> Unit
) {
    val records by viewModel.historyRecords.collectAsState()
    val currentFilter by viewModel.historyFilter.collectAsState()

    val filteredRecords = when (currentFilter) {
        "Recorded" -> records.filter { it.status == "RECORDED" }
        "Processed" -> records.filter { it.status == "PROCESSED" }
        else -> records
    }

    Scaffold(
        topBar = {
            TopTacticalBar(
                title = "History Log",
                subtitle = "TACTICAL ACOUSTIC RECORDS",
                showBackButton = false,
                showSettingsButton = true,
                onSettingsClick = onNavigateToSettings
            )
        },
        bottomBar = {
            TacticalBottomBar(
                currentRoute = "history",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateHome()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        },
        containerColor = TacticalNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            // Filter Chips matching Screen 7: [All] [Recorded] [Processed]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChipItem(
                    label = "All",
                    isSelected = currentFilter == "All",
                    onClick = { viewModel.setHistoryFilter("All") },
                    modifier = Modifier.weight(1f)
                )

                FilterChipItem(
                    label = "Recorded",
                    isSelected = currentFilter == "Recorded",
                    onClick = { viewModel.setHistoryFilter("Recorded") },
                    modifier = Modifier.weight(1f)
                )

                FilterChipItem(
                    label = "Processed",
                    isSelected = currentFilter == "Processed",
                    onClick = { viewModel.setHistoryFilter("Processed") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Record Items List
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TacticalTextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Tactical Recordings Logged",
                            color = TacticalTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Audio captures and enhancement results will appear here.",
                            color = TacticalTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        HistoryRecordCard(
                            record = record,
                            onPlayClick = {
                                val targetPath = if (record.status == "PROCESSED" && !record.enhancedFilePath.isNullOrEmpty()) {
                                    record.enhancedFilePath
                                } else {
                                    record.originalFilePath
                                }
                                val file = File(targetPath)
                                if (file.exists()) {
                                    viewModel.setActiveAudioFromFile(file, record.fileName)
                                    if (record.status == "PROCESSED") {
                                        onNavigateToResult()
                                    } else {
                                        onNavigateHome()
                                    }
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) TacticalOlive else TacticalNavyCard)
            .border(1.dp, if (isSelected) TacticalOlive else TacticalBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) TacticalTextPrimary else TacticalTextMuted,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun HistoryRecordCard(
    record: AudioEnhancementRecord,
    onPlayClick: () -> Unit
) {
    val icon: ImageVector = when {
        record.noiseCategory.contains("uav", ignoreCase = true) -> Icons.Default.Flight
        record.noiseCategory.contains("vehicle", ignoreCase = true) -> Icons.Default.DirectionsCar
        record.noiseCategory.contains("wind", ignoreCase = true) -> Icons.Default.Air
        record.noiseCategory.contains("gunfire", ignoreCase = true) -> Icons.Default.GpsFixed
        else -> Icons.Default.GraphicEq
    }

    val dateFormatted = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    val durSec = record.durationMs / 1000
    val durFormatted = String.format("%02d:%02d", durSec / 60, durSec % 60)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TacticalNavyCard)
            .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onPlayClick)
            .padding(14.dp)
            .testTag("history_record_${record.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TacticalNavyCardLight)
                    .border(1.dp, TacticalBorder, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (record.status == "PROCESSED") TacticalGreen else TacticalCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.fileName,
                        color = TacticalTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (record.status == "PROCESSED") TacticalGreen.copy(alpha = 0.15f)
                                else TacticalCyan.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (record.status == "PROCESSED") "Enhanced" else "Recorded",
                            color = if (record.status == "PROCESSED") TacticalGreen else TacticalCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$dateFormatted • ${record.fileSizeFormatted}",
                        color = TacticalTextMuted,
                        fontSize = 11.sp
                    )

                    Text(
                        text = durFormatted,
                        color = TacticalTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (record.status == "PROCESSED" && record.snrImprovement != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SNR Gain: +${record.snrImprovement} dB | SI-SNR: +${record.siSnr} dB",
                        color = TacticalGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
