package com.lukasz.nonogram.screens.camera.view.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lukasz.nonogram.screens.camera.CameraController
import com.lukasz.nonogram.screens.camera.model.Image.ImageType
import com.lukasz.nonogram.screens.camera.view.CameraPreview
import com.lukasz.nonogram.screens.camera.view.MatImage
import com.lukasz.nonogram.uiElements.CameraDebugFrame


@Composable
fun CameraDebug(controller: CameraController) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CameraPreview(controller, false)

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CameraDebugFrame(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MatImage(controller.getImage(ImageType.GLOBAL_LINES))
            }

            CameraDebugFrame(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MatImage(controller.getImage(ImageType.BOARD))
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            CameraDebugFrame(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MatImage(controller.getImage(ImageType.DRAFT))
            }

            CameraDebugFrame(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MatImage(controller.getImage(ImageType.GLOBAL_EDGES))
            }
        }
    }
}