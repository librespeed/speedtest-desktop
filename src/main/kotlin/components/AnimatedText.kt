package components

import androidx.compose.animation.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun AnimatedText(
    modifier: Modifier = Modifier,
    text : String,
    color: Color = Color.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    style: TextStyle = LocalTextStyle.current
) {

    AnimatedContent(
        modifier = modifier,
        targetState = text,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn() togetherWith
                    slideOutVertically { height -> -height } + fadeOut()).using(SizeTransform(false))
        }
    ) {
        Text(
            text = text,
            color = color,
            overflow = overflow,
            style = style,
            maxLines = maxLines
        )
    }

}