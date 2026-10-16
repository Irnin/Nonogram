package com.lukasz.nonogram.extensions

import android.graphics.Bitmap
import androidx.core.graphics.createBitmap
import org.opencv.android.Utils
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