package com.lukasz.nonogram.screens.camera

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import com.lukasz.nonogram.extensions.getGrayMap
import com.lukasz.nonogram.screens.camera.model.Image.ImageType
import com.lukasz.nonogram.screens.camera.tools.ImageProcessor
import com.lukasz.nonogram.screens.camera.view.ImageCreator
import org.opencv.core.Mat

class CameraController : ViewModel() {
    private val model = CameraModel()
    private val imageProcessor = ImageProcessor()
    val imageCreator = ImageCreator()

    override fun onCleared() {
        model.cleanModel()
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImage(imageProxy: ImageProxy) {
        var sourceImage: Mat? = null
        var boardImage: Mat? = null

        try {
            sourceImage = imageProxy.getGrayMap()

            val processedGlobalImage = imageProcessor.process(sourceImage)

            boardImage = imageProcessor.extractGameBoard(sourceImage, processedGlobalImage.lines)
            val processedBoardImage = imageProcessor.process(boardImage)

            val hints = imageProcessor.extractHints(processedBoardImage)
            val columnImage = hints.getRow(1)
//            val columnImage = hints.getColumn(7)

//            imageProcessor.extractTextBlocks(columnImage) { textImage ->
//                if (textImage == null) {
////                    Log.e("CameraAnalysis", "OCR nie zwrócił obrazu")
//                    return@extractTextBlocks
//                }
//
//                Handler(Looper.getMainLooper()).post {
//                    try {
//                        model.updateImage(ImageType.DRAFT, textImage)
//                    } catch (e: Exception) {
//                    }
//                }
//            }

//            columnImage.release()

            Handler(Looper.getMainLooper()).post {
                try {
                    model.updateImage(ImageType.GLOBAL_LINES, processedGlobalImage.linesImage)
                    model.updateImage(ImageType.GLOBAL_EDGES, processedGlobalImage.edgesImage)
                    model.updateImage(ImageType.BOARD, processedBoardImage.linesImage)
                    model.updateImage(ImageType.DRAFT, columnImage)
                } catch (e: Exception) {
                    Log.e("CameraAnalysis", "Cannot update model", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CameraAnalysis", "Cannot process frame", e)
        } finally {
            sourceImage?.release()
            boardImage?.release()
            imageProxy.close()
        }
    }

    // Getters
    fun getImage(type: ImageType): Mat {
        return model.getImage(type)
    }
}