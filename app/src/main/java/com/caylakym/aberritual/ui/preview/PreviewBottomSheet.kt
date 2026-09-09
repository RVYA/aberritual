package com.caylakym.aberritual.ui.preview

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.caylakym.aberritual.core.sensor.MotionSensorManager
import com.caylakym.aberritual.data.model.EffectMode
import com.caylakym.aberritual.ui.creator.StudioUiState
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewBottomSheet(
    uiState: StudioUiState,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
    onSensitivityChanged: (Float) -> Unit,
    onEffectModeChanged: (EffectMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sensorManager = remember { MotionSensorManager(context) }

    var isGyroActive by remember { mutableStateOf(true) }
    var touchTilt by remember { mutableFloatStateOf(0.0f) }
    var glViewInstance by remember { mutableStateOf<LenticularGLView?>(null) }

    val sensitivityCheckpoints = remember { listOf(10f, 15f, 20f, 25f, 30f, 45f, 60f) }

    LaunchedEffect(glViewInstance, uiState.layers, uiState.effectMode, uiState.lpi, uiState.chromaticAberration) {
        glViewInstance?.updateConfig(uiState.toWallpaperConfig(), uiState.layerFiles)
    }

    DisposableEffect(isGyroActive, uiState.tiltAxis, uiState.sensitivityDegrees, uiState.invertAxis) {
        if (isGyroActive) {
            sensorManager.startListening(
                axis = uiState.tiltAxis,
                sensitivity = uiState.sensitivityDegrees,
                invert = uiState.invertAxis
            ) { tilt ->
                glViewInstance?.setTilt(tilt)
            }
        } else {
            sensorManager.stopListening()
            glViewInstance?.setTilt(touchTilt)
        }

        onDispose {
            sensorManager.stopListening()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Live Lenticular Preview",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(20.dp))
                    .pointerInput(isGyroActive) {
                        if (!isGyroActive) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                val delta = dragAmount / size.width
                                touchTilt = (touchTilt + delta * 2.2f).coerceIn(-1.0f, 1.0f)
                                glViewInstance?.setTilt(touchTilt)
                            }
                        }
                    },
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AndroidView(
                        factory = { ctx ->
                            LenticularGLView(ctx).also { view ->
                                glViewInstance = view
                                view.updateConfig(uiState.toWallpaperConfig(), uiState.layerFiles)
                            }
                        },
                        modifier = Modifier.matchParentSize(),
                        onRelease = { view ->
                            view.release()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = isGyroActive,
                    onClick = { isGyroActive = true },
                    label = { Text("📱 Gyroscope Motion") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = !isGyroActive,
                    onClick = { isGyroActive = false },
                    label = { Text("👆 Touch Drag") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Transition Mode",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EffectMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.effectMode == mode,
                                onClick = { onEffectModeChanged(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            EffectMode.LENTICULAR -> "Lenticular"
                                            EffectMode.FLUID_MORPH -> "Fluid Morph"
                                            EffectMode.STEPPED_FLIP -> "Stepped Flip"
                                        }
                                    )
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Motion Sensitivity", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "${uiState.sensitivityDegrees.roundToInt()}°",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Slider(
                        value = uiState.sensitivityDegrees,
                        onValueChange = { rawVal ->
                            val closest = sensitivityCheckpoints.minByOrNull { abs(it - rawVal) } ?: rawVal
                            onSensitivityChanged(closest)
                        },
                        valueRange = 10.0f..60.0f,
                        steps = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(text = "Apply as Live Wallpaper", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
