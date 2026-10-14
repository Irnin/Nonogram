package com.lukasz.nonogram

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

import kotlin.getValue

class MainActivity : ComponentActivity() {

    private var detectedText = mutableListOf<DetectedText>()

    private val viewModel by viewModels<ViewModel>()

    private val cellSize = 40.dp
    private val rowHeaderWidth = 80.dp
    private val columnHeaderHeight = 80.dp

    private var recognizedText by mutableStateOf("Skieruj kamerę na tekst")
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize()) {

                CameraPreview()

//                Text(
//                    text = recognizedText,
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .fillMaxWidth()
//                        .background(Color.Black.copy(alpha = 0.7f))
//                        .padding(16.dp),
//                    color = Color.White,
//                    fontSize = 18.sp
//                )

                MyCanvas()
            }
        }
    }

    private fun processImage(imageProxy: ImageProxy) {
        detectedText.clear()

        val mediaImage = imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->

                textRecognizer.process(image)
                    .addOnSuccessListener { visionText ->

                        for (block in visionText.textBlocks) {

                            val text = block.text
                            val rect = block.boundingBox

                            if (rect != null) {
                                val x = rect.left
                                val y = rect.top
                                val width = rect.width()
                                val height = rect.height()

                                println("Tekst: $text")
                                println("X: $x")
                                println("Y: $y")
                                println("Szerokość: $width")
                                println("Wysokość: $height")
                                println("--------------------")

                                detectedText.add(DetectedText(x, y, width, height, text))
                            }
                        }

                        recognizedText = visionText.text.ifBlank { "Nie wykryto tekstu" }
                    }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    @Composable
    fun MyCanvas() {
        Canvas(Modifier.fillMaxSize()) {

            detectedText.forEach { text ->
                val x = text.x.toFloat()
                val y = text.y.toFloat()
                val width = text.width
                val height = text.height

                val cellSize = 50f
                val nativeCanvas = drawContext.canvas.nativeCanvas

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.YELLOW
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                nativeCanvas.drawText(
                    text.content,
                    text.x.toFloat(),
                    text.y.toFloat(),
                    paint
                )

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
    fun CameraPreview() {
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
                        processImage(imageProxy)
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
    fun BoardField(row: Int, column: Int, field: Fields) {
        Box(
            modifier = Modifier
                .size(cellSize)
                .background(
                    if (field == Fields.FULL)
                        Color.Black
                    else
                        Color.White
                )
                .border(1.dp, Color.Black)
                .clickable {
                    viewModel.doAction(row, column)
                }
        )
    }

    @Composable
    fun ColumnHeader(number: Int) {

        val header = viewModel.getColumnHeader(number)

        Column(
            modifier = Modifier
                .width(cellSize)
                .height(columnHeaderHeight)
                .border(1.dp, Color.Black),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            header.forEach {
                Text(
                    text = it.toString(),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }

    @Composable
    fun RowHeader(number: Int) {
        val header = viewModel.getRowHeader(number)

        Row(
            modifier = Modifier
                .width(rowHeaderWidth)
                .height(cellSize)
                .border(1.dp, Color.Black),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
        ) {
            header.forEach {
                Text(
                    text = it.toString(),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }

    @Composable
    fun EmptyBlock() {
        Box(
            modifier = Modifier
                .width(rowHeaderWidth)
                .height(columnHeaderHeight)
                .border(1.dp, Color.Black)

        )
    }

    @Composable
    fun Board() {
        val board = viewModel.getBoard()

        Column {

            Row {
                EmptyBlock()

                board[0].indices.forEach { column ->
                    ColumnHeader(column)
                }
            }

            board.forEachIndexed { rowIndex, row ->

                Row {
                    RowHeader(rowIndex)

                    row.forEachIndexed { columnIndex, field ->
                        BoardField(
                            row = rowIndex,
                            column = columnIndex,
                            field = field
                        )
                    }
                }
            }
        }
    }
}