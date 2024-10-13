package components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import org.jetbrains.skia.Point
import theme.ColorBox

@Composable
fun SparkUp(
    modifier: Modifier = Modifier,
    data : List<Double>,
    drawGradient : Boolean = true,
    color : Color = ColorBox.primary
) {

    val points = remember { ArrayList<Point>() }
    val conPoint1 = remember { ArrayList<Point>() }
    val conPoint2 = remember { ArrayList<Point>() }

    val path = remember { Path() }
    var canvasSize = remember { Size.Zero }

    LaunchedEffect(data.toList()) {
        if (data.isNotEmpty()) {
            calcPoints(canvasSize,data,points)
            calcCons(points,conPoint1,conPoint2)
        }
    }

    Canvas(modifier = modifier.onSizeChanged {
        canvasSize = Size(it.width.toFloat(),it.height.toFloat())
        if (data.isNotEmpty()) {
            calcPoints(canvasSize,data,points)
            calcCons(points,conPoint1,conPoint2)
        }
    }) {
        drawSparkLine(path, points, conPoint1, conPoint2, color, drawGradient)
    }

}

fun DrawScope.drawSparkLine(
    path : Path,
    points : List<Point>,
    conPoint1 : List<Point>,
    conPoint2 : List<Point>,
    color: Color,
    drawGradient: Boolean
) {
    path.reset()
    if (points.isNotEmpty()) {
        path.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            path.cubicTo(
                conPoint1[i - 1].x, conPoint1[i - 1].y, conPoint2[i - 1].x, conPoint2[i - 1].y,
                points[i].x, points[i].y
            )
        }
        drawPath(path, color = color.copy(0.4f), style = Stroke(width = 3f))
        if (drawGradient) {
            path.lineTo(size.width,size.height)
            path.lineTo(0f,size.height)
            path.lineTo(points.first().x, points.first().y)
            drawPath(path, brush = Brush.verticalGradient(colors = listOf(color.copy(0.2f),Color.Transparent)), style = Fill)
        }
    }
}

fun calcPoints(
    size: Size,
    data: List<Double>,
    points : ArrayList<Point>,
    vPadding : Float = 0f
) {
    points.clear()
    val bottomY = size.height - vPadding
    val xDiff = size.width / (data.size - 1)
    val maxData = data.max()
    for (i in data.indices) {
        val y = bottomY - (data[i] / maxData * bottomY) + (vPadding / 2f)
        points.add(Point(xDiff * i, y.toFloat()))
    }
}

fun calcCons(
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