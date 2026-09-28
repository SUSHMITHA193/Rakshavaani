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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NoiseCategoryCard
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
fun NoiseSettingsScreen(
    viewModel: RakshaVaaniViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPreview: () -> Unit
) {
    val selectedCategory by viewModel.selectedNoiseCategory.collectAsState()
    val noiseIntensity by viewModel.noiseIntensity.collectAsState()
    val snrShort = viewModel.getSnrShort(noiseIntensity)
    val snrDescription = viewModel.getSnrDescription(noiseIntensity)

    Scaffold(
        topBar = {
            TopTacticalBar(
                title = "Noise Environment",
                subtitle = "TACTICAL ACOUSTIC PROFILE",
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Select the primary noise type in your audio.",
                    color = TacticalTextSecondary,
                    fontSize = 14.sp
                )
            }

            // 2x3 Grid of Categories
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Row 1: UAV / Drone & Vehicle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NoiseCategoryCard(
                            title = "UAV / Drone",
                            icon = Icons.Default.Flight,
                            isSelected = selectedCategory == "UAV / Drone",
                            onClick = { viewModel.setNoiseCategory("UAV / Drone") },
                            modifier = Modifier.weight(1f)
                        )

                        NoiseCategoryCard(
                            title = "Vehicle",
                            icon = Icons.Default.DirectionsCar,
                            isSelected = selectedCategory == "Vehicle",
                            onClick = { viewModel.setNoiseCategory("Vehicle") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 2: Wind & Gunfire
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NoiseCategoryCard(
                            title = "Wind",
                            icon = Icons.Default.Air,
                            isSelected = selectedCategory == "Wind",
                            onClick = { viewModel.setNoiseCategory("Wind") },
                            modifier = Modifier.weight(1f)
                        )

                        NoiseCategoryCard(
                            title = "Gunfire",
                            icon = Icons.Default.GpsFixed,
                            isSelected = selectedCategory == "Gunfire / Impulsive",
                            onClick = { viewModel.setNoiseCategory("Gunfire / Impulsive") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Row 3: General & Custom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        NoiseCategoryCard(
                            title = "General",
                            icon = Icons.Default.GraphicEq,
                            isSelected = selectedCategory == "General",
                            onClick = { viewModel.setNoiseCategory("General") },
                            modifier = Modifier.weight(1f)
                        )

                        NoiseCategoryCard(
                            title = "Custom",
                            icon = Icons.Default.Tune,
                            isSelected = selectedCategory == "Custom / Mixed",
                            onClick = { viewModel.setNoiseCategory("Custom / Mixed") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Noise Intensity Section matching screen 3
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TacticalNavyCard)
                        .border(1.dp, TacticalBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                        .testTag("noise_intensity_card")
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Noise Intensity",
                                color = TacticalTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "$noiseIntensity%",
                                color = TacticalGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Slider(
                            value = noiseIntensity.toFloat(),
                            onValueChange = { viewModel.setNoiseIntensity(it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = TacticalGreen,
                                activeTrackColor = TacticalOlive,
                                inactiveTrackColor = TacticalNavyDark
                            ),
                            modifier = Modifier.testTag("noise_intensity_slider")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Approximate SNR Telemetry
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mapped Signal-to-Noise:",
                                color = TacticalTextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = snrDescription,
                                color = if (noiseIntensity > 75) TacticalAmber else TacticalCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Information callout box matching Screen 3
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(TacticalNavyCardLight)
                                .border(1.dp, TacticalBorderLight, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = TacticalCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Higher intensity adds more noise for better testing. Recommended range: 30% – 80%. Evaluated down to -20 dB SNR.",
                                    color = TacticalTextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Primary Next Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onNavigateToPreview,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalOlive
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("noise_settings_next_button")
                ) {
                    Text(
                        text = "Next",
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
