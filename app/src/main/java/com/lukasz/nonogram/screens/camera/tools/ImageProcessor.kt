package com.lukasz.nonogram.screens.camera.tools

import Constants
import android.util.Log
import androidx.core.graphics.createBitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lukasz.nonogram.extensions.Util.intersection
import com.lukasz.nonogram.screens.camera.model.BoardRegion
import com.lukasz.nonogram.screens.camera.model.ExtractedField
import com.lukasz.nonogram.screens.camera.model.Line
import com.lukasz.nonogram.screens.camera.model.Line.Orientation
import com.lukasz.nonogram.screens.camera.model.Lines
import com.lukasz.nonogram.screens.camera.model.ProcessedImage
import com.lukasz.nonogram.screens.camera.view.ImageCreator
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max

class ImageProcessor() {
    val imageCreator = ImageCreator()
    val textRecognizer = TextRecognizer()

    val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    fun process(image: Mat): ProcessedImage {
        if (image.empty()) return ProcessedImage(
            Lines(emptyList(), emptyList()),
            Mat(),
            Mat(),
            Mat(),
            Mat()
        )

        val sourceImage = image.clone()
        val adaptedImage = imageCreator.adaptiveThreshold(sourceImage)
        val edgesImage = imageCreator.createEdgesImage(adaptedImage)

        val globalLines = detectLineSegments(edgesImage)
        val lines = processLines(globalLines)
        val linesImage = imageCreator.createLinesImage(sourceImage, lines)

        return ProcessedImage(lines, image, adaptedImage, edgesImage, linesImage)
    }


    fun extractTextBlocks(gray: Mat, onResult: (Mat?) -> Unit) {

        if (gray.empty() || gray.channels() != 1) {
            onResult(null)
            return
        }

        val source = gray.clone()
        val bitmap = createBitmap(source.cols(), source.rows())

        val rgba = Mat()

        try {
            Imgproc.cvtColor(source, rgba, Imgproc.COLOR_GRAY2RGBA)
            Utils.matToBitmap(rgba, bitmap)
        } catch (e: Exception) {
            rgba.release()
            source.release()
            bitmap.recycle()
            onResult(null)
            return
        }

        rgba.release()

        val recognizer = TextRecognition.getClient(
            TextRecognizerOptions.DEFAULT_OPTIONS
        )

        recognizer.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { visionText ->

                val output = source.clone()

                try {
                    for (block in visionText.textBlocks) {
                        val bounds = block.boundingBox ?: continue

                        val left = bounds.left.coerceIn(0, output.cols() - 1)
                        val top = bounds.top.coerceIn(0, output.rows() - 1)
                        val right = bounds.right.coerceIn(0, output.cols() - 1)
                        val bottom = bounds.bottom.coerceIn(0, output.rows() - 1)

                        if (right <= left || bottom <= top) continue

                        Log.d("OCR", "Tekst: ${block.text}, bounds: $bounds")

                        Imgproc.rectangle(
                            output,
                            org.opencv.core.Point(left.toDouble(), top.toDouble()),
                            org.opencv.core.Point(right.toDouble(), bottom.toDouble()),
                            Scalar(255.0),
                            2
                        )

                        Imgproc.putText(
                            output,
                            block.text,
                            org.opencv.core.Point(
                                left.toDouble(),
                                (top - 5).coerceAtLeast(15).toDouble()
                            ),
                            Imgproc.FONT_HERSHEY_SIMPLEX,
                            0.5,
                            Scalar(255.0),
                            1
                        )
                    }

                    onResult(output)
                } catch (e: Exception) {
                    output.release()
                    onResult(null)
                } finally {
                    source.release()
                    bitmap.recycle()
                    recognizer.close()
                }
            }
            .addOnFailureListener { e ->
                source.release()
                bitmap.recycle()
                recognizer.close()
                onResult(null)
            }
    }

