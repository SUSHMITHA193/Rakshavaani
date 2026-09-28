package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.PipelineStage
import com.example.ui.viewmodel.PipelineStatus

@Composable
fun TopTacticalBar(
    title: String = "RAKSHA VAANI",
    subtitle: String? = null,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    showSettingsButton: Boolean = true,
    onSettingsClick: () -> Unit = {},
    isOnline: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("top_tactical_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(40.dp).testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TacticalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TacticalNavyCardLight)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = "DRDO Emblem",
                        tint = TacticalAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Text(
                    text = title,
                    color = TacticalTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = TacticalTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) TacticalGreen else TacticalRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOnline) "PY-GTCRN ENGINE ONLINE" else "PY-ENGINE STANDBY",
                            color = if (isOnline) TacticalGreen else TacticalTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }

        if (showSettingsButton) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TacticalNavyCard)
                    .border(1.dp, TacticalBorder, CircleShape)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TacticalTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TacticalBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = TacticalNavyDark,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = TacticalBorder.copy(alpha = 0.5f))
            .testTag("tactical_bottom_bar")
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TacticalTextPrimary,
                selectedTextColor = TacticalTextPrimary,
                indicatorColor = TacticalOlive,
                unselectedIconColor = TacticalTextMuted,
                unselectedTextColor = TacticalTextMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentRoute == "history",
            onClick = { onNavigate("history") },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History"
                )
            },
            label = { Text("History", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TacticalTextPrimary,
                selectedTextColor = TacticalTextPrimary,
                indicatorColor = TacticalOlive,
                unselectedIconColor = TacticalTextMuted,
                unselectedTextColor = TacticalTextMuted
            ),
            modifier = Modifier.testTag("nav_history")
        )

        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings", fontSize = 12.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TacticalTextPrimary,
                selectedTextColor = TacticalTextPrimary,
                indicatorColor = TacticalOlive,
                unselectedIconColor = TacticalTextMuted,
                unselectedTextColor = TacticalTextMuted
            ),
            modifier = Modifier.testTag("nav_settings")
        )
    }
}

@Composable
fun InteractiveWaveform(
    amplitudes: List<Float>,
    progress: Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    barColor: Color = TacticalCyan,
    playedBarColor: Color = TacticalGreen
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(TacticalNavyDark)
            .border(1.dp, TacticalBorder, RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(frac)
                }
            }
            .testTag("waveform_visualizer")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bars = if (amplitudes.isNotEmpty()) amplitudes else List(40) { 0.35f }
            val barCount = bars.size
            val barWidth = (size.width / (barCount * 1.6f)).coerceAtLeast(3f)
            val spacing = (size.width - (barCount * barWidth)) / (barCount - 1).coerceAtLeast(1)
            val centerY = size.height / 2f

            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing)
                val amp = bars[i].coerceIn(0.1f, 1.0f)
                val barHeight = (size.height * 0.82f * amp)
                val top = centerY - barHeight / 2f
                val barProgress = i.toFloat() / barCount

                val color = if (barProgress <= progress) {
                    playedBarColor.copy(alpha = if (isPlaying) pulseAlpha else 0.95f)
                } else {
                    barColor.copy(alpha = 0.35f)
                }

                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            // Draw Playhead scrubber line
            val playheadX = size.width * progress.coerceIn(0f, 1f)
            drawLine(
                color = TacticalAmber,
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun NoiseCategoryCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) TacticalNavyCardLight else TacticalNavyCard)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) TacticalGreen else TacticalBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("noise_card_${title.replace(" ", "_").lowercase()}")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) TacticalGreen else TacticalTextSecondary,
                    modifier = Modifier.size(32.dp)
                )

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(TacticalGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = TacticalNavyDark,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = if (isSelected) TacticalTextPrimary else TacticalTextSecondary,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun PipelineStatusCard(
    currentStage: PipelineStage,
    currentStatus: PipelineStatus,
    progress: Float,
    statusText: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TacticalNavyCard)
            .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .testTag("pipeline_status_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROCESSING PIPELINE",
                    color = TacticalTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                val (badgeBg, badgeText, badgeColor) = when (currentStatus) {
                    PipelineStatus.READY -> Triple(TacticalNavyCardLight, "READY", TacticalCyan)
                    PipelineStatus.ANALYZING -> Triple(TacticalAmber.copy(alpha = 0.2f), "ANALYZING", TacticalAmber)
                    PipelineStatus.PROCESSING -> Triple(TacticalCyan.copy(alpha = 0.2f), "PROCESSING", TacticalCyan)
                    PipelineStatus.COMPLETE -> Triple(TacticalGreen.copy(alpha = 0.2f), "COMPLETE", TacticalGreen)
                    PipelineStatus.ERROR -> Triple(TacticalRed.copy(alpha = 0.2f), "ERROR", TacticalRed)
                    PipelineStatus.BACKEND_OFFLINE -> Triple(TacticalRed.copy(alpha = 0.2f), "BACKEND OFFLINE", TacticalRed)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Stages Pipeline Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PipelineStepItem("CAPTURE", currentStage.ordinal >= 0, currentStage == PipelineStage.CAPTURE)
                PipelineDivider(currentStage.ordinal > 0)
                PipelineStepItem("NOISE", currentStage.ordinal >= 1, currentStage == PipelineStage.NOISE_ANALYSIS)
                PipelineDivider(currentStage.ordinal > 1)
                PipelineStepItem("GTCRN", currentStage.ordinal >= 2, currentStage == PipelineStage.SPEECH_ENHANCEMENT)
                PipelineDivider(currentStage.ordinal > 2)
                PipelineStepItem("OUTPUT", currentStage.ordinal >= 3, currentStage == PipelineStage.OUTPUT)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TacticalNavyDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(TacticalOlive, TacticalGreen)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusText,
                color = TacticalTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PipelineStepItem(
    label: String,
    isCompletedOrActive: Boolean,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) TacticalAmber
                    else if (isCompletedOrActive) TacticalGreen
                    else TacticalNavyCardLight
                )
                .border(
                    1.dp,
                    if (isActive) TacticalAmber else TacticalBorder,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompletedOrActive && !isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = TacticalNavyDark,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) TacticalNavyDark else TacticalTextMuted)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) TacticalAmber else if (isCompletedOrActive) TacticalTextPrimary else TacticalTextMuted
        )
    }
}

@Composable
private fun PipelineDivider(isCompleted: Boolean) {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(2.dp)
            .background(if (isCompleted) TacticalGreen else TacticalBorder)
    )
}

@Composable
fun QualityMetricsCard(
    snrImprovement: Float = 12.4f,
    siSnr: Float = 11.8f,
    pesq: Float = 3.2f,
    modelName: String = "GTCRN (16 kHz)",
    isSimulated: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TacticalNavyCard)
            .border(1.dp, TacticalBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
            .testTag("quality_metrics_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quality Metrics",
                    color = TacticalTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (isSimulated) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TacticalAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "DIAGNOSTIC TEST BENCH",
                            color = TacticalAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TacticalGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = modelName,
                            color = TacticalGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MetricRow(
                title = "SNR Improvement",
                value = String.format("+%.1f dB", snrImprovement),
                valueColor = TacticalGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetricRow(
                title = "SI-SNR",
                value = String.format("+%.1f dB", siSnr),
                valueColor = TacticalGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            MetricRow(
                title = "PESQ (est.)",
                value = String.format("%.2f", pesq),
                valueColor = TacticalGreen
            )
        }
    }
}

@Composable
fun MetricRow(
    title: String,
    value: String,
    valueColor: Color = TacticalGreen
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TacticalTextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
