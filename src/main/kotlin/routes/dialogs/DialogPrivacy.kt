package routes.dialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.ResourceLoader
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    icon = "icons/close.svg",
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

            val privacy = ResourceLoader.Default.load("/configs/privacy_en.md").bufferedReader().use { it.readText() }

            Markdown(
                modifier = Modifier.verticalScroll(scrollState).padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
                content = privacy,
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