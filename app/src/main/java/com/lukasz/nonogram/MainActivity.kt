package com.lukasz.nonogram

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.lukasz.nonogram.screens.camera.CameraController
import com.lukasz.nonogram.screens.game.GameController
import org.opencv.android.OpenCVLoader

class MainActivity : ComponentActivity() {

    private val gameController by viewModels<GameController>()
    private val cameraController by viewModels<CameraController>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val initialized = OpenCVLoader.initLocal()

        if (!initialized) {
            Log.e("OpenCV", "Nie udało się zainicjalizować OpenCV")
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
        }

        setContent {
            Navigation(gameController, cameraController)
        }
    }
}