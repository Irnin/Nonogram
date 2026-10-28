package com.lukasz.nonogram.screens.camera.view

import Constants
import android.R.attr.font
import com.lukasz.nonogram.extensions.Util
import com.lukasz.nonogram.screens.camera.model.Lines
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ImageCreator {
    fun createLinesImage(sourceImage: Mat, lines: Lines): Mat {

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
            for (horizontalLine in lines.horizontalLines) {
                Imgproc.putText(
                    result,
                    "$counter",
                    Point(10.0, horizontalLine.start.y),
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
            for (verticalLine in lines.verticalLines) {

                Imgproc.putText(
                    result,
                    "$counter",
                    Point(verticalLine.start.x, 25.0),
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

    fun adaptiveThreshold(gray: Mat): Mat {
        val binary = Mat()

        Imgproc.adaptiveThreshold(
            gray,
            binary,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY,
            31,
            10.0
        )

        return binary
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

    fun upscaleForOcr(source: Mat, scale: Double = 3.0): Mat {

        val enlarged = Mat()

        Imgproc.resize(
            source,
            enlarged,
            Size(
                source.cols() * scale,
                source.rows() * scale
            ),
            0.0,
            0.0,
            Imgproc.INTER_CUBIC
        )

        return enlarged
    }

    fun openDigits(binary: Mat): Mat {
        val result = Mat()

        val kernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            Size(2.0, 2.0)
        )

        Imgproc.morphologyEx(
            binary,
            result,
            Imgproc.MORPH_OPEN,
            kernel
        )

        kernel.release()

        return result
    }

    fun adaptiveThresholdForOcr(gray: Mat): Mat {

        val binary = Mat()

        Imgproc.adaptiveThreshold(
            gray,
            binary,
            255.0,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            31,
            7.0
        )

        return binary
    }

    fun removeHorizontalLines(binary: Mat): Mat {
        require(!binary.empty()) { "Source image is empty" }

        val horizontalLines = Mat()
        val result = Mat()

        val kernelWidth = (binary.cols() / 2).coerceAtLeast(10)

        val kernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            Size(kernelWidth.toDouble(), 1.0)
        )

        Imgproc.morphologyEx(
            binary,
            horizontalLines,
            Imgproc.MORPH_OPEN,
            kernel
        )

        Core.subtract(binary, horizontalLines, result)

        horizontalLines.release()
        kernel.release()

        return result
    }
}