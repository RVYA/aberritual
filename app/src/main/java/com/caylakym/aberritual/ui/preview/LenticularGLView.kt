package com.caylakym.aberritual.ui.preview

import android.content.Context
import android.opengl.GLSurfaceView
import com.caylakym.aberritual.core.engine.LenticularRenderer
import com.caylakym.aberritual.data.model.WallpaperConfig
import java.io.File
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class LenticularGLView(context: Context) : GLSurfaceView(context) {

    private val renderer = LenticularRenderer()
    private var currentTilt = 0.0f
    private var pendingFiles: List<File>? = null
    private var isSurfaceCreated = false

    init {
        setEGLContextClientVersion(2)
        setZOrderMediaOverlay(true)
        setRenderer(object : Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                renderer.onSurfaceCreated(context)
                isSurfaceCreated = true
                pendingFiles?.let { files ->
                    renderer.loadLayerFiles(files)
                }
                requestRender()
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                renderer.onSurfaceChanged(width, height)
                requestRender()
            }

            override fun onDrawFrame(gl: GL10?) {
                renderer.render(currentTilt)
            }
        })
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateConfig(config: WallpaperConfig, files: List<File>) {
        pendingFiles = files
        queueEvent {
            renderer.currentConfig = config
            if (isSurfaceCreated) {
                renderer.loadLayerFiles(files)
            }
            requestRender()
        }
    }

    fun setTilt(tilt: Float) {
        currentTilt = tilt
        requestRender()
    }

    fun release() {
        queueEvent {
            renderer.release()
        }
    }
}
