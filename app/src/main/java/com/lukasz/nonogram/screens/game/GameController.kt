package com.lukasz.nonogram.screens.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel

class GameController() : ViewModel() {

    val cellSize = 40.dp
    val rowHeaderWidth = 80.dp
    val columnHeaderHeight = 80.dp

    var isSolved by mutableStateOf(false)
        private set


    private val model = GameModel()

    fun setBoard(
        rowHeaders: List<List<Int>>,
        columnHeaders: List<List<Int>>
    ) {
        model.rowHeaders = rowHeaders
        model.columnHeaders = columnHeaders

        model.rows = rowHeaders.size
        model.columns = columnHeaders.size

        model.board = MutableList(model.rows) {
            MutableList(model.columns) {
                mutableStateOf(Fields.EMPTY)
            }
        }

        isSolved = false
    }

    fun getBoard(): List<List<Fields>> {
        return model.board.map { row ->
            row.map { fieldState ->
                fieldState.value
            }
        }
    }

    fun getField(row: Int, column: Int): Fields {
        return model.board[row][column].value
    }

    fun setField(row: Int, column: Int, field: Fields) {
        model.updateBoard(row, column, field)
    }

    fun getRowHeader(row: Int): List<Int> {
        return model.rowHeaders[row]
    }

    fun getColumnHeader(column: Int): List<Int> {
        return model.columnHeaders[column]
    }

    fun doAction(row: Int, column: Int) {
        val field = getField(row, column)

        val newField = if (field == Fields.EMPTY) {
            Fields.FULL
        } else {
            Fields.EMPTY
        }

        setField(row, column, newField)

        isSolved = model.isSolved()
    }
}