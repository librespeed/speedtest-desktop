package components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import theme.ColorBox
import theme.Fonts

@Composable
fun TableView(
    tableRows: List<TableItemRow>,
    columnCount : Int,
    modifier: Modifier,
    itemContent: @Composable (column: Int,row : Int) -> String
) {

    val scrollState = rememberLazyListState()

    Column(modifier = modifier) {
        TableItem(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(ColorBox.primary.copy(0.2f)).padding(start = 20.dp, end = 20.dp),
            textStyle = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.open_sans),
            textColor = ColorBox.text,
            list = tableRows.map { Triple(it.title,it.weight,it.textAlign) }
        )
        Box(Modifier.fillMaxWidth()) {
            LazyColumn(Modifier.fillMaxWidth(), state = scrollState) {
                items(columnCount) { column ->
                    val bg = if (column % 2 == 0) ColorBox.text.copy(0.03f) else Color.Transparent
                    TableItem(
                        modifier = Modifier.fillMaxWidth().height(58.dp).background(bg).padding(start = 20.dp, end = 20.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.open_sans),
                        textColor = ColorBox.text,
                        list = tableRows.mapIndexed { index, tableItemRow -> Triple(itemContent(column,index),tableItemRow.weight,tableItemRow.textAlign) }
                    )
                }
            }
            if (scrollState.canScrollForward || scrollState.canScrollBackward) {
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    style = LocalScrollbarStyle.current
                        .copy(thickness = 5.dp, hoverColor = ColorBox.text.copy(0.6f), unhoverColor = ColorBox.text.copy(0.1f)),
                    adapter = rememberScrollbarAdapter(scrollState)
                )
            }
        }
    }

}

data class TableItemRow (
    var weight : Float,
    var title : String,
    var textAlign: TextAlign
)

@Composable
private fun TableItem (modifier: Modifier,textColor: Color,textStyle : TextStyle,list: List<Triple<String,Float,TextAlign>>) {
    val weights = remember { mutableStateListOf<Float>() }
    var totalWeight by remember { mutableStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()
    LaunchedEffect(list) {
        weights.clear()
        totalWeight = 0f
        weights.addAll(list.map {
            totalWeight += it.second
            it.second
        })
    }
    Canvas(modifier) {
        if (weights.isNotEmpty()) {
            var currentX = 0f
            for (i in list.indices) {
                val childWidth = (size.width * (weights[i] / totalWeight))
                val textLayoutResult = textMeasurer.measure(
                    text = list[i].first,
                    style = textStyle.copy(textAlign = list[i].third),
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Clip,
                    constraints = Constraints(0,childWidth.toInt(),0,size.height.toInt())
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(currentX,size.height / 2 - textLayoutResult.size.height / 2),
                    color = textColor
                )
                currentX += childWidth
            }
        }
    }
}