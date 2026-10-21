import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc

object Constants {

    val NO_PICTURE: Mat = createNoCameraImage(640, 480)

    fun createNoCameraImage(width: Int, height: Int): Mat {
        val image = Mat.zeros(height, width, CvType.CV_8UC3)

        val font = Imgproc.FONT_HERSHEY_SIMPLEX
        val fontScale = 1.0
        val thickness = 2

        val text = "NO PICTURE"

        val textSize = Imgproc.getTextSize(
            text,
            font,
            fontScale,
            thickness,
            null
        )

        val x = (width - textSize.width) / 2.0
        val y = (height + textSize.height) / 2.0

        Imgproc.putText(
            image,
            text,
            Point(x, y),
            font,
            fontScale,
            Scalar(255.0, 255.0, 255.0),
            thickness,
            Imgproc.LINE_AA,
            false
        )

        return image
    }
}