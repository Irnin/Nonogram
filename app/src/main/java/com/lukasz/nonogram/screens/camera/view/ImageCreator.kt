package com.lukasz.nonogram.screens.camera.view

import Constants
import android.R.attr.font
import com.lukasz.nonogram.extensions.Util
import com.lukasz.nonogram.screens.camera.model.Line
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class ImageCreator {
    fun createLinesImage(
        sourceImage: Mat,
        horizontalLines: List<Line>,
        verticalLines: List<Line>
    ): Mat {

        if (sourceImage.empty()) return Constants.NO_PICTURE

        var result = Mat()
        var counter = 0

        try {
            if (sourceImage.channels() == 1) {
                Imgproc.cvtColor(sourceImage, result, Imgproc.COLOR_GRAY2BGR)
            } else {
                result = sourceImage.clone()
            }

            counter = 0
            for (horizontalLine in horizontalLines) {
                Imgproc.putText(
                    result,
                    "$counter",
                    horizontalLine.start,
                    font,
                    1.0,
                    Scalar(255.0, 255.0, 255.0),
                    1,
                    Imgproc.LINE_AA,
                    false
                )

                Imgproc.line(
                    result,
                    horizontalLine.start,
                    horizontalLine.end,
                    Util.getRandomScalar(),
                    2
                )

                counter += 1
            }

            counter = 0
            for (verticalLine in verticalLines) {

                Imgproc.putText(
                    result,
                    "$counter",
                    verticalLine.start,
                    font,
                    1.0,
                    Scalar(0.0, 255.0, 255.0),
                    1,
                    Imgproc.LINE_AA,
                    false
                )

                Imgproc.line(
                    result,
                    verticalLine.start,
                    verticalLine.end,
                    Util.getRandomScalar(),
                    2
                )

                counter += 1
            }

            return result

        } catch (e: Exception) {
            result.release()
            throw e
        }
    }

    fun createEdgesImage(sourceImage: Mat): Mat {
        var source = Mat()

        if (sourceImage.channels() == 1) {
            source = sourceImage.clone()
        } else {
            Imgproc.cvtColor(sourceImage, source, Imgproc.COLOR_GRAY2BGR)
        }

        val result = Mat()

        Imgproc.Canny(
            source,
            result,
            100.0,
            200.0
        )
        return result
    }

    fun createCornersImage(sourceImage: Mat, corners: List<Point>): Mat {
        if (sourceImage.empty()) return Constants.NO_PICTURE

        var result = Mat()

        if (sourceImage.channels() == 1) {
            Imgproc.cvtColor(
                sourceImage,
                result,
                Imgproc.COLOR_GRAY2BGR
            )
        } else {
            result = sourceImage.clone()
        }

        for (point in corners) {
            Imgproc.circle(
                result,
                point,
                5,
                Scalar(0.0, 0.0, 255.0),
                -1
            )
        }

        return result

    }
}