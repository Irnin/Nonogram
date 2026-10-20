package com.lukasz.nonogram.screens.camera.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.opencv.core.Mat

class Image {
    private var image by mutableStateOf(Mat())

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    constructor() {
        updateSize()
    }

    @Synchronized
    fun updateImage(mat: Mat?) {
        val newImage = if (mat == null || mat.empty()) {
            Mat()
        } else {
            mat.clone()
        }

        val oldImage = image
        image = newImage

        updateSize()
        oldImage.release()
    }

    @Synchronized
    fun get(): Mat {
        return image
    }

    @Synchronized
    fun clean() {
        image.release()
        image = Mat()
        updateSize()
    }

    private fun updateSize() {
        width = image.width()
        height = image.height()
    }

    enum class ImageType {
        GLOBAL_GRAY, GLOBAL_EDGES, GLOBAL_LINES, GLOBAL_CORNERS, BOARD, DRAFT
    }
}

