package com.lukasz.nonogram.screens.camera.view

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    }
}