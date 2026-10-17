package com.lukasz.nonogram.screens.camera.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lukasz.nonogram.screens.camera.CameraController
import com.lukasz.nonogram.uiElements.CameraDebugFrame

@Composable
fun CameraDebug(controller: CameraController) {
    val color = Color(0xFFFF69B4)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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
                Box() {
                    CameraPreview(controller)
                    Text(
                        "Cam",
                        fontSize = 20.sp,
                        color = color,
                        modifier = Modifier.padding(40.dp)
                    )
                }

            }

            CameraDebugFrame(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MatImage(controller.grayImage)
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
                MatImage(controller.edgeImage)
            }

            CameraDebugFrame(
                controller = controller,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                MatImage(controller.linesImage)
            }
        }
    }
}