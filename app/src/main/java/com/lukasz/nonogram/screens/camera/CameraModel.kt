package com.lukasz.nonogram.screens.camera

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CameraModel {

    var detectedText by mutableStateOf<List<DetectedText>>(emptyList())
}