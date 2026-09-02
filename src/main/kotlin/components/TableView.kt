package components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    onRowClick : ((Int) -> Unit)? = null,
    onHeaderClick : ((Int) -> Unit)? = null,
    itemContent: @Composable (column: Int,row : Int) -> String
) {

    val scrollState = rememberLazyListState()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(ColorBox.primary.copy(0.2f)).padding(start = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tableRows.forEachIndexed { index, row ->
                val cellModifier = Modifier.weight(row.weight).let { m ->
                    if (onHeaderClick != null) {
                        m.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onHeaderClick.invoke(index) }
                    } else m
                }
                Text(
                    modifier = cellModifier.padding(end = 12.dp),
                    text = row.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = Fonts.open_sans, textAlign = row.textAlign),
                    color = ColorBox.text
                )
            }
        }
        Box(Modifier.fillMaxWidth()) {
            LazyColumn(Modifier.fillMaxWidth(), state = scrollState) {
                items(columnCount) { column ->
                    val bg = if (column % 2 == 0) ColorBox.text.copy(0.03f) else Color.Transparent
                    val rowModifier = if (onRowClick != null) {
                        Modifier.fillMaxWidth().clickable { onRowClick.invoke(column) }
                    } else {
                        Modifier.fillMaxWidth()
                    }
                    Column(rowModifier) {
                        TableItem(
                            modifier = Modifier.fillMaxWidth().height(58.dp).background(bg).padding(start = 20.dp, end = 20.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = Fonts.open_sans),
                            textColor = ColorBox.text,
                            list = tableRows.mapIndexed { index, tableItemRow -> Triple(itemContent(column,index),tableItemRow.weight,tableItemRow.textAlign) }
                        )
                        if (column < columnCount - 1) {
                            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp, color = ColorBox.text.copy(0.06f))
                        }
                    }
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
            val columnGap = 12.dp.toPx()
            for (i in list.indices) {
                val childWidth = (size.width * (weights[i] / totalWeight))
                val textWidth = (childWidth - if (i < list.size - 1) columnGap else 0f).toInt().coerceAtLeast(0)
                val textLayoutResult = textMeasurer.measure(
                    text = list[i].first,
                    style = textStyle.copy(textAlign = list[i].third),
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    constraints = Constraints(0,textWidth,0,size.height.toInt())
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