package com.lukasz.nonogram.screens.camera.tools

import com.lukasz.nonogram.screens.camera.model.Line
import com.lukasz.nonogram.screens.camera.model.Orientation
import org.opencv.core.Point
import kotlin.math.abs

class StatisticLine(
    val bufferSize: Int = 100
) {
    val threshold: Int = 10
    var index = 0
    lateinit var orientation: Orientation
    var lastRefresh = System.currentTimeMillis()

    private val startXPositions = mutableMapOf<Double, Int>()
    private val startYPositions = mutableMapOf<Double, Int>()
    private val endXPositions = mutableMapOf<Double, Int>()
    private val endYPositions = mutableMapOf<Double, Int>()

    fun add(line: Line) {
        lastRefresh = System.currentTimeMillis()


        if (index == bufferSize) {
            startXPositions.clear()
            startYPositions.clear()
            endXPositions.clear()
            endYPositions.clear()

            index = 0
        }

        addToStatistic(startXPositions, line.start.x)
        addToStatistic(startYPositions, line.start.y)
        addToStatistic(endXPositions, line.end.x)
        addToStatistic(endYPositions, line.end.y)

        index += 1
        orientation = line.orientation
    }

    private fun addToStatistic(map: MutableMap<Double, Int>, value: Double) {

        for (entity in map) {
            if (abs(entity.key - value) < threshold) {
                entity.setValue(entity.value + 1)
                return
            }
        }

        map[value] = 1
    }

    fun getLine(): Line {
        val startX = startXPositions.entries.maxByOrNull { it.value }?.key

        val startY = startYPositions.entries.maxByOrNull { it.value }?.key

        val endX = endXPositions.entries.maxByOrNull { it.value }?.key

        val endY = endYPositions.entries.maxByOrNull { it.value }?.key

        val start = Point(startX!!, startY!!)
        val end = Point(endX!!, endY!!)

        return Line(start, end, orientation)
    }

    fun isAlive(): Boolean {
        return System.currentTimeMillis() - lastRefresh < 200
    }
}