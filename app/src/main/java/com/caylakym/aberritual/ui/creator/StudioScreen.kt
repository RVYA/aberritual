package com.caylakym.aberritual.ui.creator

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.caylakym.aberritual.data.model.EffectMode
import com.caylakym.aberritual.data.model.TiltAxis
import com.caylakym.aberritual.ui.preview.PreviewBottomSheet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.importImages(uris, screenWidthPx, screenHeightPx)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aberritual Studio") }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.layers.isEmpty()) "Import Photos" else "Replace Photos")
                    }

                    Button(
                        onClick = { viewModel.openPreview() },
                        enabled = uiState.layers.size >= 2,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Preview & Apply")
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (uiState.isImporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing & optimizing layers...", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lenticular Layers (${uiState.layers.size}/5)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (uiState.layers.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Photos Imported",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Select 2 to 5 photographs to create your dynamic lenticular live wallpaper.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                FilledTonalButton(
                                    onClick = {
                                        photoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                ) {
                                    Text("Pick 2–5 Photos")
                                }
                            }
                        }
                    }
                } else {
                    itemsIndexed(uiState.layers, key = { _, layer -> layer.id }) { index, layer ->
                        val layerFile = uiState.layerFiles.getOrNull(index)
                        val bitmap = remember(layerFile?.absolutePath) {
                            layerFile?.let { if (it.exists()) BitmapFactory.decodeFile(it.absolutePath) else null }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Layer $index",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Surface(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {}
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Frame ${index + 1}",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = if (index == 0) "Left / Top angle" else if (index == uiState.layers.size - 1) "Right / Bottom angle" else "Mid transition",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Row {
                                    TextButton(
                                        onClick = { viewModel.moveLayerUp(index) },
                                        enabled = index > 0
                                    ) {
                                        Text("▲")
                                    }
                                    TextButton(
                                        onClick = { viewModel.moveLayerDown(index) },
                                        enabled = index < uiState.layers.size - 1
                                    ) {
                                        Text("▼")
                                    }
                                    TextButton(
                                        onClick = { viewModel.removeLayer(index) }
                                    ) {
                                        Text("✕")
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(text = "Optical Effect & Physics", style = MaterialTheme.typography.titleMedium)
                }

                item {
                    Text(text = "Transition Mode", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EffectMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.effectMode == mode,
                                onClick = { viewModel.setEffectMode(mode) },
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
                }

                item {
                    Text(text = "Tilt Orientation Axis", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TiltAxis.entries.forEach { axis ->
                            FilterChip(
                                selected = uiState.tiltAxis == axis,
                                onClick = { viewModel.setTiltAxis(axis) },
                                label = {
                                    Text(
                                        when (axis) {
                                            TiltAxis.HORIZONTAL -> "Horizontal Roll"
                                            TiltAxis.VERTICAL -> "Vertical Pitch"
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Motion Sensitivity", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "${uiState.sensitivityDegrees.roundToInt()}°", style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = uiState.sensitivityDegrees,
                        onValueChange = { viewModel.setSensitivity(it) },
                        valueRange = 10.0f..60.0f
                    )
                }

                if (uiState.effectMode == EffectMode.LENTICULAR) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Virtual LPI (Lens Density)", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "${uiState.lpi.roundToInt()} LPI", style = MaterialTheme.typography.bodyMedium)
                        }
                        Slider(
                            value = uiState.lpi,
                            onValueChange = { viewModel.setLpi(it) },
                            valueRange = 10.0f..60.0f
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Chromatic Aberration (RGB Shift)", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = uiState.chromaticAberration,
                            onCheckedChange = { viewModel.toggleChromaticAberration() }
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Invert Tilt Direction", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = uiState.invertAxis,
                            onCheckedChange = { viewModel.toggleInvertAxis() }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    if (uiState.isPreviewOpen) {
        PreviewBottomSheet(
            uiState = uiState,
            onDismissRequest = { viewModel.closePreview() },
            onApply = {
                viewModel.applyWallpaper(context)
                viewModel.closePreview()
            },
            onSensitivityChanged = { viewModel.setSensitivity(it) },
            onEffectModeChanged = { viewModel.setEffectMode(it) }
        )
    }
}
