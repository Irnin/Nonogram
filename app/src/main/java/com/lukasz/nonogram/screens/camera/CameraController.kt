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
import com.lukasz.nonogram.extensions.getGrayMap
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class CameraController : ViewModel() {

    private val model = CameraModel()

    var grayImage by mutableStateOf<Mat?>(null)
        private set

    var edgeImage by mutableStateOf<Mat?>(null)
        private set

    fun updateGrayImage(mat: Mat) {
        grayImage?.release()
        grayImage = mat
    }

    fun updateEdgeImage(mat: Mat) {
        edgeImage?.release()
        edgeImage = mat
    }

    override fun onCleared() {
        grayImage?.release()
        grayImage = null

        edgeImage?.release()
        edgeImage = null
    }

    fun calculateCanny(grayMat: Mat): Mat {
        val edges = Mat()

        Imgproc.Canny(
            grayMat,
            edges,
            100.0,
            200.0
        )
        return edges
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        var grayMat: Mat? = null
        var edgeMat: Mat? = null

        try {
            grayMat = imageProxy.getGrayMap().clone()
            edgeMat = calculateCanny(grayMat)

            val grayResult = grayMat
            val edgeResult = edgeMat

            Handler(Looper.getMainLooper()).post {
                updateGrayImage(grayResult)
                updateEdgeImage(edgeResult)
            }

            grayMat = null
            edgeMat = null

        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            grayMat?.release()
            edgeMat?.release()
            imageProxy.close()
        }
    }
}