package com.lukasz.nonogram.screens.game

import androidx.compose.runtime.mutableStateOf

class GameModel {
    var rows = 5
    var columns = 5

    var rowHeaders = listOf(
        listOf(1, 1, 1),
        listOf(1, 1, 1),
        listOf(1, 1, 1),
        listOf(3),
        listOf(1)
    )
    var columnHeaders = listOf(
        listOf(3),
        listOf(1),
        listOf(5),
        listOf(1),
        listOf(3)
    )

    var board = MutableList(rows) {
        MutableList(columns) {
            mutableStateOf(Fields.EMPTY)
        }
    }

    fun updateBoard(row: Int, column: Int, field: Fields) {
        board[row][column].value = field
    }

    fun calculateRowHeader(row: Int): List<Int> {
        return calculateHeader(columns) { column ->
            board[row][column].value == Fields.FULL
        }
    }

    fun calculateColumnHeader(column: Int): List<Int> {
        return calculateHeader(rows) { row ->
            board[row][column].value == Fields.FULL
        }
    }

    private fun calculateHeader(
        size: Int,
        isFull: (Int) -> Boolean
    ): List<Int> {
        val header = mutableListOf<Int>()
        var counter = 0

        for (index in 0 until size) {
            if (isFull(index)) {
                counter++
            } else if (counter > 0) {
                header.add(counter)
                counter = 0
            }
        }

        if (counter > 0) {
            header.add(counter)
        }

        return header
    }

    fun isSolved(): Boolean {
        val rowsMatch = (0 until rows).all { row ->
            calculateRowHeader(row) == rowHeaders[row]
        }

        val columnsMatch = (0 until columns).all { column ->
            calculateColumnHeader(column) == columnHeaders[column]
        }

        return rowsMatch && columnsMatch
    }
}