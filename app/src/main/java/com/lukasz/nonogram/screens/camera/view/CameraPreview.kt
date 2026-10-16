package com.lukasz.nonogram.screens.camera.view

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lukasz.nonogram.screens.camera.CameraController
import java.util.concurrent.Executors

@Composable
fun CameraPreview(cameraController: CameraController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = Executors.newSingleThreadExecutor()

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
                    cameraController.processImage(imageProxy)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)

            }, ContextCompat.getMainExecutor(context))
        }
    },
        modifier = Modifier.fillMaxSize())
}