package components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.ColorBox

@Composable
fun MyIconButton(
    size : Dp = 48.dp,
    enabled: Boolean = true,
    rotate : Float = 0f,
    icon : String,
    background : Color = Color.Transparent,
    padding : PaddingValues = PaddingValues(0.dp),
    contentPadding : Dp = 12.dp,
    colorFilter : Color = ColorBox.text,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.padding(padding)
            .size(size)
            .rotate(rotate)
            .clip(RoundedCornerShape(50))
            .background(background)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button
            ),
        contentAlignment = Alignment.Center
    ) {
        val contentAlpha = if (enabled) LocalContentAlpha.current else ContentAlpha.disabled
        CompositionLocalProvider(LocalContentAlpha provides contentAlpha, content = {
            Icon(
                modifier = Modifier.fillMaxSize().padding(start = contentPadding, end = contentPadding),
                painter = painterResource(icon),
                contentDescription = null,
                tint = colorFilter
            )
        })
    }
}