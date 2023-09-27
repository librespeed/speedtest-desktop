package routes.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import components.MyIconButton
import core.Service
import core.lib.serverSelector.TestPoint
import theme.ColorBox
import util.Utils.clickable

@Composable
fun BoxScope.HomeBottomSheet(
    closeClicked : () -> Unit
) {

    val servers = Service.serverList().filter { it.ping != -1f }.sortedBy { it.ping }
    val scrollState = rememberLazyListState()
    var selectedServer by remember { mutableStateOf(Service.testPoint.value) }

    Column(Modifier.fillMaxWidth().clickable()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(color = ColorBox.primaryDark.copy(0.5f)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.padding(3.dp))
            MyIconButton(
                contentPadding = 14.dp,
                rotate = 90f,
                colorFilter = ColorBox.text.copy(0.6f),
                icon = "icons/arrow-right.svg",
                onClick = {
                    closeClicked.invoke()
                }
            )
            Text(
                modifier = Modifier.padding(start = 12.dp).weight(1f),
                text = "Servers",
                color = ColorBox.text.copy(0.8f),
                style = MaterialTheme.typography.titleMedium
            )
            Row(modifier = Modifier.padding(end = 12.dp).height(40.dp).clip(RoundedCornerShape(50)).background(ColorBox.primary).clickable {
                Service.testPoint.value = selectedServer
                closeClicked.invoke()
            }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource("icons/check-circle.svg"),
                    contentDescription = null,
                    tint = ColorBox.text.copy(0.9f)
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp, end = 2.dp),
                    text = "Done",
                    color = ColorBox.text.copy(0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        LazyColumn(Modifier.fillMaxWidth(),state = scrollState, contentPadding = PaddingValues(8.dp)) {
            items(servers) {
                ServerCell(
                    it,
                    selectedServer == it,
                    onClicked = {
                        selectedServer = it
                    }
                )
            }
        }
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


@Composable
private fun ServerCell (testPoint: TestPoint, isChecked : Boolean,onClicked : () -> Unit) {
    Row(Modifier.padding(6.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ColorBox.primaryDark).clickable {
        onClicked.invoke()
    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        RadioButton(
            selected = isChecked,
            onClick = null
        )
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = testPoint.name.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = ColorBox.text
            )
            if (testPoint.ping != -1f) {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = "${testPoint.ping.toInt()} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorBox.primary
                )
            }
        }
    }
}