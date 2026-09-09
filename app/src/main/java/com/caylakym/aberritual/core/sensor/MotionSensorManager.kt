package com.caylakym.aberritual.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.caylakym.aberritual.data.model.TiltAxis
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

class MotionSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val filter = EmaFilter(alpha = 0.20f, deadband = 0.003f)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var onTiltCallback: ((Float) -> Unit)? = null

    var currentAxis: TiltAxis = TiltAxis.HORIZONTAL
    var sensitivityDegrees: Float = 20.0f
    var invertAxis: Boolean = false

    private var pitchBaseline: Float? = null
    private var isListening = false

    fun startListening(
        axis: TiltAxis = currentAxis,
        sensitivity: Float = sensitivityDegrees,
        invert: Boolean = invertAxis,
        onTilt: ((Float) -> Unit)? = null
    ) {
        if (isListening) return
        currentAxis = axis
        sensitivityDegrees = sensitivity
        invertAxis = invert
        onTiltCallback = onTilt
        pitchBaseline = null
        filter.reset()

        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            isListening = true
        }
    }

    fun stopListening() {
        if (!isListening) return
        sensorManager.unregisterListener(this)
        isListening = false
        onTiltCallback = null
        pitchBaseline = null
        filter.reset()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val (pitchDegrees, rollDegrees) = when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                val pitch = orientationAngles[1] * (180.0f / PI.toFloat())
                val roll = orientationAngles[2] * (180.0f / PI.toFloat())
                Pair(pitch, roll)
            }
            Sensor.TYPE_GRAVITY, Sensor.TYPE_ACCELEROMETER -> {
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]
                val roll = (atan2(ax.toDouble(), sqrt((ay * ay + az * az).toDouble())) * (180.0 / PI)).toFloat()
                val pitch = (atan2(-ay.toDouble(), sqrt((ax * ax + az * az).toDouble())) * (180.0 / PI)).toFloat()
                Pair(pitch, roll)
            }
            else -> return
        }

        val rawAngle = when (currentAxis) {
            TiltAxis.HORIZONTAL -> rollDegrees
            TiltAxis.VERTICAL -> {
                if (pitchBaseline == null) {
                    pitchBaseline = pitchDegrees
                } else {
                    pitchBaseline = pitchBaseline!! * 0.992f + pitchDegrees * 0.008f
                }
                pitchDegrees - (pitchBaseline ?: pitchDegrees)
            }
        }

        val clampedAngle = rawAngle.coerceIn(-sensitivityDegrees, sensitivityDegrees)
        var normalized = clampedAngle / sensitivityDegrees

        if (invertAxis) {
            normalized = -normalized
        }

        val smoothed = filter.filter(normalized)
        onTiltCallback?.invoke(smoothed)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
