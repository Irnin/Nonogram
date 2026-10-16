package com.lukasz.nonogram.screens.game.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lukasz.nonogram.screens.game.GameController

@Composable
fun ColumnHeader(controller: GameController, number: Int) {

    val header = controller.getColumnHeader(number)
    val columnHeaderHeight = controller.columnHeaderHeight
    val cellSize = controller.cellSize

    Column(
        modifier = Modifier
            .width(cellSize)
            .height(columnHeaderHeight)
            .border(1.dp, Color.Black),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        header.forEach {
            Text(
                text = it.toString(),
                modifier = Modifier.padding(vertical = 1.dp)
            )
        }
    }
}