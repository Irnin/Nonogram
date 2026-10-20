package com.lukasz.nonogram.screens.camera.model

import Constants
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.opencv.core.Mat

class Image {
    private var image by mutableStateOf(Constants.NO_PICTURE)

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    constructor() {
        updateImage(null)
    }

    fun updateImage(mat: Mat?) {
        clean()
        
        if (mat == null) {
            image = Constants.NO_PICTURE
            updateSize()
            return
        }

        image = mat

        updateSize()
    }

    fun get(): Mat {
        return image
    }

    fun clean() {
        image.release()
    }

    private fun updateSize() {
        width = image.width()
        height = image.height()
    }
}