package com.lukasz.nonogram.screens.camera

import Constants
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lukasz.nonogram.screens.camera.model.Image
import com.lukasz.nonogram.screens.camera.model.ImageType
import com.lukasz.nonogram.screens.camera.model.Line
import org.opencv.core.Mat
import org.opencv.core.Point

class CameraModel {
    val images = mutableMapOf<ImageType, Image>()
    var corners = mutableListOf<Point>()

    constructor() {
        for (type in ImageType.entries) {
            images[type] = Image()
        }
    }

    fun getImage(type: ImageType): Mat {
        return images[type]?.get() ?: Constants.NO_PICTURE
    }

    fun updateImage(type: ImageType, image: Mat?) {
        images[type]?.updateImage(image)
    }

    var lines by mutableStateOf(Mat())
        private set

    var horizontalLines by mutableStateOf<List<Line>>(emptyList())
        private set

    var verticalLines by mutableStateOf<List<Line>>(emptyList())
        private set

    fun updateLines(mat: Mat) {
        lines.release()
        lines = mat
    }

    fun updateCorners(points: List<Point>) {
        corners = points as MutableList<Point>
    }

    fun updateHorizontalLines(lines: List<Line>) {
        horizontalLines = lines
    }

    fun updateVerticalLines(lines: List<Line>) {
        verticalLines = lines
    }

    fun cleanModel() {
        for (image in images.values) {
            image.clean()
        }

        lines.release()

        horizontalLines = emptyList()
        verticalLines = emptyList()
    }
}