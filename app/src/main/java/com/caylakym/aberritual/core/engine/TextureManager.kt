package com.caylakym.aberritual.core.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import java.io.File

class TextureManager {

    private val textureIds = IntArray(5) { 0 }
    var loadedCount: Int = 0
        private set

    fun loadFromFiles(files: List<File>): Int {
        release()

        val count = files.take(5).size
        val generated = IntArray(count)
        if (count > 0) {
            GLES20.glGenTextures(count, generated, 0)
        }

        for (i in 0 until count) {
            val file = files[i]
            if (!file.exists()) continue

            val bitmap = BitmapFactory.decodeFile(file.absolutePath) ?: continue
            textureIds[i] = generated[i]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }

        loadedCount = count
        return loadedCount
    }

    fun loadFromBitmaps(bitmaps: List<Bitmap>): Int {
        release()

        val count = bitmaps.take(5).size
        val generated = IntArray(count)
        if (count > 0) {
            GLES20.glGenTextures(count, generated, 0)
        }

        for (i in 0 until count) {
            val bitmap = bitmaps[i]
            textureIds[i] = generated[i]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[i])
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }

        loadedCount = count
        return loadedCount
    }

    fun getTextureId(index: Int): Int {
        if (index in 0 until loadedCount) {
            return textureIds[index]
        }
        return 0
    }

    fun release() {
        for (i in 0 until 5) {
            if (textureIds[i] != 0) {
                val toDelete = intArrayOf(textureIds[i])
                GLES20.glDeleteTextures(1, toDelete, 0)
                textureIds[i] = 0
            }
        }
        loadedCount = 0
    }
}
