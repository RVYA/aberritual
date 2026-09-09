package com.caylakym.aberritual.data.model

data class WallpaperConfig(
    val layers: List<LayerItem> = emptyList(),
    val effectMode: EffectMode = EffectMode.LENTICULAR,
    val tiltAxis: TiltAxis = TiltAxis.HORIZONTAL,
    val sensitivityDegrees: Float = 20.0f,
    val invertAxis: Boolean = false,
    val chromaticAberration: Boolean = false,
    val lpi: Float = 30.0f,
    val wrapAround: Boolean = false
)
