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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lukasz.nonogram.extensions.getGrayMap
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class CameraController : ViewModel() {

    private val model = CameraModel()

    var grayImage by mutableStateOf<Mat?>(null)
        private set

    fun updateGrayImage(mat: Mat) {
        grayImage?.release()
        grayImage = mat
    }

    override fun onCleared() {
        grayImage?.release()
        grayImage = null
    }

    fun getDetectedText(): List<DetectedText> {
        return model.detectedText
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        try {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->

                    val newDetectedText = visionText.textBlocks.mapNotNull { block ->
                        val rect = block.boundingBox ?: return@mapNotNull null

                        DetectedText(
                            rect.left,
                            rect.top,
                            rect.width(),
                            rect.height(),
                            block.text
                        )
                    }

                    Handler(Looper.getMainLooper()).post {
                        model.detectedText = newDetectedText
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MLKit", "There was an issue during text decognition", exception)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }


            val grayMat = imageProxy.getGrayMap()

            try {
                val horizontalLines = detectHorizontalLines(grayMat)
                Log.d("OpenCV", "found horizontal line: ${horizontalLines.joinToString()}")

                // Tworzymy osobną kopię, którą może przechowywać ViewModel.
                val displayMat = grayMat.clone()

                Handler(Looper.getMainLooper()).post {
                    updateGrayImage(displayMat)
                }
            } finally {
                grayMat.release()
            }

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Can not process frame", e)
            imageProxy.close()
        }
    }

    private fun detectHorizontalLines(gray: Mat): List<Int> {
        val binary = Mat()
        val horizontalLines = Mat()
        var kernel: Mat? = null

        try {
            // 1. Binaryzacja obrazu.
            Imgproc.threshold(
                gray,
                binary,
                0.0,
                255.0,
                Imgproc.THRESH_BINARY_INV or Imgproc.THRESH_OTSU
            )

            // 2. Utworzenie poziomego elementu strukturalnego.
            val horizontalLength = (gray.cols() / 20).coerceAtLeast(20)

            kernel = Imgproc.getStructuringElement(
                Imgproc.MORPH_RECT,
                Size(horizontalLength.toDouble(), 1.0)
            )

            // 3. Wyodrębnienie poziomych linii.
            Imgproc.morphologyEx(
                binary,
                horizontalLines,
                Imgproc.MORPH_OPEN,
                kernel
            )

            // 4. Zliczenie białych pikseli w każdym wierszu.
            val rowProjection = DoubleArray(gray.rows())

            for (y in 0 until horizontalLines.rows()) {
                val row = horizontalLines.row(y)

                try {
                    rowProjection[y] = Core.countNonZero(row).toDouble()
                } finally {
                    row.release()
                }
            }

            // 5. Wybranie wierszy, które prawdopodobnie zawierają linie.
            val threshold = gray.cols() * 0.5

            val candidateRows = rowProjection.indices.filter { y ->
                rowProjection[y] >= threshold
            }

            // 6. Połączenie sąsiadujących pikseli w jedną linię.
            return groupNearbyRows(candidateRows, maxGap = 3)

        } finally {
            binary.release()
            horizontalLines.release()

            kernel?.release()
        }
    }

    fun groupNearbyRows(rows: List<Int>, maxGap: Int): List<Int> {
        if (rows.isEmpty()) return emptyList()

        val result = mutableListOf<Int>()

        var start = rows.first()
        var previous = start
        var count = 1

        for (i in 1 until rows.size) {
            val current = rows[i]

            if (current - previous <= maxGap + 1) {
                count++
            } else {
                result.add((start + previous) / 2)
                start = current
                count = 1
            }

            previous = current
        }

        result.add((start + previous) / 2)

        return result
    }
}