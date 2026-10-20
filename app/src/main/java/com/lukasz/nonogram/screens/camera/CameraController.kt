package com.lukasz.nonogram.screens.camera

import Constants
import android.R.attr.font
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.lukasz.nonogram.extensions.Util
import com.lukasz.nonogram.extensions.getGrayMap
import com.lukasz.nonogram.screens.camera.model.ImageType
import com.lukasz.nonogram.screens.camera.model.Line
import com.lukasz.nonogram.screens.camera.tools.GridDetector
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.sqrt

class CameraController : ViewModel() {
    private val model = CameraModel()
    private val gridDetector = GridDetector(model)

    override fun onCleared() {
        model.cleanModel()
    }

    fun calculateEdges(): Mat {
        val gray = model.getImage(ImageType.GRAY)

        val edges = Mat()

        Imgproc.Canny(
            gray,
            edges,
            100.0,
            200.0
        )
        return edges
    }


    fun createBoardImage(): Mat {
        val result = getImage(ImageType.GRAY).clone()

        val points = model.corners.toList()

        for (point in points) {
            val lines = model.horizontalLines
            for (line in lines) {
                if (!isPointOnLine(line, point)) {
                    continue
                }

                Imgproc.circle(
                    result,
                    point,
                    5,
                    Scalar(255.0, 105.0, 180.0, 255.0),
                    2
                )

            }
        }

        return result
    }

    fun isPointOnLine(
        line: Line,
        point: Point,
        epsilon: Double = 2.0
    ): Boolean {
        val dx = line.end.x - line.start.x
        val dy = line.end.y - line.start.y

        val px = point.x - line.start.x
        val py = point.y - line.start.y

        val length = sqrt(dx * dx + dy * dy)

        if (length == 0.0) {
            return sqrt(px * px + py * py) <= epsilon
        }

        val distance = abs(dx * py - dy * px) / length

        return distance <= epsilon
    }

    // DETECTED LINES==========================================================================================================
    fun createLinesImage(): Mat {
        if (model.getImage(ImageType.GRAY).empty()) return Constants.NO_PICTURE

        val grayImage = model.getImage(ImageType.GRAY)

        val result = Mat()
        var counter = 0

        try {
            Imgproc.cvtColor(grayImage, result, Imgproc.COLOR_GRAY2BGR)

            counter = 0
            for (horizontalLine in model.horizontalLines) {
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
            for (verticalLine in model.verticalLines) {

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
        } finally {
            model.lines?.release()
        }
    }
    
    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        var grayImage: Mat? = null
        var edgeImage: Mat? = null

        try {
            grayImage = imageProxy.getGrayMap().clone()

            if (grayImage.empty()) {
                Log.e("CameraAnalysis", "Pobrany obraz jest pusty")
                return
            }

            // Zapisz nową klatkę przed rozpoczęciem detekcji.
            model.updateImage(ImageType.GRAY, grayImage)

            edgeImage = Mat()

            Imgproc.Canny(
                grayImage,
                edgeImage,
                100.0,
                200.0
            )

            model.updateImage(ImageType.EDGES, edgeImage)

            // Teraz GridDetector widzi aktualne obrazy.
            gridDetector.initialProcess()

            Handler(Looper.getMainLooper()).post {
                model.updateImage(ImageType.LINES, createLinesImage())
                model.updateImage(ImageType.BOARD, createBoardImage())
            }

            // Własność obrazów została przekazana do modelu.
            grayImage = null
            edgeImage = null

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            grayImage?.release()
            edgeImage?.release()
            imageProxy.close()
        }
    }

    // Getters

    fun getImage(type: ImageType): Mat {
        return model.getImage(type)
    }
}