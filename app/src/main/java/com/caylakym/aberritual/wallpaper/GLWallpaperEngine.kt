package com.caylakym.aberritual.wallpaper

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.EGLExt
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.caylakym.aberritual.core.engine.LenticularRenderer
import com.caylakym.aberritual.data.model.WallpaperConfig
import java.io.File

class GLWallpaperEngine(private val context: Context) {

    val renderer = LenticularRenderer()

    private var renderThread: HandlerThread? = null
    private var renderHandler: Handler? = null

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var eglConfig: EGLConfig? = null

    private var surfaceHolder: SurfaceHolder? = null
    private var isSurfaceCreated = false
    private var isVisible = false
    private var currentTilt = 0.0f
    private var pendingFiles: List<File>? = null

    init {
        val thread = HandlerThread("GLWallpaperRenderThread").apply { start() }
        renderThread = thread
        renderHandler = Handler(thread.looper)
    }

    fun onSurfaceCreated(holder: SurfaceHolder) {
        surfaceHolder = holder
        renderHandler?.post {
            initEGL()
            createEGLSurface(holder)
            renderer.onSurfaceCreated(context)
            isSurfaceCreated = true
            pendingFiles?.let { files ->
                renderer.loadLayerFiles(files)
            }
            drawFrame()
        }
    }

    fun onSurfaceChanged(holder: SurfaceHolder, width: Int, height: Int) {
        surfaceHolder = holder
        renderHandler?.post {
            if (eglSurface == EGL14.EGL_NO_SURFACE) {
                createEGLSurface(holder)
            }
            renderer.onSurfaceChanged(width, height)
            drawFrame()
        }
    }

    fun onSurfaceDestroyed() {
        isSurfaceCreated = false
        renderHandler?.post {
            destroyEGLSurface()
        }
    }

    fun onVisibilityChanged(visible: Boolean) {
        isVisible = visible
        if (visible) {
            renderHandler?.post {
                drawFrame()
            }
        }
    }

    fun requestRender(tiltAngle: Float) {
        currentTilt = tiltAngle
        if (!isVisible || !isSurfaceCreated) return

        renderHandler?.post {
            drawFrame()
        }
    }

    fun updateConfig(config: WallpaperConfig, files: List<File>) {
        pendingFiles = files
        renderHandler?.post {
            renderer.currentConfig = config
            if (isSurfaceCreated && eglContext != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
                renderer.loadLayerFiles(files)
                drawFrame()
            }
        }
    }

    private fun drawFrame() {
        if (eglDisplay == EGL14.EGL_NO_DISPLAY || eglSurface == EGL14.EGL_NO_SURFACE) {
            return
        }

        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
        renderer.render(currentTilt)
        EGL14.eglSwapBuffers(eglDisplay, eglSurface)
    }

    private fun initEGL() {
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) return

        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_DEPTH_SIZE, 0,
            EGL14.EGL_STENCIL_SIZE, 0,
            EGL14.EGL_NONE
        )

        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        val success = EGL14.eglChooseConfig(
            eglDisplay,
            configAttribs,
            0,
            configs,
            0,
            1,
            numConfigs,
            0
        )

        if (!success || numConfigs[0] == 0) {
            configAttribs[1] = EGL14.EGL_OPENGL_ES2_BIT
            EGL14.eglChooseConfig(
                eglDisplay,
                configAttribs,
                0,
                configs,
                0,
                1,
                numConfigs,
                0
            )
        }

        eglConfig = configs[0]

        val contextAttribs = intArrayOf(
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL14.EGL_NONE
        )

        eglContext = EGL14.eglCreateContext(
            eglDisplay,
            eglConfig,
            EGL14.EGL_NO_CONTEXT,
            contextAttribs,
            0
        )
    }

    private fun createEGLSurface(holder: SurfaceHolder) {
        destroyEGLSurface()
        if (!holder.surface.isValid || eglConfig == null) return

        val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
        eglSurface = EGL14.eglCreateWindowSurface(
            eglDisplay,
            eglConfig,
            holder.surface,
            surfaceAttribs,
            0
        )
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    private fun destroyEGLSurface() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(
                eglDisplay,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_SURFACE,
                EGL14.EGL_NO_CONTEXT
            )
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            eglSurface = EGL14.EGL_NO_SURFACE
        }
    }

    fun release() {
        renderHandler?.post {
            renderer.release()
            destroyEGLSurface()
            if (eglContext != EGL14.EGL_NO_CONTEXT) {
                EGL14.eglDestroyContext(eglDisplay, eglContext)
                eglContext = EGL14.EGL_NO_CONTEXT
            }
            if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglTerminate(eglDisplay)
                eglDisplay = EGL14.EGL_NO_DISPLAY
            }
        }
        renderThread?.quitSafely()
        renderThread = null
        renderHandler = null
    }
}
