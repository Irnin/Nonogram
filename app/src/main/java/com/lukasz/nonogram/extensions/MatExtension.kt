package com.lukasz.nonogram.extensions

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun Mat.getBitmap(): Bitmap {
    require(!empty()) {
        "Cannot convert an empty Mat to Bitmap"
    }

    val bitmap = Bitmap.createBitmap(
        cols(),
        rows(),
        Bitmap.Config.ARGB_8888
    )

    val rgba = Mat()

    try {
        when (channels()) {
            1 -> Imgproc.cvtColor(this, rgba, Imgproc.COLOR_GRAY2RGBA)
            3 -> Imgproc.cvtColor(this, rgba, Imgproc.COLOR_BGR2RGBA)
            4 -> Imgproc.cvtColor(this, rgba, Imgproc.COLOR_BGRA2RGBA)
            else -> error("Unsupported Mat channels: ${channels()}")
        }

        Utils.matToBitmap(rgba, bitmap)
    } finally {
        rgba.release()
    }

    return bitmap
}

fun Mat.normalizeOrientation(degrees: Int): Mat {
    val rotated = Mat()

    when (degrees) {
        90 -> Core.rotate(this, rotated, Core.ROTATE_90_CLOCKWISE)
        180 -> Core.rotate(this, rotated, Core.ROTATE_180)
        270 -> Core.rotate(this, rotated, Core.ROTATE_90_COUNTERCLOCKWISE)
        else -> this.copyTo(rotated)
    }

    return rotated
}