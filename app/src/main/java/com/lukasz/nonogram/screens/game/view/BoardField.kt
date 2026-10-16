package com.lukasz.nonogram.screens.game.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lukasz.nonogram.screens.game.Fields
import com.lukasz.nonogram.screens.game.GameController

@Composable
fun BoardField(controller: GameController, row: Int, column: Int, field: Fields) {
    val color = if (field == Fields.FULL) Color.Black else Color.White
    val cellSize = controller.cellSize

    Box(
        modifier = Modifier
            .size(cellSize)
            .background(color)
            .border(1.dp, Color.Black)
            .clickable {
                controller.doAction(row, column)
            }
    )
}