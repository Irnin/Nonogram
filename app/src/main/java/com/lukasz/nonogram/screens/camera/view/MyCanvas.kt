package com.lukasz.nonogram.screens.camera.view

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import com.lukasz.nonogram.screens.camera.CameraController

@Composable
fun MyCanvas(controller: CameraController) {
    Canvas(Modifier.fillMaxSize()) {
        val paint = Paint().apply {
            color = android.graphics.Color.rgb(255, 105, 180)
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val nativeCanvas = drawContext.canvas.nativeCanvas


        controller.detectedText.forEach { text ->
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