package com.lukasz.nonogram.screens.camera.tools

import com.lukasz.nonogram.screens.camera.model.Line
import com.lukasz.nonogram.screens.camera.model.Orientation
import kotlin.math.abs

// Dodaje tutaj linie, aby uzyskać stabilną linie
class LineBuffer {
    val threshold: Int = 10
    val horizontalBuffer = mutableMapOf<Double, StatisticLine>()

    fun addLines(lines: MutableList<Line>) {
        for (line in lines) {
            addLine(line)
        }
    }

    fun addLine(line: Line) {
        cleanUpBuffer()

        if (line.orientation == Orientation.HORIZONTAL) {
            addHorizontalLine(line)
        } else {
            addVerticalLine(line)
        }
    }

    fun cleanUpBuffer() {
        for (bufferRecord in horizontalBuffer) {
            val statisticLine = bufferRecord.value

            if (!statisticLine.isAlive()) {
                horizontalBuffer.remove(bufferRecord.key)
            }
        }
    }

    private fun addHorizontalLine(line: Line) {

        for (bufferElement in horizontalBuffer) {
            if (abs(line.start.y - bufferElement.key) < threshold) {
                val statisticLine = horizontalBuffer[bufferElement.key]!!
                statisticLine.add(line)
                return
            }
        }

        val statisticLine = StatisticLine()
        statisticLine.add(line)
        horizontalBuffer[line.start.y] = statisticLine
    }

    private fun addVerticalLine(line: Line) {

    }

    fun getHorizontalLines(): MutableList<Line> {
        val list = mutableListOf<Line>()
        for (bufferedLines in horizontalBuffer) {
            list.add(bufferedLines.value.getLine())
        }

        return list
    }

}