package com.lukasz.nonogram.screens.camera

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.lukasz.nonogram.extensions.getGrayMap
import com.lukasz.nonogram.screens.camera.model.Line
import com.lukasz.nonogram.screens.camera.model.Orientation
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.random.Random

class CameraController : ViewModel() {
    private val model = CameraModel()

    override fun onCleared() {
        model.cleanModel()
    }

    fun calculateEdges(): Mat? {
        val gray = model.grayImage ?: return null

        val edges = Mat()

        Imgproc.Canny(
            gray,
            edges,
            100.0,
            200.0
        )
        return edges
    }

    fun detectLineSegments() {
        if (model.edgeImage?.empty() != false) {
            return
        }

        val lines = Mat()

        Imgproc.HoughLinesP(
            model.edgeImage,
            lines,
            1.0,
            Math.PI / 180.0,
            80,
            50.0,
            10.0
        )

        model.updateLines(lines)
    }

    fun findHorizontalVerticalLines() {
        val lines = model.lines ?: return

        val angleTolerance = 10.0

        val horizontalLines = mutableListOf<Line>()
        val verticalLines = mutableListOf<Line>()

        for (i in 0 until lines.rows()) {

            val line = lines.get(i, 0) ?: continue

            if (line.size < 4) continue

            val x1 = line[0]
            val y1 = line[1]
            val x2 = line[2]
            val y2 = line[3]

            val dx = x2 - x1
            val dy = y2 - y1

            val angle = Math.toDegrees(atan2(dy, dx))

            when {
                abs(angle) <= angleTolerance || abs(angle) >= 180.0 - angleTolerance -> {
                    val detectedLine = Line(Point(x1, y1), Point(x2, y2), Orientation.HORIZONTAL)
                    horizontalLines.add(detectedLine)
                }

                abs(abs(angle) - 90.0) <= angleTolerance -> {
                    val detectedLine = Line(Point(x1, y1), Point(x2, y2), Orientation.VERTICAL)
                    verticalLines.add(detectedLine)
                }
            }
        }

        model.updateHorizontalLines(horizontalLines)
        model.updateVerticalLines(verticalLines)
    }

    fun normalizeLines() {
        val threshold = 10

        normalizeHorizontalLines(threshold)
        normalizeVerticalLines(threshold)
    }

    fun normalizeHorizontalLines(threshold: Int = 10) {
        val lines = model.horizontalLines.sortedBy { it.start.y }
        if (lines.isEmpty()) return

        val linesGroup = mutableListOf<Line>()
        val normalizedLines = mutableListOf<Line>()
        var y = lines[0].start.y

        for (line in lines) {
            if (y - threshold < line.start.y && line.start.y < y + threshold) {
                linesGroup.add(line)
            } else {
                val merged = mergeHorizontalLines(linesGroup)
                normalizedLines.add(merged)
                linesGroup.clear()
                linesGroup.add(line)
                y = line.start.y
            }
        }

        model.updateHorizontalLines(normalizedLines)
    }

    fun normalizeVerticalLines(threshold: Int = 10) {
        val lines = model.verticalLines.sortedBy { it.start.x }
        if (lines.isEmpty()) return

        val linesGroup = mutableListOf<Line>()
        val normalizedLines = mutableListOf<Line>()
        var x = lines[0].start.x

        for (line in lines) {
            if (x - threshold < line.start.x && line.start.x < x + threshold) {
                linesGroup.add(line)
            } else {
                val merged = mergeVerticalLines(linesGroup)
                normalizedLines.add(merged)
                linesGroup.clear()
                linesGroup.add(line)
                x = line.start.x
            }
        }

        model.updateVerticalLines(normalizedLines)
    }

    fun mergeHorizontalLines(lines: List<Line>): Line {
        var start = lines[0].start
        var end = lines[0].end

        for (current in lines) {
            if (current.start.x < start.x) {
                start = current.start
            }

            if (current.end.x > end.x) {
                end = current.end
            }
        }

        return Line(start, end, Orientation.HORIZONTAL)
    }

    fun mergeVerticalLines(lines: List<Line>): Line {
        var start = lines[0].start
        var end = lines[0].end

        for (current in lines) {
            if (current.start.y < start.y) {
                start = current.start
            }

            if (current.end.y > end.y) {
                end = current.end
            }
        }

        return Line(start, end, Orientation.VERTICAL)
    }


    fun drowDetectedLines(): Mat? {
        if (model.grayImage?.empty() != false) return null
        if (model.horizontalLines.isEmpty()) return null
        if (model.verticalLines.isEmpty()) return null

        val result = Mat()

        try {
            Imgproc.cvtColor(model.grayImage, result, Imgproc.COLOR_GRAY2BGR)

            for (horizontalLine in model.horizontalLines) {
                Imgproc.line(
                    result,
                    horizontalLine.start,
                    horizontalLine.end,
                    randomColor(),
                    2
                )
            }

            for (verticalLine in model.verticalLines) {
                Imgproc.line(
                    result,
                    verticalLine.start,
                    verticalLine.end,
                    randomColor(),
                    2
                )
            }

            return result

        } catch (e: Exception) {
            result.release()
            throw e
        } finally {
            model.lines?.release()
        }
    }


    fun drawGridLines(): Mat? {
        val gray = model.grayImage ?: return null

        if (gray.empty()) return null

        val result = Mat()

        try {
            Imgproc.cvtColor(
                gray,
                result,
                Imgproc.COLOR_GRAY2BGR
            )

            for (line in model.gridHorizontalLines) {
                Imgproc.line(
                    result,
                    line.start,
                    line.end,
                    randomColor(),
                    2
                )
            }

            for (line in model.gridVerticalLines) {
                Imgproc.line(
                    result,
                    line.start,
                    line.end,
                    Scalar(255.0, 0.0, 0.0),
                    2
                )
            }

            return result
        } catch (e: Exception) {
            result.release()
            throw e
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        var grayImage: Mat? = null
        var edgeImage: Mat? = null
        var detectedLinesImage: Mat? = null

        try {
            grayImage = imageProxy.getGrayMap().clone()
            edgeImage = calculateEdges()
            detectLineSegments()
            findHorizontalVerticalLines()
            normalizeLines()
            detectedLinesImage = drowDetectedLines() //drawGridLines()

            val grayResult = grayImage
            val edgeResult = edgeImage
            val detectedLinesResult = detectedLinesImage

            Handler(Looper.getMainLooper()).post {
                model.updateGrayImage(grayResult)
                model.updateEdgeImage(edgeResult)
                model.updateLinesImage(detectedLinesResult)
            }

            grayImage = null
            edgeImage = null
            detectedLinesImage = null

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            grayImage?.release()
            edgeImage?.release()
            detectedLinesImage?.release()
            imageProxy.close()
        }
    }

    // Getters
    fun getGrayImage(): Mat {
        return model.grayImage ?: model.noPicImage
    }

    fun getEdgeImage(): Mat {
        return model.edgeImage ?: model.noPicImage
    }

    fun getLinesImage(): Mat {
        return model.linesImage ?: model.noPicImage
    }

    private fun randomColor(): Scalar {
        return Scalar(
            Random.nextInt(256).toDouble(),
            Random.nextInt(256).toDouble(),
            Random.nextInt(256).toDouble()
        )
    }
}