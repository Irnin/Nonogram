package com.lukasz.nonogram.screens.camera

import com.lukasz.nonogram.screens.camera.model.Image
import com.lukasz.nonogram.screens.camera.model.Image.ImageType
import org.opencv.core.Mat

class CameraModel {
    val images = mutableMapOf<ImageType, Image>()

    constructor() {
        for (type in ImageType.entries) {
            images[type] = Image()
        }
    }

    fun getImage(type: ImageType): Mat {
        return images[type]?.get() ?: Mat()
    }

    fun updateImage(type: ImageType, image: Mat?) {
        images[type]?.updateImage(image)
    }


    fun cleanModel() {
        for (image in images.values) {
            image.clean()
        }
    }
}