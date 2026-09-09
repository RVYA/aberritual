package com.caylakym.aberritual.core.engine

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.caylakym.aberritual.data.model.EffectMode
import com.caylakym.aberritual.data.model.WallpaperConfig
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class LenticularRenderer {

    private val shaderProgram = ShaderProgram()
    val textureManager = TextureManager()

    private var viewportWidth = 1080
    private var viewportHeight = 2400

    var currentConfig: WallpaperConfig = WallpaperConfig()

    private val vertexData = floatArrayOf(
        -1.0f,  1.0f, 0.0f, 0.0f,
        -1.0f, -1.0f, 0.0f, 1.0f,
         1.0f,  1.0f, 1.0f, 0.0f,
         1.0f, -1.0f, 1.0f, 1.0f
    )

    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .apply {
            put(vertexData)
            position(0)
        }

    fun onSurfaceCreated(context: Context) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        shaderProgram.loadFromAssets(context)
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    fun loadLayerFiles(files: List<File>) {
        textureManager.loadFromFiles(files)
    }

    fun loadLayerBitmaps(bitmaps: List<Bitmap>) {
        textureManager.loadFromBitmaps(bitmaps)
    }

    fun render(tiltAngle: Float) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (shaderProgram.programId == 0 || textureManager.loadedCount == 0) {
            return
        }

        shaderProgram.use()

        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(shaderProgram.aPositionLoc)
        GLES20.glVertexAttribPointer(
            shaderProgram.aPositionLoc,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 4,
            vertexBuffer
        )

        vertexBuffer.position(2)
        GLES20.glEnableVertexAttribArray(shaderProgram.aTexCoordLoc)
        GLES20.glVertexAttribPointer(
            shaderProgram.aTexCoordLoc,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * 4,
            vertexBuffer
        )

        val activeCount = textureManager.loadedCount
        for (i in 0 until activeCount) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureManager.getTextureId(i))
            GLES20.glUniform1i(shaderProgram.uTextureLocs[i], i)
        }

        val modeIndex = when (currentConfig.effectMode) {
            EffectMode.LENTICULAR -> 0
            EffectMode.FLUID_MORPH -> 1
            EffectMode.STEPPED_FLIP -> 2
        }

        GLES20.glUniform1i(shaderProgram.uLayerCountLoc, activeCount)
        GLES20.glUniform1f(shaderProgram.uTiltAngleLoc, tiltAngle)
        GLES20.glUniform1i(shaderProgram.uModeLoc, modeIndex)
        GLES20.glUniform1f(shaderProgram.uLpiLoc, currentConfig.lpi)
        GLES20.glUniform1i(shaderProgram.uChromaticLoc, if (currentConfig.chromaticAberration) 1 else 0)
        GLES20.glUniform1i(shaderProgram.uWrapAroundLoc, if (currentConfig.wrapAround) 1 else 0)
        GLES20.glUniform2f(
            shaderProgram.uResolutionLoc,
            viewportWidth.toFloat(),
            viewportHeight.toFloat()
        )

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(shaderProgram.aPositionLoc)
        GLES20.glDisableVertexAttribArray(shaderProgram.aTexCoordLoc)
    }

    fun release() {
        textureManager.release()
        shaderProgram.release()
    }
}
