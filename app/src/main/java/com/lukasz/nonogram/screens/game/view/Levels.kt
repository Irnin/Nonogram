package com.lukasz.nonogram.screens.game.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.lukasz.nonogram.screens.Screen
import com.lukasz.nonogram.screens.game.GameController

@Composable
fun Levels(controller: GameController, navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp)
    ) {

        Text("Levels", fontSize = 50.sp, modifier = Modifier.padding(40.dp))

        Spacer(Modifier.height(50.dp))

        Button(
            onClick = {
                val rowHeaders = listOf(
                    listOf(1, 1, 1),
                    listOf(1, 1, 1),
                    listOf(1, 1, 1),
                    listOf(3),
                    listOf(1)
                )
                val columnHeaders = listOf(
                    listOf(3),
                    listOf(1),
                    listOf(5),
                    listOf(1),
                    listOf(3)
                )

                controller.setBoard(rowHeaders, columnHeaders)
                navController.navigate(Screen.GameScreen.route)
            },
            Modifier
                .padding(horizontal = 40.dp, vertical = 5.dp)
                .fillMaxWidth()
        ) {
            Text("Lv 1")
        }

        Button(
            onClick = {
                val rowHeaders = listOf(
                    listOf(1, 1),
                    listOf(1, 1),
                    listOf(10),
                    listOf(1, 2, 2),
                    listOf(3, 1),
                    listOf(1, 1, 1),
                    listOf(3, 1),
                    listOf(4, 2),
                    listOf(10),
                    listOf(1, 1)
                )
                val columnHeaders = listOf(
                    listOf(7),
                    listOf(1, 1, 4),
                    listOf(1, 7),
                    listOf(3, 2),
                    listOf(1, 1),
                    listOf(1, 1),
                    listOf(2, 1),
                    listOf(1, 1, 1),
                    listOf(2, 3),
                    listOf(7)
                )

                controller.setBoard(rowHeaders, columnHeaders)
                navController.navigate(Screen.GameScreen.route)
            },
            Modifier
                .padding(horizontal = 40.dp, vertical = 5.dp)
                .fillMaxWidth()
        ) {
            Text("Lv 2")
        }


    }
}