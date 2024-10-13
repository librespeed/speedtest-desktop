package routes.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.SimpleButton
import theme.ColorBox

@Composable
fun DialogDelete(
    title : String,
    description : String,
    show : Boolean,
    onDismiss: () -> Unit,
    onOk: () -> Unit,
) {

    BaseDialog(
        expanded = show,
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.width(350.dp),horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(20.dp),
                text = title,
                color = ColorBox.text,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                modifier = Modifier.padding(start = 20.dp, bottom = 16.dp, end = 20.dp),
                text = description,
                color = ColorBox.text.copy(0.7f),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            )
            Row(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp).fillMaxWidth()) {
                SimpleButton(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    text = "Close",
                    onClick = {
                        onDismiss.invoke()
                    }
                )
                SimpleButton(
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    backgroundColor = ColorBox.error.copy(0.1f),
                    textColor = ColorBox.error,
                    text = "Clear",
                    onClick = {
                        onOk.invoke()
                    }
                )
            }
        }
    }

}