package com.lukasz.nonogram.screens.camera.model

import com.lukasz.nonogram.extensions.Util
import org.opencv.core.Point
import org.opencv.core.Scalar

class Line {
    val start: Point
    val end: Point
    val index: Double
    val orientation: Orientation
    val color: Scalar = Util.getRandomScalar()

    constructor(p1: Point, p2: Point, orientation: Orientation) {
        this.orientation = orientation

        if (orientation == Orientation.HORIZONTAL) {
            if (p1.x < p2.x) {
                this.start = p1
                this.end = p2
            } else {
                this.start = p2
                this.end = p1
            }

            this.index = this.start.x
        } else {
            if (p1.y < p2.y) {
                this.start = p1
                this.end = p2
            } else {
                this.start = p2
                this.end = p1
            }

            this.index = this.start.y
        }
    }
}