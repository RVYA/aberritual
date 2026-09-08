package com.caylakym.aberritual.data.local

import android.content.Context
import java.io.File
import java.util.UUID

class StorageManager(private val context: Context) {

    private val layersDirectory: File
        get() {
            val dir = File(context.filesDir, "wallpaper_layers")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun generateLayerFile(id: String = UUID.randomUUID().toString()): File {
        return File(layersDirectory, "layer_$id.webp")
    }

    fun getLayerFile(fileName: String): File {
        return File(layersDirectory, fileName)
    }

    fun deleteOrphanedLayers(activeFileNames: Set<String>) {
        val files = layersDirectory.listFiles() ?: return
        for (file in files) {
            if (file.isFile && !activeFileNames.contains(file.name)) {
                file.delete()
            }
        }
    }

    fun clearAllLayers() {
        val files = layersDirectory.listFiles() ?: return
        for (file in files) {
            if (file.isFile) {
                file.delete()
            }
        }
    }
}
