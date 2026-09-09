package com.caylakym.aberritual.ui.creator

import com.caylakym.aberritual.data.model.EffectMode
import com.caylakym.aberritual.data.model.LayerItem
import com.caylakym.aberritual.data.model.TiltAxis
import com.caylakym.aberritual.data.model.WallpaperConfig
import java.io.File

data class StudioUiState(
    val layers: List<LayerItem> = emptyList(),
    val layerFiles: List<File> = emptyList(),
    val effectMode: EffectMode = EffectMode.LENTICULAR,
    val tiltAxis: TiltAxis = TiltAxis.HORIZONTAL,
    val sensitivityDegrees: Float = 25.0f,
    val invertAxis: Boolean = false,
    val chromaticAberration: Boolean = false,
    val lpi: Float = 30.0f,
    val isImporting: Boolean = false,
    val isPreviewOpen: Boolean = false,
    val isSaved: Boolean = false,
    val simulatedTilt: Float = 0.0f
) {
    fun toWallpaperConfig(): WallpaperConfig {
        return WallpaperConfig(
            layers = layers,
            effectMode = effectMode,
            tiltAxis = tiltAxis,
            sensitivityDegrees = sensitivityDegrees,
            invertAxis = invertAxis,
            chromaticAberration = chromaticAberration,
            lpi = lpi
        )
    }
}
