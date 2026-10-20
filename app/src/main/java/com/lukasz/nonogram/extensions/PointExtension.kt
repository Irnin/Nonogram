package com.lukasz.nonogram.extensions

import org.opencv.core.Point
import kotlin.math.abs

fun Point.isAround(p: Point, threshold: Int = 10): Boolean {

    if (abs(this.x - p.x) > threshold)
        return false

    if (abs(this.y - p.y) > threshold) {
        return false
    }

    return true
}