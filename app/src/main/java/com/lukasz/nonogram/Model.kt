package com.lukasz.nonogram

import androidx.compose.runtime.mutableStateOf

class Model {

    val columns = 5
    val rows = 6

    val board = MutableList(rows) {
        MutableList(columns) {
            mutableStateOf(Fields.EMPTY)
        }
    }

    val columnHeaders = List(columns) {
        mutableListOf<Int>()
    }

    val rowHeaders = List(rows) {
        mutableListOf<Int>()
    }

    fun updateBoard(row: Int, column: Int, field: Fields) {
        board[row][column].value = field
    }

    constructor() {
        updateBoard(0, 0, Fields.FULL)
        updateBoard(0, 2, Fields.FULL)
        updateBoard(0, 4, Fields.FULL)

        updateBoard(1, 0, Fields.FULL)
        updateBoard(1, 2, Fields.FULL)
        updateBoard(1, 4, Fields.FULL)

        updateBoard(2, 0, Fields.FULL)
        updateBoard(2, 2, Fields.FULL)
        updateBoard(2, 4, Fields.FULL)

        updateBoard(3, 1, Fields.FULL)
        updateBoard(3, 2, Fields.FULL)
        updateBoard(3, 3, Fields.FULL)

        updateBoard(4, 2, Fields.FULL)

        calculateRowHeader()
        calculateColumnHeader()
    }

    fun calculateRowHeader() {
        var detectedBlock = false
        var counter: Int

        for (row in 0 until rows) {
            println("Checking row $row")
            counter = 0;

            for (column in 0 until columns) {

                val field = board[row][column]

                print("checking column $column: ${field.value}")

                if (field.value == Fields.FULL) {
                    counter += 1
                    detectedBlock = true
                } else {
                    if (detectedBlock) {
                        print(" Adding counter: $counter")
                        rowHeaders[row].add(counter)
                        counter = 0
                        detectedBlock = false
                    }
                }
                println()
            }

            if (detectedBlock) {
                print(" Adding counter: $counter")
                rowHeaders[row].add(counter)
                counter = 0
                detectedBlock = false
            }
        }
    }

    fun calculateColumnHeader() {
        var detectedBlock = false
        var counter: Int

        for (column in 0 until columns) {
            counter = 0;

            for (row in 0 until rows) {

                val field = board[row][column]

                if (field.value == Fields.FULL) {
                    counter += 1
                    detectedBlock = true
                } else {
                    if (detectedBlock) {
                        print(" Adding counter: $counter")
                        columnHeaders[column].add(counter)
                        counter = 0
                        detectedBlock = false
                    }
                }
                println()
            }

            if (detectedBlock) {
                print(" Adding counter: $counter")
                columnHeaders[column].add(counter)
                counter = 0
                detectedBlock = false
            }
        }
    }
}