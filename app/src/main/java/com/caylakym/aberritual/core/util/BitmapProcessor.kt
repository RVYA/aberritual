package com.caylakym.aberritual.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.media.ExifInterface
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

            val orientedBitmap = applyAutoOrientation(sourceUri, sampledBitmap)
            val croppedBitmap = centerCropAndScale(orientedBitmap, targetWidth, targetHeight)

            if (orientedBitmap != sampledBitmap) {
                sampledBitmap.recycle()
            }
            if (croppedBitmap != orientedBitmap) {
                orientedBitmap.recycle()
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
        } catch (_: Exception) {
            false
        }
    }

    private fun applyAutoOrientation(uri: Uri, bitmap: Bitmap): Bitmap {
        var rotationDegrees = 0f

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                rotationDegrees = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }
        } catch (_: Exception) {}

        val effectiveWidth = if (rotationDegrees == 90f || rotationDegrees == 270f) bitmap.height else bitmap.width
        val effectiveHeight = if (rotationDegrees == 90f || rotationDegrees == 270f) bitmap.width else bitmap.height

        if (effectiveWidth > effectiveHeight) {
            rotationDegrees = (rotationDegrees + 90f) % 360f
        }

        if (rotationDegrees == 0f) return bitmap

        val matrix = Matrix().apply { postRotate(rotationDegrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
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
        val canvas = Canvas(output)
        val matrix = Matrix().apply {
            postScale(scale, scale)
            postTranslate(left, top)
        }
        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(src, matrix, paint)

        return output
    }
}
