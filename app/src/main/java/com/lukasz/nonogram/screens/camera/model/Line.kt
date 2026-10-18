package com.lukasz.nonogram.screens.camera.model

import org.opencv.core.Point

class Line {
    val start: Point
    val end: Point

    constructor(p1: Point, p2: Point, orientation: Orientation) {

        if (orientation == Orientation.HORIZONTAL) {
            if (p1.x < p2.x) {
                this.start = p1
                this.end = p2
            } else {
                this.start = p2
                this.end = p1
            }
        } else {
            if (p1.y < p2.y) {
                this.start = p1
                this.end = p2
            } else {
                this.start = p2
                this.end = p1
            }
        }

    }
}