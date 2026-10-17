package com.lukasz.nonogram.extensions

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

fun Mat.getBitmap(): Bitmap {
    val bitmap = createBitmap(this.cols(), this.rows())
    val rgbaMat = Mat()

    try {
        Imgproc.cvtColor(this, rgbaMat, Imgproc.COLOR_GRAY2RGBA)
        Utils.matToBitmap(rgbaMat, bitmap)
    } finally {
        rgbaMat.release()
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