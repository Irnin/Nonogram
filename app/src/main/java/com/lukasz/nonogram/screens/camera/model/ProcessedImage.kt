package com.lukasz.nonogram.screens.camera.model

import org.opencv.core.Mat

class ProcessedImage(
    val lines: Lines,
    val sourceImage: Mat,
    val edgesImage: Mat,
    val linesImage: Mat
) {
}