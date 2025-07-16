package routes.dialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dosse.speedtest.res.Res
import com.dosse.speedtest.res.close
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.markdownColor
import com.mikepenz.markdown.model.markdownTypography
import components.MyIconButton
import theme.ColorBox
import theme.Fonts

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialogPrivacy(
    closeClicked: () -> Unit
) {

    val scrollState = rememberScrollState()
    var privacyText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        privacyText = String(Res.readBytes("files/privacy_en.md"), Charsets.UTF_8)
    }

    Box(modifier = Modifier.width(460.dp).heightIn(max = 600.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(64.dp).background(color = ColorBox.card.copy(0.5f)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.padding(3.dp))
                MyIconButton(
                    contentPadding = 10.dp,
                    colorFilter = ColorBox.text.copy(0.6f),
                    icon = Res.drawable.close,
                    onClick = {
                        closeClicked.invoke()
                    }
                )
                Text(
                    modifier = Modifier.padding(start = 12.dp).weight(1f),
                    text = "Privacy Policy",
                    color = ColorBox.text.copy(0.8f),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Markdown(
                modifier = Modifier.verticalScroll(scrollState).padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                content = privacyText,
                colors = markdownColor(text = ColorBox.text),
                typography = markdownTypography(
                    h4 = TextStyle(fontFamily = Fonts.open_sans, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    text = TextStyle(fontFamily = Fonts.open_sans, fontSize = 14.sp),
                    paragraph = TextStyle(fontFamily = Fonts.open_sans, fontSize = 14.sp),
                    bullet = TextStyle(fontFamily = Fonts.open_sans, fontSize = 14.sp),
                    h6 = TextStyle(fontFamily = Fonts.open_sans, fontSize = 14.sp),
                )
            )

        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
            style = LocalScrollbarStyle.current.copy(
                thickness = 6.dp,
                hoverColor = ColorBox.text.copy(0.6f),
                unhoverColor = ColorBox.text.copy(0.1f)
            )
        )
    }


}