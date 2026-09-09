package com.caylakym.aberritual.data.local

import android.content.Context
import android.content.SharedPreferences
import com.caylakym.aberritual.data.model.EffectMode
import com.caylakym.aberritual.data.model.LayerItem
import com.caylakym.aberritual.data.model.TiltAxis
import com.caylakym.aberritual.data.model.WallpaperConfig
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class WallpaperPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveConfig(config: WallpaperConfig) {
        val layersJson = JSONArray().apply {
            config.layers.forEach { layer ->
                val obj = JSONObject().apply {
                    put(KEY_LAYER_ID, layer.id)
                    put(KEY_LAYER_PATH, layer.filePath)
                    put(KEY_LAYER_ORDER, layer.order)
                }
                put(obj)
            }
        }

        prefs.edit {
            putString(KEY_LAYERS_DATA, layersJson.toString())
                .putString(KEY_EFFECT_MODE, config.effectMode.id)
                .putString(KEY_TILT_AXIS, config.tiltAxis.id)
                .putFloat(KEY_SENSITIVITY, config.sensitivityDegrees)
                .putBoolean(KEY_INVERT_AXIS, config.invertAxis)
                .putBoolean(KEY_CHROMATIC, config.chromaticAberration)
                .putFloat(KEY_LPI, config.lpi)
                .putBoolean(KEY_WRAP_AROUND, config.wrapAround)
        }
    }

    fun loadConfig(): WallpaperConfig {
        val layersJsonStr = prefs.getString(KEY_LAYERS_DATA, null)
        val layers = mutableListOf<LayerItem>()

        if (!layersJsonStr.isNullOrEmpty()) {
            try {
                val jsonArray = JSONArray(layersJsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    layers.add(
                        LayerItem(
                            id = obj.getString(KEY_LAYER_ID),
                            filePath = obj.getString(KEY_LAYER_PATH),
                            order = obj.getInt(KEY_LAYER_ORDER)
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        val modeId = prefs.getString(KEY_EFFECT_MODE, EffectMode.LENTICULAR.id)
        val effectMode = EffectMode.entries.find { it.id == modeId } ?: EffectMode.LENTICULAR

        val axisId = prefs.getString(KEY_TILT_AXIS, TiltAxis.HORIZONTAL.id)
        val tiltAxis = TiltAxis.entries.find { it.id == axisId } ?: TiltAxis.HORIZONTAL

        val sensitivity = prefs.getFloat(KEY_SENSITIVITY, 20.0f)
        val invertAxis = prefs.getBoolean(KEY_INVERT_AXIS, false)
        val chromatic = prefs.getBoolean(KEY_CHROMATIC, false)
        val lpi = prefs.getFloat(KEY_LPI, 30.0f)
        val wrapAround = prefs.getBoolean(KEY_WRAP_AROUND, false)

        return WallpaperConfig(
            layers = layers.sortedBy { it.order },
            effectMode = effectMode,
            tiltAxis = tiltAxis,
            sensitivityDegrees = sensitivity,
            invertAxis = invertAxis,
            chromaticAberration = chromatic,
            lpi = lpi,
            wrapAround = wrapAround
        )
    }

    companion object {
        private const val PREFS_NAME = "aberritual_wallpaper_prefs"
        private const val KEY_LAYERS_DATA = "key_layers_data"
        private const val KEY_EFFECT_MODE = "key_effect_mode"
        private const val KEY_TILT_AXIS = "key_tilt_axis"
        private const val KEY_SENSITIVITY = "key_sensitivity"
        private const val KEY_INVERT_AXIS = "key_invert_axis"
        private const val KEY_CHROMATIC = "key_chromatic"
        private const val KEY_LPI = "key_lpi"
        private const val KEY_WRAP_AROUND = "key_wrap_around"

        private const val KEY_LAYER_ID = "id"
        private const val KEY_LAYER_PATH = "path"
        private const val KEY_LAYER_ORDER = "order"
    }
}
