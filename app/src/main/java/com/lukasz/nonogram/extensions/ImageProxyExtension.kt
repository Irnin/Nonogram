package com.lukasz.nonogram.extensions

import androidx.camera.core.ImageProxy
import org.opencv.core.CvType
import org.opencv.core.Mat

fun ImageProxy.getGrayMap(): Mat {
    val plane = planes[0]
    val buffer = plane.buffer.duplicate()

    val rowStride = plane.rowStride

    val pixels = ByteArray(width * height)

    for (y in 0 until height) {
        for (x in 0 until width) {
            val bufferIndex = y * rowStride + x
            pixels[y * width + x] = buffer.get(bufferIndex)
        }
    }

    val gray = Mat(height, width, CvType.CV_8UC1)
    gray.put(0, 0, pixels)

    val rotated = gray.normalizeOrientation(imageInfo.rotationDegrees)

    gray.release()
    return rotated
}