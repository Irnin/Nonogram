package com.lukasz.nonogram.screens.camera

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.lukasz.nonogram.extensions.getGrayMap
import com.lukasz.nonogram.screens.camera.model.ImageType
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
        var edgeImage: Mat? = null

        try {
            grayImage = imageProxy.getGrayMap().clone()
            model.updateImage(ImageType.GRAY, grayImage)

            edgeImage = imageCreator.createEdgesImage(grayImage)
            model.updateImage(ImageType.EDGES, edgeImage)

            gridDetector.initialProcess()

            Handler(Looper.getMainLooper()).post {
                model.updateImage(
                    ImageType.LINES,
                    imageCreator.createLinesImage(
                        model.getImage(ImageType.GRAY),
                        model.horizontalLines,
                        model.verticalLines
                    )
                )
            }

            grayImage = null
            edgeImage = null

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            grayImage?.release()
            edgeImage?.release()
            imageProxy.close()
        }
    }

    // Getters

    fun getImage(type: ImageType): Mat {
        return model.getImage(type)
    }
}