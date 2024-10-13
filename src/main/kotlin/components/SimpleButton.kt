package components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import theme.ColorBox
import theme.Fonts

@Composable
fun SimpleButton(
    modifier: Modifier = Modifier,
    backgroundColor: Color = ColorBox.text.copy(0.1f),
    text: String,
    textColor: Color = ColorBox.text,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val textLayout = rememberTextMeasurer().measure(text, style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans))
    Canvas(modifier
        .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
        .height(38.dp).alpha(if (enabled) 1f else 0.7f).clip(RoundedCornerShape(50)).background(backgroundColor)
        .clickable(enabled) { onClick.invoke() }) {
        drawText(
            textLayoutResult = textLayout,
            color = textColor,
            topLeft = Offset(size.width / 2 - textLayout.size.width / 2,size.height / 2 - textLayout.size.height / 2),
        )
    }
}