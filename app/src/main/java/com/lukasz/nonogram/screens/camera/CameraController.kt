package com.lukasz.nonogram.screens.camera

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.lukasz.nonogram.extensions.getGrayMap
import com.lukasz.nonogram.screens.camera.model.Image.ImageType
import com.lukasz.nonogram.screens.camera.tools.GridDetector
import com.lukasz.nonogram.screens.camera.view.ImageCreator
import org.opencv.core.Mat

class CameraController : ViewModel() {
    private val model = CameraModel()
    private val gridDetector = GridDetector(model)
    private val imageCreator = ImageCreator()

    override fun onCleared() {
        model.cleanModel()
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        var grayImage: Mat? = null

        try {
            grayImage = imageProxy.getGrayMap().clone()

            gridDetector.initialProcess(grayImage)

            grayImage = null

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            grayImage?.release()
            imageProxy.close()
        }
    }

    // Getters
    fun getImage(type: ImageType): Mat {
        return model.getImage(type)
    }
}