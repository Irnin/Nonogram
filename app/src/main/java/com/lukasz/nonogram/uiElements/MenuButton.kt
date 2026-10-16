package com.lukasz.nonogram.uiElements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.lukasz.nonogram.screens.Screen

@Composable
fun MenuButton(navController: NavController, screen: Screen) {
    Button(
        onClick = { navController.navigate(screen.route) },
        Modifier
            .padding(horizontal = 40.dp, vertical = 5.dp)
            .fillMaxWidth()
    ) {
        Text(screen.title)
    }
}