package com.lukasz.nonogram

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import org.opencv.core.Mat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class ViewModel: ViewModel() {

    private val model = Model()
    var grayImage by mutableStateOf<Mat?>(null)
        private set

    fun updateGrayImage(mat: Mat) {
        grayImage?.release()
        grayImage = mat
    }

    override fun onCleared() {
        grayImage?.release()
        grayImage = null
    }



    fun getBoard(): List<List<Fields>>{
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

        if(field == Fields.EMPTY) {
            setField(row, column, Fields.FULL)
        }
        else {
            setField(row, column, Fields.EMPTY)
        }
    }
}