package com.caylakym.aberritual.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

class BitmapProcessor(private val context: Context) {

    fun processAndSaveImage(
        sourceUri: Uri,
        destinationFile: File,
        targetWidth: Int = 1080,
        targetHeight: Int = 2400
    ): Boolean {
        return try {
            val sampledBitmap = decodeSampledBitmapFromUri(sourceUri, targetWidth, targetHeight)
                ?: return false

            val croppedBitmap = centerCropAndScale(sampledBitmap, targetWidth, targetHeight)
            if (croppedBitmap != sampledBitmap) {
                sampledBitmap.recycle()
            }

            FileOutputStream(destinationFile).use { outputStream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    croppedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outputStream)
                } else {
                    @Suppress("DEPRECATION")
                    croppedBitmap.compress(Bitmap.CompressFormat.WEBP, 85, outputStream)
                }
            }

            croppedBitmap.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun decodeSampledBitmapFromUri(
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun centerCropAndScale(
        src: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val srcWidth = src.width
        val srcHeight = src.height

        val scale = max(
            targetWidth.toFloat() / srcWidth.toFloat(),
            targetHeight.toFloat() / srcHeight.toFloat()
        )

        val scaledWidth = scale * srcWidth
        val scaledHeight = scale * srcHeight

        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(output)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(left, top)
        }
        val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(src, matrix, paint)

        return output
    }
}
