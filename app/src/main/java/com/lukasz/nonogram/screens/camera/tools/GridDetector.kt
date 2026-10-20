package com.lukasz.nonogram.screens.camera.tools

import Constants
import com.lukasz.nonogram.extensions.Util.intersection
import com.lukasz.nonogram.screens.camera.CameraModel
import com.lukasz.nonogram.screens.camera.model.BoardRegion
import com.lukasz.nonogram.screens.camera.model.Image.ImageType
import com.lukasz.nonogram.screens.camera.model.Line
import com.lukasz.nonogram.screens.camera.model.Line.Orientation
import com.lukasz.nonogram.screens.camera.view.ImageCreator
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

class GridDetector(val model: CameraModel) {

    //val lineBuffer = LineBuffer()
    val imageCreator = ImageCreator()

    fun initialProcess(grayImage: Mat) {
        model.updateImage(ImageType.GLOBAL_GRAY, grayImage)

        val edgeImage = imageCreator.createEdgesImage(grayImage)
        model.updateImage(ImageType.GLOBAL_EDGES, edgeImage)

        val corners = calculateCorners(grayImage)
        val cornersImage = imageCreator.createCornersImage(grayImage, corners)
        model.updateImage(ImageType.GLOBAL_CORNERS, cornersImage)

        val globalLines = detectLineSegments()

        val processedLines = processLines(globalLines)
        val horizontalLines = processedLines.first
        val verticalLines = processedLines.second

        val globalImageLines = imageCreator.createLinesImage(
            model.getImage(ImageType.GLOBAL_GRAY),
            horizontalLines,
            verticalLines
        )

        model.updateImage(ImageType.GLOBAL_LINES, globalImageLines)

        val boardRegion = localizeGameBoard(horizontalLines, verticalLines)
        val boardImage = extractGameBoard(model.getImage(ImageType.GLOBAL_GRAY), boardRegion)

        processBoardPicture(boardImage)
    }

    fun processBoardPicture(boardImage: Mat) {

        model.updateImage(ImageType.DRAFT, boardImage)
    }


    private fun calculateCorners(sourceImage: Mat): List<Point> {

        if (sourceImage.empty()) return emptyList()

        val corners = MatOfPoint()

        val points = mutableListOf<Point>()

        Imgproc.goodFeaturesToTrack(
            sourceImage,
            corners,
            100,
            0.01,
            10.0
        )

        for (point in corners.toArray()) {
            points.add(point)
        }

        return points
    }

    private fun detectLineSegments(): Mat {
        val edgeImage = model.getImage(ImageType.GLOBAL_EDGES)

        if (edgeImage.empty()) return Mat()

        val grayEdges = Mat()

        if (edgeImage.channels() == 1) {
            edgeImage.copyTo(grayEdges)
        } else {
            Imgproc.cvtColor(edgeImage, grayEdges, Imgproc.COLOR_BGR2GRAY)
        }

        val lines = Mat()

        Imgproc.HoughLinesP(
            edgeImage,
            lines,
            1.0,
            Math.PI / 180.0,
            80,
            50.0,
            10.0
        )

        return lines
    }

    private fun processLines(lines: Mat): Pair<List<Line>, List<Line>> {
        val categorizedLines = categorizeLinesByOrientation(lines)

        val horizontalLines = processLines(Orientation.HORIZONTAL, categorizedLines.first)
        val verticalLines = processLines(Orientation.VERTICAL, categorizedLines.second)

        return Pair(horizontalLines, verticalLines)
    }

    private fun categorizeLinesByOrientation(lines: Mat): Pair<List<Line>, List<Line>> {
        val angleTolerance = 20.0

        val horizontalLines = mutableListOf<Line>()
        val verticalLines = mutableListOf<Line>()

        for (i in 0 until lines.rows()) {

            val line = lines.get(i, 0) ?: continue

            if (line.size < 4) continue

            val x1 = line[0]
            val y1 = line[1]
            val x2 = line[2]
            val y2 = line[3]

            val dx = x2 - x1
            val dy = y2 - y1

            val angle = Math.toDegrees(atan2(dy, dx))

            when {
                abs(angle) <= angleTolerance || abs(angle) >= 180.0 - angleTolerance -> {
                    val detectedLine = Line(Point(x1, y1), Point(x2, y2), Orientation.HORIZONTAL)
                    horizontalLines.add(detectedLine)
                }

                abs(abs(angle) - 90.0) <= angleTolerance -> {
                    val detectedLine = Line(Point(x1, y1), Point(x2, y2), Orientation.VERTICAL)
                    verticalLines.add(detectedLine)
                }
            }
        }

        return Pair(horizontalLines, verticalLines)
    }

