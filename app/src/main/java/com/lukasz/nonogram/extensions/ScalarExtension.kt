package com.lukasz.nonogram.extensions

import com.lukasz.nonogram.screens.camera.model.Line
import org.opencv.core.Point
import org.opencv.core.Scalar
import kotlin.math.abs
import kotlin.random.Random


object Util {

    fun getRandomScalar(): Scalar {
        return Scalar(
            Random.nextInt(256).toDouble(),
            Random.nextInt(256).toDouble(),
            Random.nextInt(256).toDouble()
        )
    }

    fun intersection(
        line1: Line,
        line2: Line,
        offsetX: Double = 0.0,
        offsetY: Double = 0.0
    ): Point? {
        val x1 = line1.start.x + offsetX
        val y1 = line1.start.y + offsetY
        val x2 = line1.end.x + offsetX
        val y2 = line1.end.y + offsetY

        val x3 = line2.start.x
        val y3 = line2.start.y
        val x4 = line2.end.x
        val y4 = line2.end.y

        val denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4)

        if (abs(denominator) < 1e-9) {
            return null
        }

        val determinant1 = x1 * y2 - y1 * x2
        val determinant2 = x3 * y4 - y3 * x4

        val px = (
                determinant1 * (x3 - x4) -
                        (x1 - x2) * determinant2
                ) / denominator

        val py = (
                determinant1 * (y3 - y4) -
                        (y1 - y2) * determinant2
                ) / denominator

        return Point(px, py)
    }
}