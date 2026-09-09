package com.caylakym.aberritual.ui.creator

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caylakym.aberritual.data.model.EffectMode
import com.caylakym.aberritual.data.model.LayerItem
import com.caylakym.aberritual.data.model.TiltAxis
import com.caylakym.aberritual.data.repository.WallpaperRepository
import com.caylakym.aberritual.wallpaper.AberritualWallpaperService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class StudioViewModel(
    private val repository: WallpaperRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    init {
        loadCurrentConfig()
    }

    private fun loadCurrentConfig() {
        val config = repository.getActiveConfig()
        val files = config.layers.map { repository.getLayerFile(it.filePath) }
        _uiState.update {
            it.copy(
                layers = config.layers,
                layerFiles = files,
                effectMode = config.effectMode,
                tiltAxis = config.tiltAxis,
                sensitivityDegrees = config.sensitivityDegrees,
                invertAxis = config.invertAxis,
                chromaticAberration = config.chromaticAberration,
                lpi = config.lpi
            )
        }
    }

    fun importImages(uris: List<Uri>, viewportWidth: Int, viewportHeight: Int) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val imported = repository.importLayers(uris, viewportWidth, viewportHeight)
            val updatedLayers = (imported).take(5)
            val files = updatedLayers.map { repository.getLayerFile(it.filePath) }
            _uiState.update {
                it.copy(
                    layers = updatedLayers,
                    layerFiles = files,
                    isImporting = false
                )
            }
        }
    }

    fun moveLayerUp(index: Int) {
        if (index <= 0) return
        val currentList = _uiState.value.layers.toMutableList()
        val item = currentList.removeAt(index)
        currentList.add(index - 1, item)
        val reindexed = currentList.mapIndexed { i, layer -> layer.copy(order = i) }
        val files = reindexed.map { repository.getLayerFile(it.filePath) }
        _uiState.update { it.copy(layers = reindexed, layerFiles = files) }
    }

    fun moveLayerDown(index: Int) {
        val currentList = _uiState.value.layers.toMutableList()
        if (index >= currentList.size - 1) return
        val item = currentList.removeAt(index)
        currentList.add(index + 1, item)
        val reindexed = currentList.mapIndexed { i, layer -> layer.copy(order = i) }
        val files = reindexed.map { repository.getLayerFile(it.filePath) }
        _uiState.update { it.copy(layers = reindexed, layerFiles = files) }
    }

    fun removeLayer(index: Int) {
        val currentList = _uiState.value.layers.toMutableList()
        if (index !in currentList.indices) return
        currentList.removeAt(index)
        val reindexed = currentList.mapIndexed { i, layer -> layer.copy(order = i) }
        val files = reindexed.map { repository.getLayerFile(it.filePath) }
        _uiState.update { it.copy(layers = reindexed, layerFiles = files) }
    }

    fun setEffectMode(mode: EffectMode) {
        _uiState.update { it.copy(effectMode = mode) }
    }

    fun setTiltAxis(axis: TiltAxis) {
        _uiState.update { it.copy(tiltAxis = axis) }
    }

    fun setSensitivity(degrees: Float) {
        _uiState.update { it.copy(sensitivityDegrees = degrees) }
    }

    fun toggleInvertAxis() {
        _uiState.update { it.copy(invertAxis = !it.invertAxis) }
    }

    fun toggleChromaticAberration() {
        _uiState.update { it.copy(chromaticAberration = !it.chromaticAberration) }
    }

    fun setLpi(lpi: Float) {
        _uiState.update { it.copy(lpi = lpi) }
    }

    fun openPreview() {
        _uiState.update { it.copy(isPreviewOpen = true) }
    }

    fun closePreview() {
        _uiState.update { it.copy(isPreviewOpen = false) }
    }

    fun setSimulatedTilt(tilt: Float) {
        _uiState.update { it.copy(simulatedTilt = tilt) }
    }

    fun applyWallpaper(context: Context) {
        viewModelScope.launch {
            val config = _uiState.value.toWallpaperConfig()
            repository.saveAndApplyConfig(config)
            _uiState.update { it.copy(isSaved = true) }

            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, AberritualWallpaperService::class.java)
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    class Factory(private val repository: WallpaperRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StudioViewModel(repository) as T
        }
    }
}
