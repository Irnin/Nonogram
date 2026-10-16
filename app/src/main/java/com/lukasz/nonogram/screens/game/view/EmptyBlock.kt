package com.lukasz.nonogram.screens.game.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lukasz.nonogram.screens.game.GameController

@Composable
fun EmptyBlock(controller: GameController) {

    val rowHeaderWidth = controller.rowHeaderWidth
    val columnHeaderHeight = controller.columnHeaderHeight

    Box(
        modifier = Modifier
            .width(rowHeaderWidth)
            .height(columnHeaderHeight)
            .border(1.dp, Color.Black)

    )
}