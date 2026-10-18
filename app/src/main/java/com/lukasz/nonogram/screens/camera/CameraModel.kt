package com.lukasz.nonogram.screens.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lukasz.nonogram.screens.camera.model.Line
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

class CameraModel {
    var imageHeight: Int = 0
    var imageWidth: Int = 0

    var grayImage by mutableStateOf<Mat?>(null)
        private set

    var edgeImage by mutableStateOf<Mat?>(null)
        private set

    var linesImage by mutableStateOf<Mat?>(null)
        private set

    var lines by mutableStateOf<Mat?>(null)
        private set

    // Wszystkie linie wykryte przez HoughLinesP().
    var horizontalLines by mutableStateOf<List<Line>>(emptyList())
        private set

    var verticalLines by mutableStateOf<List<Line>>(emptyList())
        private set

    // Linie wybrane przez algorytm jako należące do siatki.
    var gridHorizontalLines by mutableStateOf<List<Line>>(emptyList())
        private set

    var gridVerticalLines by mutableStateOf<List<Line>>(emptyList())
        private set

    fun updateGridHorizontalLines(lines: List<Line>) {
        gridHorizontalLines = lines
    }

    fun updateGridVerticalLines(lines: List<Line>) {
        gridVerticalLines = lines
    }

    val noPicImage = createNoCameraImage(600, 300)

    fun updateGrayImage(mat: Mat?) {
        mat ?: return

        grayImage?.release()
        grayImage = mat

        imageWidth = mat.width()
        imageHeight = mat.height()
    }

    fun updateEdgeImage(mat: Mat?) {
        mat ?: return

        edgeImage?.release()
        edgeImage = mat
    }

    fun updateLines(mat: Mat?) {
        mat ?: return
        lines?.release()
        lines = mat
    }

    fun updateHorizontalLines(lines: List<Line>) {
        horizontalLines = lines
    }

    fun updateVerticalLines(lines: List<Line>) {
        verticalLines = lines
    }

    fun updateLinesImage(mat: Mat?) {
        mat ?: return
        linesImage?.release()
        linesImage = mat
    }

    fun cleanModel() {
        grayImage?.release()
        grayImage = null

        edgeImage?.release()
        edgeImage = null

        linesImage?.release()
        linesImage = null

        lines?.release()
        lines = null

        horizontalLines = emptyList()
        verticalLines = emptyList()

        gridHorizontalLines = emptyList()
        gridVerticalLines = emptyList()
    }

    fun createNoCameraImage(width: Int, height: Int): Mat {
        val image = Mat.zeros(height, width, CvType.CV_8UC3)

        val text = "NO IMAGE"
        val font = Imgproc.FONT_HERSHEY_SIMPLEX
        val fontScale = 1.0
        val thickness = 2

        val textSize = Imgproc.getTextSize(
            text,
            font,
            fontScale,
            thickness,
            null
        )

        val x = (width - textSize.width) / 2.0
        val y = (height + textSize.height) / 2.0

        Imgproc.putText(
            image,
            text,
            Point(x, y),
            font,
            fontScale,
            Scalar(255.0, 255.0, 255.0),
            thickness,
            Imgproc.LINE_AA,
            false
        )

        return image
    }
}