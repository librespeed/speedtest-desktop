package components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.*

@Composable
fun LinearChart(
    data : List<Double>,
    color : Color = Color.White,
    modifier: Modifier = Modifier
) {

    val points = ArrayList<Point>()
    val conPoint1 = ArrayList<Point>()
    val conPoint2 = ArrayList<Point>()

    val path = Path()

    val paint = Paint()
    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.mode = PaintMode.STROKE
    frameworkPaint.strokeWidth = 2.dp.value
    frameworkPaint.strokeCap = PaintStrokeCap.ROUND
    frameworkPaint.color = color.toArgb()
    frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.SOLID, 10f)

    Canvas(modifier.onSizeChanged {
        calcPoints(Size(it.width.toFloat(),it.height.toFloat()),data, points)
        calcCons(points, conPoint1, conPoint2)
    }) {
        //draw
        path.reset()
        path.moveTo(points.first().x, points.first().y)

        for (i in 1 until points.size) {
            path.cubicTo(
                conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                points[i].x, points[i].y
            )
        }

        drawIntoCanvas {
            it.drawPath(
                path, paint
            )
        }
    }

}

private fun calcPoints(
    size: Size,
    data: List<Double>,
    points : ArrayList<Point>
) {
    points.clear()
    val bottomY = size.height
    val xDiff = size.width / (data.size - 1)
    val maxData = data.maxBy { it }
    for (i in data.indices) {
        val y = bottomY - (data[i] / maxData * bottomY)
        points.add(Point(xDiff * i, y.toFloat()))
    }

}

private fun calcCons(
    points: ArrayList<Point>,
    conPoint1 : ArrayList<Point>,
    conPoint2 : ArrayList<Point>
) {
    conPoint1.clear()
    conPoint2.clear()
    for (i in 1 until points.size) {
        conPoint1.add(Point((points[i].x + points[i - 1].x) / 2, points[i - 1].y))
        conPoint2.add(Point((points[i].x + points[i - 1].x) / 2, points[i].y))
    }
}