package com.lukasz.nonogram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import kotlin.getValue

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<ViewModel>()

    private val cellSize = 40.dp
    private val rowHeaderWidth = 80.dp
    private val columnHeaderHeight = 80.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center) {

                Board()
            }
        }
    }

    @Composable
    fun BoardField(
        row: Int,
        column: Int,
        field: Fields
    ) {
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
                    viewModel.doAction(row, column)
                }
        )
    }

    @Composable
    fun ColumnHeader(number: Int) {

        val header = viewModel.getColumnHeader(number)

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
    fun RowHeader(number: Int) {
        val header = viewModel.getRowHeader(number)

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
    fun Board() {
        val board = viewModel.getBoard()

        Column {

            Row {
                EmptyBlock()

                board[0].indices.forEach { column ->
                    ColumnHeader(column)
                }
            }

            board.forEachIndexed { rowIndex, row ->

                Row {
                    RowHeader(rowIndex)

                    row.forEachIndexed { columnIndex, field ->
                        BoardField(
                            row = rowIndex,
                            column = columnIndex,
                            field = field
                        )
                    }
                }
            }
        }
    }
}