    fun extractGameBoard(sourceImage: Mat, lines: Lines): Mat {

        if (sourceImage.empty()) {
            return Mat()
        }

        val boardRegion = localizeGameBoard(lines)

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

    fun extractLastColumn(data: ProcessedImage): Mat {
        val verticalLines = data.lines.verticalLines.toList()

        if (verticalLines.size < 2) {
            Log.w(
                "ImageProcessor",
                "Za mało linii pionowych: ${verticalLines.size}"
            )
            return Mat()
        }

        val lastIndex = verticalLines.lastIndex

        val lastColumnLine = verticalLines[lastIndex]
        val almostLastColumnLine = verticalLines[lastIndex - 1]

        val x = almostLastColumnLine.getMinX().toInt()
        val right = lastColumnLine.getMinX().toInt()

        val image = data.adaptedImage

        if (x < 0 || right > image.cols() || x >= right) {
            Log.w(
                "ImageProcessor",
                "Nieprawidłowe granice kolumny: x=$x, right=$right"
            )
            return Mat()
        }

        return image.submat(
            0,
            image.rows(),
            x,
            right
        ).clone()
    }

    fun extractColumns(data: ProcessedImage): List<ExtractedField> {
        val verticalLines = data.lines.verticalLines.toList()

        if (verticalLines.size < 2) {
            return emptyList()
        }

        val extractedFields = mutableListOf<ExtractedField>()

        var lastIndex = verticalLines.lastIndex
        var column = 0

        while (lastIndex > 0) {
            val rightColumn = verticalLines[lastIndex]
            val leftColumn = verticalLines[lastIndex - 1]

            val columnImage = extractColumn(data, leftColumn, rightColumn)

            if (columnImage.empty()) {
                Log.w("OCR", "Skipped empty column image")
                columnImage.release()
                lastIndex -= 1
                continue
            }

            val extractedField = ExtractedField(columnImage, column, Orientation.VERTICAL)
            extractedFields.add(extractedField)

            lastIndex -= 1
            column += 1
        }

        return extractedFields
    }

    fun getColumn(data: ProcessedImage, number: Int): Mat {
        val columns = extractColumns(data)

        return columns
            .firstOrNull { it.number == number }
            ?.image
            ?: Mat()
    }

    fun extractColumn(data: ProcessedImage, leftColumn: Line, rightColumn: Line): Mat {
        val leftBorder = leftColumn.getMinX().toInt()
        val rightBorder = rightColumn.getMaxX().toInt()

        val image = data.adaptedImage

        if (leftBorder < 0 || rightBorder > image.cols() || leftBorder >= rightBorder) {
            Log.w(
                "ImageProcessor",
                "incorrect column border: left=$leftBorder, right=$rightBorder"
            )
            return Mat()
        }

        return image.submat(
            0,
            image.rows(),
            leftBorder,
            rightBorder
        ).clone()
    }

    private fun detectLineSegments(edgesImage: Mat): Mat {
        if (edgesImage.empty()) return Mat()

        val grayEdges = Mat()

        if (edgesImage.channels() == 1) {
            edgesImage.copyTo(grayEdges)
        } else {
            Imgproc.cvtColor(edgesImage, grayEdges, Imgproc.COLOR_BGR2GRAY)
        }

        val lines = Mat()

        Imgproc.HoughLinesP(
            edgesImage,
            lines,
            1.0,
            Math.PI / 180.0,
            80,
            50.0,
            10.0
        )

        return lines
    }

    private fun processLines(lines: Mat): Lines {
        val categorizedLines = categorizeLinesByOrientation(lines)

        val horizontalLines = processLines(Orientation.HORIZONTAL, categorizedLines.horizontalLines)
        val verticalLines = processLines(Orientation.VERTICAL, categorizedLines.verticalLines)

        return Lines(horizontalLines, verticalLines)
    }

    private fun categorizeLinesByOrientation(lines: Mat): Lines {
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

        return Lines(horizontalLines, verticalLines)
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

    private fun localizeGameBoard(lines: Lines): BoardRegion {

        val horizontal = lines.horizontalLines
        val vertical = lines.verticalLines

        if (horizontal.isEmpty() || vertical.isEmpty()) {
            val point = Point(0.0, 0.0)
            return BoardRegion(point, point, point, point)
        }

        val firstHorizontal = horizontal.first()
        val lastHorizontal = horizontal.last()
        val firstVertical = vertical.first()
        val lastVertical = vertical.last()

        val topLeft = intersection(firstHorizontal, firstVertical) ?: return emptyBoardRegion()
        val topRight = intersection(firstHorizontal, lastVertical) ?: return emptyBoardRegion()
        val bottomLeft = intersection(lastHorizontal, firstVertical) ?: return emptyBoardRegion()
        val bottomRight = intersection(lastHorizontal, lastVertical) ?: return emptyBoardRegion()

        val offset = 10.0

        return BoardRegion(
            Point(topLeft.x - offset, topLeft.y - offset),
            Point(topRight.x + offset, topRight.y - offset),
            Point(bottomLeft.x - offset, bottomLeft.y + offset),
            Point(bottomRight.x + offset, bottomRight.y + offset)
        )

    }

    private fun emptyBoardRegion(): BoardRegion {
        val point = Point(0.0, 0.0)
        return BoardRegion(point, point, point, point)
    }
}


