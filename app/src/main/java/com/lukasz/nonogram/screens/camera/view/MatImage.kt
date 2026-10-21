package com.lukasz.nonogram.screens.camera.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.lukasz.nonogram.extensions.getBitmap
import org.opencv.core.Mat


@Composable
fun MatImage(matImage: Mat?) {
    if (matImage == null) {
        return
    }

    if (matImage.empty()) {
        return
    }

    val grayBitmap = remember(matImage) {
        matImage.getBitmap()
    }

    Image(
        bitmap = grayBitmap.asImageBitmap(),
        contentDescription = "Camera preview",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}