    private fun processLines(
        orientation: Orientation,
        lines: List<Line>,
        threshold: Int = 10
    ): List<Line> {

        val sortedLines = when (orientation) {
            Orientation.HORIZONTAL -> lines.sortedBy { it.start.y }
            Orientation.VERTICAL -> lines.sortedBy { it.start.x }
        }

        if (sortedLines.isEmpty()) return emptyList()

        val linesGroup = mutableListOf<Line>()
        val normalizedLines = mutableListOf<Line>()

        var coordinate = when (orientation) {
            Orientation.HORIZONTAL -> sortedLines[0].start.y
            Orientation.VERTICAL -> sortedLines[0].start.x
        }

        for (line in sortedLines) {
            val currentCoordinate = when (orientation) {
                Orientation.HORIZONTAL -> line.start.y
                Orientation.VERTICAL -> line.start.x
            }

            if (abs(currentCoordinate - coordinate) < threshold) {
                linesGroup.add(line)
            } else {
                normalizedLines.add(mergeLines(linesGroup, orientation))

                linesGroup.clear()
                linesGroup.add(line)
                coordinate = currentCoordinate
            }
        }

        if (linesGroup.isNotEmpty()) {
            normalizedLines.add(mergeLines(linesGroup, orientation))
        }

        return normalizedLines
    }

    private fun mergeLines(lines: List<Line>, orientation: Orientation): Line {
        var start = lines[0].start
        var end = lines[0].end

        for (current in lines) {
            when (orientation) {
                Orientation.HORIZONTAL -> {
                    if (current.start.x < start.x) {
                        start = current.start
                    }

                    if (current.end.x > end.x) {
                        end = current.end
                    }
                }

                Orientation.VERTICAL -> {
                    if (current.start.y < start.y) {
                        start = current.start
                    }

                    if (current.end.y > end.y) {
                        end = current.end
                    }
                }
            }
        }

        return Line(start, end, orientation)
    }

    private fun localizeGameBoard(
        horizontalLines: List<Line>,
        verticalLines: List<Line>
    ): BoardRegion {
        val horizontal = horizontalLines.toList()
        val vertical = verticalLines.toList()

        if (horizontal.isEmpty() || vertical.isEmpty()) {
            val point = Point(0.0, 0.0)
            return BoardRegion(point, point, point, point)
        }

        val firstHorizontalLine = horizontal[0]
        val lastHorizontalLine = horizontal[horizontal.size - 1]

        val firstVerticalLine = vertical[0]
        val lastVerticalLine = vertical[vertical.size - 1]

        val offset = 40.0

        val topLeft = intersection(firstHorizontalLine, firstVerticalLine, -offset, -offset)!!
        val topRight = intersection(firstHorizontalLine, lastVerticalLine, offset, -offset)!!
        val bottomLeft = intersection(lastHorizontalLine, firstVerticalLine, offset, offset)!!
        val bottomRight = intersection(lastHorizontalLine, lastVerticalLine, -offset, offset)!!


        return BoardRegion(topLeft, topRight, bottomLeft, bottomRight)
    }

    private fun extractGameBoard(sourceImage: Mat, boardRegion: BoardRegion): Mat {

        if (sourceImage.empty()) {
            return Constants.NO_PICTURE
        }

        val topLeft = boardRegion.topLeft
        val topRight = boardRegion.topRight
        val bottomLeft = boardRegion.bottomLeft
        val bottomRight = boardRegion.bottomRight

        val widthTop = hypot(
            topRight.x - topLeft.x,
            topRight.y - topLeft.y
        )

        val widthBottom = hypot(
            bottomRight.x - bottomLeft.x,
            bottomRight.y - bottomLeft.y
        )

        val outputWidth = max(widthTop, widthBottom).toInt()

        val heightLeft = hypot(
            bottomLeft.x - topLeft.x,
            bottomLeft.y - topLeft.y
        )

        val heightRight = hypot(
            bottomRight.x - topRight.x,
            bottomRight.y - topRight.y
        )

        val outputHeight = max(heightLeft, heightRight).toInt()

        if (outputWidth < 0 && outputHeight < 0) {
            return Constants.NO_PICTURE
        }

        val sourcePoints = MatOfPoint2f(
            topLeft,
            topRight,
            bottomRight,
            bottomLeft
        )

        val destinationPoints = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(outputWidth - 1.0, 0.0),
            Point(outputWidth - 1.0, outputHeight - 1.0),
            Point(0.0, outputHeight - 1.0)
        )

        val transform = Imgproc.getPerspectiveTransform(sourcePoints, destinationPoints)

        val result = Mat()

        Imgproc.warpPerspective(
            sourceImage,
            result,
            transform,
            Size(outputWidth.toDouble(), outputHeight.toDouble())
        )

        sourcePoints.release()
        destinationPoints.release()
        transform.release()

        return result
    }
}


