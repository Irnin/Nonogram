package com.lukasz.nonogram.screens.camera.model

import org.opencv.core.Point

/**
 * Used to define detected localization of a nonogram board
 */
data class BoardRegion(
    val topLeft: Point,
    val topRight: Point,
    val bottomLeft: Point,
    val bottomRight: Point
) {

}