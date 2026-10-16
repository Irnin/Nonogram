package com.lukasz.nonogram.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lukasz.nonogram.Fields
import com.lukasz.nonogram.ViewModel
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

private val cellSize = 40.dp

private val rowHeaderWidth = 80.dp
private val columnHeaderHeight = 80.dp

@Composable
fun BoardField(vm: ViewModel, row: Int, column: Int, field: Fields) {
    Box(
        modifier = Modifier
            .size(cellSize)
            .background(
                if (field == Fields.FULL)
                    Color.Black
                else
                    Color.White
            )
            .border(1.dp, Color.Black)
            .clickable {
                vm.doAction(row, column)
            }
    )
}

@Composable
fun ColumnHeader(vm: ViewModel, number: Int) {

    val header = vm.getColumnHeader(number)

    Column(
        modifier = Modifier
            .width(cellSize)
            .height(columnHeaderHeight)
            .border(1.dp, Color.Black),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
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

@Composable
fun RowHeader(vm: ViewModel, number: Int) {
    val header = vm.getRowHeader(number)

    Row(
        modifier = Modifier
            .width(rowHeaderWidth)
            .height(cellSize)
            .border(1.dp, Color.Black),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
    ) {
        header.forEach {
            Text(
                text = it.toString(),
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

@Composable
fun EmptyBlock() {
    Box(
        modifier = Modifier
            .width(rowHeaderWidth)
            .height(columnHeaderHeight)
            .border(1.dp, Color.Black)

    )
}

@Composable
fun Board(vm: ViewModel) {
    val board = vm.getBoard()

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 4f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
        ,
        contentAlignment = Alignment.Center

    ) {
        Column(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offsetX
                    translationY = offsetY
                }

        ) {
            Row {
                EmptyBlock()

                board[0].indices.forEach { column ->
                    ColumnHeader(vm, column)
                }
            }

            board.forEachIndexed { rowIndex, row ->
                Row {
                    RowHeader(vm, rowIndex)

                    row.forEachIndexed { columnIndex, field ->
                        BoardField(vm, rowIndex, columnIndex, field)
                    }
                }
            }
        }
    }
}