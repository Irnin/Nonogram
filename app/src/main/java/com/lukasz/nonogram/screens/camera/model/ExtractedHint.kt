package com.lukasz.nonogram.screens.camera.model

import org.opencv.core.Mat

class ExtractedHint(
    val image: Mat,
    val number: Int,
    val orientation: Line.Orientation
) {
}