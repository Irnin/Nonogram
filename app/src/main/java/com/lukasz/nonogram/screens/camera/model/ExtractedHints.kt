package com.lukasz.nonogram.screens.camera.model

import org.opencv.core.Mat

class ExtractedHints(
    val columns: List<ExtractedHint>,
    val rows: List<ExtractedHint>
) {

    fun getColumn(number: Int): Mat {
        return columns
            .firstOrNull { it.number == number }
            ?.image
            ?: Mat()
    }

    fun getRow(number: Int): Mat {
        return rows
            .firstOrNull { it.number == number }
            ?.image
            ?: Mat()
    }
}