package com.lukasz.nonogram.screens

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lukasz.nonogram.DetectedText
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import org.opencv.android.Utils
import androidx.core.graphics.createBitmap
import com.lukasz.nonogram.ViewModel

private val cameraExecutor = Executors.newSingleThreadExecutor()
private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
private var detectedText by mutableStateOf<List<DetectedText>>(emptyList())

@Composable
fun CameraScreen(vm: ViewModel) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CameraPreview(vm)
            MyCanvas()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MatImage(vm.grayImage)
        }
    }
}

@Composable
fun CameraPreview(vm: ViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(factory = { context ->
        PreviewView(context).apply {

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

            cameraProviderFuture.addListener({

                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build()         // ??

                preview.surfaceProvider = surfaceProvider

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImage(vm, imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)

            }, ContextCompat.getMainExecutor(context))
        }
    },
        modifier = Modifier.fillMaxSize())
}

@Composable
fun MyCanvas() {
    Canvas(Modifier.fillMaxSize()) {
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(255, 105, 180)
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val nativeCanvas = drawContext.canvas.nativeCanvas


        detectedText.forEach { text ->
            val x = text.x.toFloat()
            val y = text.y.toFloat()
            val width = text.width
            val height = text.height

            nativeCanvas.drawText(text.content, text.x.toFloat(), text.y.toFloat(), paint)

            drawLine(
                color = Color.Green,
                start = Offset(x, y),
                end = Offset(x + width, y)
            )

            drawLine(
                color = Color.Green,
                start = Offset(x, y + height),
                end = Offset(x + width, y + height)
            )

            drawLine(
                color = Color.Green,
                start = Offset(x, y),
                end = Offset(x, y + height)
            )

            drawLine(
                color = Color.Green,
                start = Offset(x + width, y),
                end = Offset(x + width, y + height)
            )
        }
    }
}

@Composable
fun MatImage(mat: Mat?) {
    if (mat == null || mat.empty()) {
        return
    }

    val grayBitmap = remember(mat) {
        grayMatToBitmap(mat)
    }

    Image(
        bitmap = grayBitmap.asImageBitmap(),
        contentDescription = "Obraz z OpenCV w skali szarości",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImage(vm: ViewModel, imageProxy: ImageProxy) {
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
                    detectedText = newDetectedText
                }

                Log.d("MLKit", "Rozpoznany tekst: ${visionText.text}")
            }
            .addOnFailureListener { exception ->
                Log.e("MLKit", "Błąd rozpoznawania tekstu", exception)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }


        val grayMat = imageProxyToGrayMat(imageProxy)

        try {
            val horizontalLines = detectHorizontalLines(grayMat)
            Log.d("OpenCV", "Wykryte linie poziome: ${horizontalLines.joinToString()}")

            // Tworzymy osobną kopię, którą może przechowywać ViewModel.
            val displayMat = grayMat.clone()

            Handler(Looper.getMainLooper()).post {
                vm.updateGrayImage(displayMat)
            }
        } finally {
            grayMat.release()
        }

    } catch (e: Exception) {
        Log.e("CameraAnalysis", "Błąd przetwarzania klatki", e)
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

@Composable
fun GrayImagePreview(bitmap: Bitmap?) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Podgląd obrazu w skali szarości",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Fit
        )
    }
}

private fun imageProxyToGrayMat(imageProxy: ImageProxy): Mat {
    require(imageProxy.format == ImageFormat.YUV_420_888) {
        "Oczekiwano obrazu YUV_420_888"
    }

    val plane = imageProxy.planes[0]
    val buffer = plane.buffer.duplicate()

    val width = imageProxy.width
    val height = imageProxy.height
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride

    val pixels = ByteArray(width * height)

    for (y in 0 until height) {
        val rowStart = y * rowStride

        for (x in 0 until width) {
            val index = rowStart + x * pixelStride

            if (index >= buffer.limit()) {
                throw IllegalStateException("Nieprawidłowy indeks płaszczyzny Y: $index, " + "limit: ${buffer.limit()}")
            }

            pixels[y * width + x] = buffer.get(index)
        }
    }

    val gray = Mat(height, width, CvType.CV_8UC1)

    gray.put(0, 0, pixels)

    return gray
}

private fun grayMatToBitmap(grayMat: Mat): Bitmap {
    val bitmap = createBitmap(grayMat.cols(), grayMat.rows())
    val rgbaMat = Mat()

    try {
        Imgproc.cvtColor(grayMat, rgbaMat, Imgproc.COLOR_GRAY2RGBA)
        Utils.matToBitmap(rgbaMat, bitmap)
    } finally {
        rgbaMat.release()
    }

    return bitmap
}

private fun groupNearbyRows(rows: List<Int>, maxGap: Int): List<Int> {
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