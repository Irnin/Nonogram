package com.lukasz.nonogram.extensions

import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import org.opencv.core.CvType
import org.opencv.core.Mat

fun ImageProxy.getGrayMap(): Mat {
    require(this.format == ImageFormat.YUV_420_888) {
        "Oczekiwano obrazu YUV_420_888"
    }

    val plane = this.planes[0]
    val buffer = plane.buffer.duplicate()

    val width = this.width
    val height = this.height
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride

    val pixels = ByteArray(width * height)

    for (y in 0 until height) {
        val rowStart = y * rowStride

        for (x in 0 until width) {
            val index = rowStart + x * pixelStride

            if (index >= buffer.limit()) {
                throw IllegalStateException("Nieprawidłowy indeks płaszczyzny Y: $index, " + "limit: ${buffer.limit()}")
            }

            pixels[y * width + x] = buffer.get(index)
        }
    }

    val gray = Mat(height, width, CvType.CV_8UC1)

    gray.put(0, 0, pixels)

    return gray
}