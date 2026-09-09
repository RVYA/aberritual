package com.caylakym.aberritual.core.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.caylakym.aberritual.data.model.TiltAxis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.PI

class MotionSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    private val filter = EmaFilter(alpha = 0.20f, deadband = 0.003f)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private val _tiltFlow = MutableStateFlow(0.0f)
    val tiltFlow: StateFlow<Float> = _tiltFlow.asStateFlow()

    private var onTiltCallback: ((Float) -> Unit)? = null

    var currentAxis: TiltAxis = TiltAxis.HORIZONTAL
    var sensitivityDegrees: Float = 25.0f
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

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        } else {
            return
        }

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val pitchDegrees = (orientationAngles[1] * (180.0f / PI.toFloat()))
        val rollDegrees = (orientationAngles[2] * (180.0f / PI.toFloat()))

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
        _tiltFlow.value = smoothed
        onTiltCallback?.invoke(smoothed)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
