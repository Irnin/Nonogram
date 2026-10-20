package com.lukasz.nonogram.screens.camera.model

import org.opencv.core.Point

class BoardRegion(
    val topLeft: Point,
    val topRight: Point,
    val bottomLeft: Point,
    val bottomRight: Point
) {

}