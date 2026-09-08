package com.caylakym.aberritual.data.repository

import android.content.Context
import android.net.Uri
import com.caylakym.aberritual.core.util.BitmapProcessor
import com.caylakym.aberritual.data.local.StorageManager
import com.caylakym.aberritual.data.local.WallpaperPreferences
import com.caylakym.aberritual.data.model.LayerItem
import com.caylakym.aberritual.data.model.WallpaperConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

interface WallpaperRepository {
    val configFlow: StateFlow<WallpaperConfig>
    suspend fun importLayers(uris: List<Uri>, targetWidth: Int, targetHeight: Int): List<LayerItem>
    suspend fun saveAndApplyConfig(config: WallpaperConfig)
    fun getActiveConfig(): WallpaperConfig
    fun getLayerFile(fileName: String): File
}

class WallpaperRepositoryImpl(
    private val context: Context,
    private val storageManager: StorageManager = StorageManager(context),
    private val preferences: WallpaperPreferences = WallpaperPreferences(context),
    private val bitmapProcessor: BitmapProcessor = BitmapProcessor(context)
) : WallpaperRepository {

    private val _configFlow = MutableStateFlow(preferences.loadConfig())
    override val configFlow: StateFlow<WallpaperConfig> = _configFlow.asStateFlow()

    override suspend fun importLayers(
        uris: List<Uri>,
        targetWidth: Int,
        targetHeight: Int
    ): List<LayerItem> = withContext(Dispatchers.IO) {
        val importedLayers = mutableListOf<LayerItem>()

        uris.take(5).forEachIndexed { index, uri ->
            val layerId = UUID.randomUUID().toString()
            val destFile = storageManager.generateLayerFile(layerId)
            val success = bitmapProcessor.processAndSaveImage(
                sourceUri = uri,
                destinationFile = destFile,
                targetWidth = targetWidth,
                targetHeight = targetHeight
            )

            if (success) {
                importedLayers.add(
                    LayerItem(
                        id = layerId,
                        filePath = destFile.name,
                        order = index
                    )
                )
            }
        }

        importedLayers
    }

    override suspend fun saveAndApplyConfig(config: WallpaperConfig) = withContext(Dispatchers.IO) {
        preferences.saveConfig(config)

        val activeFileNames = config.layers.map { it.filePath }.toSet()
        storageManager.deleteOrphanedLayers(activeFileNames)

        _configFlow.value = config
    }

    override fun getActiveConfig(): WallpaperConfig {
        return preferences.loadConfig()
    }

    override fun getLayerFile(fileName: String): File {
        return storageManager.getLayerFile(fileName)
    }
}
