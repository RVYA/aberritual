package com.caylakym.aberritual.core.engine

import android.content.Context
import android.opengl.GLES20
import java.io.BufferedReader
import java.io.InputStreamReader

class ShaderProgram {

    var programId: Int = 0
        private set

    var aPositionLoc: Int = -1
        private set
    var aTexCoordLoc: Int = -1
        private set

    val uTextureLocs = IntArray(5) { -1 }
    var uLayerCountLoc: Int = -1
        private set
    var uTiltAngleLoc: Int = -1
        private set
    var uModeLoc: Int = -1
        private set
    var uLpiLoc: Int = -1
        private set
    var uChromaticLoc: Int = -1
        private set
    var uResolutionLoc: Int = -1
        private set

    fun loadFromAssets(
        context: Context,
        vertexPath: String = "shaders/passthrough.vert",
        fragmentPath: String = "shaders/lenticular.frag"
    ): Boolean {
        val vertexCode = readAssetFile(context, vertexPath) ?: return false
        val fragmentCode = readAssetFile(context, fragmentPath) ?: return false

        val vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        if (vertexShader == 0) return false

        val fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        if (fragmentShader == 0) {
            GLES20.glDeleteShader(vertexShader)
            return false
        }

        programId = linkProgram(vertexShader, fragmentShader)

        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)

        if (programId == 0) return false

        cacheLocations()
        return true
    }

    fun use() {
        if (programId != 0) {
            GLES20.glUseProgram(programId)
        }
    }

    fun release() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
    }

    private fun cacheLocations() {
        aPositionLoc = GLES20.glGetAttribLocation(programId, "a_Position")
        aTexCoordLoc = GLES20.glGetAttribLocation(programId, "a_TexCoord")

        for (i in 0..4) {
            uTextureLocs[i] = GLES20.glGetUniformLocation(programId, "u_Texture$i")
        }

        uLayerCountLoc = GLES20.glGetUniformLocation(programId, "u_LayerCount")
        uTiltAngleLoc = GLES20.glGetUniformLocation(programId, "u_TiltAngle")
        uModeLoc = GLES20.glGetUniformLocation(programId, "u_Mode")
        uLpiLoc = GLES20.glGetUniformLocation(programId, "u_LPI")
        uChromaticLoc = GLES20.glGetUniformLocation(programId, "u_ChromaticAberration")
        uResolutionLoc = GLES20.glGetUniformLocation(programId, "u_Resolution")
    }

    private fun compileShader(type: Int, code: String): Int {
        val shaderId = GLES20.glCreateShader(type)
        if (shaderId == 0) return 0

        GLES20.glShaderSource(shaderId, code)
        GLES20.glCompileShader(shaderId)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shaderId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderId)
            return 0
        }
        return shaderId
    }

    private fun linkProgram(vertexShader: Int, fragmentShader: Int): Int {
        val progId = GLES20.glCreateProgram()
        if (progId == 0) return 0

        GLES20.glAttachShader(progId, vertexShader)
        GLES20.glAttachShader(progId, fragmentShader)
        GLES20.glLinkProgram(progId)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(progId, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(progId)
            return 0
        }
        return progId
    }

    private fun readAssetFile(context: Context, path: String): String? {
        return try {
            context.assets.open(path).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
