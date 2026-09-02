package routes.sections

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.unit.dp
import com.dosse.speedtest.res.Res
import com.dosse.speedtest.res.add_server
import com.dosse.speedtest.res.arrow_right
import com.dosse.speedtest.res.check_circle
import com.dosse.speedtest.res.trash
import components.MyIconButton
import components.SimpleButton
import core.CustomServers
import core.Service
import core.lib.serverSelector.TestPoint
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import kotlinx.coroutines.launch
import routes.dialogs.BaseDialog
import theme.ColorBox
import theme.Fonts
import util.Utils.clickable

@Composable
fun BoxScope.HomeBottomSheet(
    closeClicked : () -> Unit
) {

    val scope = rememberCoroutineScope()
    val serversVersion = Service.serversVersion.observeAsState()
    val servers = remember(serversVersion.value) {
        Service.serverList()
            .filter { it.ping != -1f || CustomServers.isCustom(it) }
            .sortedBy { if (it.ping == -1f) Float.MAX_VALUE else it.ping }
    }
    val scrollState = rememberLazyListState()
    var selectedServer by remember(serversVersion.value) { mutableStateOf(Service.testPoint.value) }
    var showAddDialog by remember { mutableStateOf(false) }
    var reloading by remember { mutableStateOf(false) }

    fun reloadServers() {
        reloading = true
        Service.onServerSelected = { reloading = false }
        scope.launch {
            Service.startFetchServers { _, _ -> }
        }
    }

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
                icon = Res.drawable.arrow_right,
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
            MyIconButton(
                contentPadding = 13.dp,
                colorFilter = ColorBox.text.copy(0.6f),
                icon = Res.drawable.add_server,
                onClick = {
                    showAddDialog = true
                }
            )
            Row(modifier = Modifier.padding(end = 12.dp).height(40.dp).clip(RoundedCornerShape(50)).background(ColorBox.primary).clickable {
                Service.testPoint.value = selectedServer
                closeClicked.invoke()
            }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(Res.drawable.check_circle),
                    contentDescription = null,
                    tint = ColorBox.COLOR_TEXT_NIGHT.copy(0.9f)
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp, end = 2.dp),
                    text = "Done",
                    color = ColorBox.COLOR_TEXT_NIGHT.copy(0.9f),
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
                    },
                    onDelete = if (CustomServers.isCustom(it)) {
                        {
                            CustomServers.remove(it)
                            reloadServers()
                        }
                    } else null
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

    DialogAddServer(
        show = showAddDialog,
        onDismiss = {
            showAddDialog = false
        },
        onAdd = { name, url ->
            val added = try {
                CustomServers.add(name, url)
                true
            } catch (_: Exception) {
                false
            }
            if (added) {
                showAddDialog = false
                reloadServers()
            }
        }
    )

    BaseDialog(expanded = reloading) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.padding(20.dp), strokeCap = StrokeCap.Round)
            Text(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                text = "Finding best servers ...",
                color = ColorBox.text,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans),
            )
        }
    }

}

@Composable
private fun DialogAddServer(
    show : Boolean,
    onDismiss : () -> Unit,
    onAdd : (name : String, url : String) -> Unit
) {

    var name by remember(show) { mutableStateOf("") }
    var url by remember(show) { mutableStateOf("") }

    BaseDialog(
        expanded = show,
        onDismissRequest = onDismiss
    ) {
        Column(modifier = Modifier.width(400.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(20.dp),
                text = "Add custom server",
                color = ColorBox.text,
                style = MaterialTheme.typography.titleLarge
            )
            OutlinedTextField(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
                colors = dialogTextFieldColors(),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            OutlinedTextField(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp).fillMaxWidth(),
                value = url,
                onValueChange = { url = it },
                singleLine = true,
                label = { Text("URL") },
                placeholder = { Text("https://speedtest.example.com/backend") },
                colors = dialogTextFieldColors(),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            Text(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp).fillMaxWidth(),
                text = "The URL must point to a LibreSpeed backend (garbage.php, empty.php, getIP.php).",
                color = ColorBox.text.copy(0.5f),
                style = MaterialTheme.typography.labelSmall
            )
            Row(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                SimpleButton(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    text = "Cancel",
                    onClick = {
                        onDismiss.invoke()
                    }
                )
                SimpleButton(
                    modifier = Modifier.weight(1f).padding(start = 10.dp),
                    backgroundColor = ColorBox.primary.copy(0.15f),
                    textColor = ColorBox.primary,
                    text = "Add",
                    enabled = name.isNotBlank() && url.isNotBlank(),
                    onClick = {
                        onAdd.invoke(name, url)
                    }
                )
            }
        }
    }

}

@Composable
private fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ColorBox.text,
    unfocusedTextColor = ColorBox.text,
    focusedBorderColor = ColorBox.primary,
    unfocusedBorderColor = ColorBox.text.copy(0.3f),
    focusedLabelColor = ColorBox.primary,
    unfocusedLabelColor = ColorBox.text.copy(0.5f),
    focusedPlaceholderColor = ColorBox.text.copy(0.3f),
    unfocusedPlaceholderColor = ColorBox.text.copy(0.3f),
    cursorColor = ColorBox.primary
)

@Composable
private fun ServerCell (testPoint: TestPoint, isChecked : Boolean, onClicked : () -> Unit, onDelete : (() -> Unit)? = null) {
    Row(Modifier.padding(6.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ColorBox.primaryDark).clickable {
        onClicked.invoke()
    }.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        RadioButton(
            selected = isChecked,
            onClick = null,
            colors = RadioButtonDefaults.colors().copy(selectedColor = ColorBox.primary)
        )
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = testPoint.name.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = ColorBox.text
            )
            if (testPoint.ping != -1f) {
                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${testPoint.ping.toInt()} ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorBox.primary
                    )
                    if (testPoint.ipVersion != 0) {
                        val badgeColor = if (testPoint.ipVersion == 6) Color(0xFF17c3b2) else Color(0xFFF7941D)
                        Text(
                            modifier = Modifier.padding(start = 8.dp).clip(RoundedCornerShape(4.dp)).background(badgeColor.copy(0.15f)).padding(start = 5.dp, end = 5.dp, top = 1.dp, bottom = 1.dp),
                            text = "IPv${testPoint.ipVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor
                        )
                    }
                }
            } else {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = "unreachable",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorBox.error
                )
            }
        }
        if (onDelete != null) {
            MyIconButton(
                size = 40.dp,
                contentPadding = 10.dp,
                colorFilter = ColorBox.error.copy(0.8f),
                icon = Res.drawable.trash,
                onClick = {
                    onDelete.invoke()
                }
            )
        }
    }
}
