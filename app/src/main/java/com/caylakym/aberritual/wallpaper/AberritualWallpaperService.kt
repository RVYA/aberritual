package com.caylakym.aberritual.wallpaper

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.caylakym.aberritual.core.sensor.MotionSensorManager
import com.caylakym.aberritual.data.repository.WallpaperRepository
import com.caylakym.aberritual.data.repository.WallpaperRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AberritualWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LenticularWallpaperEngine()
    }

    private inner class LenticularWallpaperEngine : Engine() {

        private val glEngine = GLWallpaperEngine(applicationContext)
        private val sensorManager = MotionSensorManager(applicationContext)
        private val repository: WallpaperRepository = WallpaperRepositoryImpl(applicationContext)
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        private var isVisibleState = false

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)

            scope.launch {
                repository.configFlow.collect { config ->
                    val layerFiles = config.layers.map { layer ->
                        repository.getLayerFile(layer.filePath)
                    }

                    sensorManager.currentAxis = config.tiltAxis
                    sensorManager.sensitivityDegrees = config.sensitivity
                    sensorManager.invertAxis = config.invertTilt

                    glEngine.updateConfig(config, layerFiles)
                }
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            glEngine.onSurfaceCreated(holder)
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            glEngine.onSurfaceChanged(holder, width, height)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            glEngine.onSurfaceDestroyed()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisibleState = visible
            glEngine.onVisibilityChanged(visible)

            if (visible) {
                sensorManager.startListening(
                    axis = sensorManager.currentAxis,
                    sensitivity = sensorManager.sensitivityDegrees,
                    invert = sensorManager.invertAxis
                ) { tilt ->
                    glEngine.requestRender(tilt)
                }
            } else {
                sensorManager.stopListening()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            sensorManager.stopListening()
            scope.cancel()
            glEngine.release()
        }
    }
}
