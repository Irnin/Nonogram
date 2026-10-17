package com.lukasz.nonogram.screens.camera

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.lukasz.nonogram.extensions.getGrayMap
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class CameraController : ViewModel() {

    private val model = CameraModel()

    var grayImage by mutableStateOf<Mat?>(null)
        private set

    var edgeImage by mutableStateOf<Mat?>(null)
        private set

    var linesImage by mutableStateOf<Mat?>(null)
        private set

    fun updateGrayImage(mat: Mat?) {
        mat ?: return

        grayImage?.release()
        grayImage = mat
    }

    fun updateEdgeImage(mat: Mat?) {
        mat ?: return

        edgeImage?.release()
        edgeImage = mat
    }

    fun updateLinesImage(mat: Mat?) {
        mat ?: return
        linesImage?.release()
        linesImage = mat
    }

    override fun onCleared() {
        grayImage?.release()
        grayImage = null

        edgeImage?.release()
        edgeImage = null

        linesImage?.release()
        linesImage = null
    }

    fun calculateCanny(grayMat: Mat?): Mat? {
        val gray = grayMat ?: return null

        val edges = Mat()

        Imgproc.Canny(
            gray,
            edges,
            100.0,
            200.0
        )
        return edges
    }

    fun detectLineSegments(edges: Mat?): Mat? {
        if (edges == null || edges.empty()) {
            return null
        }

        val lines = Mat()

        Imgproc.HoughLinesP(
            edges,
            lines,
            1.0,
            Math.PI / 180.0,
            80,     // threshold
            50.0,   // minLineLength
            10.0    // maxLineGap
        )

        return lines
    }

    fun drowLine(): Mat? {
        val angleTolerance = 10.0

        val gray = grayImage ?: return null
        val edges = edgeImage ?: return null

        if (gray.empty() || edges.empty()) return null

        val result = Mat()
        var lines: Mat? = null

        try {
            Imgproc.cvtColor(gray, result, Imgproc.COLOR_GRAY2BGR)

            lines = detectLineSegments(edges)

            if (lines == null || lines.empty()) {
                return result
            }

            for (i in 0 until lines.rows()) {
                val line = lines.get(i, 0) ?: continue
                if (line.size < 4) continue

                val x1 = line[0]
                val y1 = line[1]
                val x2 = line[2]
                val y2 = line[3]

                val dx = x2 - x1
                val dy = y2 - y1

                val angle = Math.toDegrees(
                    kotlin.math.atan2(dy, dx)
                )

                val color = when {
                    kotlin.math.abs(angle) <= angleTolerance ||
                            kotlin.math.abs(angle) >= 180.0 - angleTolerance ->
                        Scalar(0.0, 0.0, 255.0)

                    kotlin.math.abs(kotlin.math.abs(angle) - 90.0) <= angleTolerance ->
                        Scalar(255.0, 0.0, 0.0)

                    else -> continue
                }

                Imgproc.line(
                    result,
                    Point(x1, y1),
                    Point(x2, y2),
                    color,
                    2
                )
            }

            return result

        } catch (e: Exception) {
            result.release()
            throw e
        } finally {
            lines?.release()
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        var grayMat: Mat? = null
        var edgeMat: Mat? = null
        var lineMat: Mat? = null

        try {
            grayMat = imageProxy.getGrayMap().clone()
            edgeMat = calculateCanny(grayMat)
            lineMat = drowLine()

            val grayResult = grayMat
            val edgeResult = edgeMat
            val lineResult = lineMat

            Handler(Looper.getMainLooper()).post {
                updateGrayImage(grayResult)
                updateEdgeImage(edgeResult)
                updateLinesImage(lineResult)
            }

            grayMat = null
            edgeMat = null
            lineMat = null

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            grayMat?.release()
            edgeMat?.release()
            lineMat?.release()
            imageProxy.close()
        }
    }
}