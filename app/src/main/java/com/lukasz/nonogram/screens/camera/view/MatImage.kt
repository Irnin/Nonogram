package com.lukasz.nonogram.screens.camera.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.lukasz.nonogram.screens.camera.CameraController

@Composable
fun MatImage(controller: CameraController) {
    val mat = controller.grayImage

    if (mat == null || mat.empty()) {
        return
    }

    val grayBitmap = remember(mat) {
        controller.grayMatToBitmap(mat)
    }

    Image(
        bitmap = grayBitmap.asImageBitmap(),
        contentDescription = "Obraz z OpenCV w skali szarości",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}