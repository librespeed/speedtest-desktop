package routes.scenes

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import App
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.translate
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dosse.speedtest.res.Res
import com.dosse.speedtest.res.arrow_left
import com.dosse.speedtest.res.export
import com.dosse.speedtest.res.history
import com.dosse.speedtest.res.trash
import components.MyIconButton
import components.SimpleButton
import components.SwitchUnit
import components.TableItemRow
import components.TableView
import core.Database
import core.ModelHistory
import core.Service
import core.Service.toValidString
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import routes.dialogs.BaseDialog
import routes.dialogs.DialogDelete
import theme.ColorBox
import theme.Fonts
import util.Utils
import util.Utils.formatToDate
import util.Utils.openInBrowser
import util.Utils.suffixItems
import util.Utils.systemOpenFile

@Composable
fun HistoryScene(navigator: Navigator) {

    val historyList = remember { SnapshotStateList<ModelHistory>() }
    val unitSetting = Service.unitSetting.observeAsState()

    val coroutineScope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ModelHistory?>(null) }
    var sortColumn by remember { mutableStateOf(6) }
    var sortAscending by remember { mutableStateOf(false) }

    val displayList = remember(historyList.toList(), sortColumn, sortAscending) {
        val comparator: Comparator<ModelHistory> = when (sortColumn) {
            0 -> compareBy { it.netAdapter.lowercase() }
            1 -> compareBy { it.ping }
            2 -> compareBy { it.jitter }
            3 -> compareBy { it.download }
            4 -> compareBy { it.upload }
            5 -> compareBy { it.testPoint.lowercase() }
            else -> compareBy { it.date }
        }
        if (sortAscending) historyList.sortedWith(comparator) else historyList.sortedWith(comparator.reversed())
    }
    fun sortMark(index: Int) = if (sortColumn == index) (if (sortAscending) " ▲" else " ▼") else ""

    LaunchedEffect(Unit) {
        //the query runs off the UI thread; the transition into History stays smooth
        val rows = withContext(Dispatchers.IO) { Database.readHistory() }
        historyList.clear()
        historyList.addAll(rows)
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorBox.primaryDark), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 1200.dp).fillMaxSize().background(ColorBox.primaryDark)) {
            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp).fillMaxWidth().height(72.dp), verticalAlignment = Alignment.CenterVertically) {
                MyIconButton(
                    padding = PaddingValues(start = 6.dp),
                    icon = Res.drawable.arrow_left,
                    onClick = {
                        navigator.goBack()
                    }
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = "History",
                        color = ColorBox.text,
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = Fonts.open_sans)
                    )
                    Text(
                        text = historyList.size.suffixItems(),
                        color = ColorBox.text.copy(0.7f),
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = Fonts.open_sans)
                    )
                }
                MyIconButton(
                    icon = Res.drawable.export,
                    enabled = historyList.isNotEmpty(),
                    onClick = {
                        App.showLoading.value = true
                        coroutineScope.launch {
                            try {
                                Utils.exportHistoryToCSV(historyList, onSuccess = { it.systemOpenFile() })
                            } catch (t: Throwable) {
                                App.errorMessage.value = "Export failed:\n${t.message ?: t}"
                            } finally {
                                App.showLoading.value = false
                            }
                        }
                    }
                )
                MyIconButton(
                    padding = PaddingValues(end = 8.dp),
                    icon = Res.drawable.trash,
                    enabled = historyList.isNotEmpty(),
                    onClick = {
                        showClearDialog = true
                    }
                )
                SwitchUnit(
                    modifier = Modifier.padding(end = 16.dp),
                    isMbps = unitSetting.value == Service.UNIT_MBIT,
                    onClicked = {
                        Service.unitSetting.value = if (it == 1) Service.UNIT_MBYTE else Service.UNIT_MBIT
                    }
                )
            }
            if (historyList.isEmpty()) {
                val emptyIcon = painterResource(Res.drawable.history)
                val emptyText = rememberTextMeasurer().measure("History is empty !", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = Fonts.open_sans))
                Canvas(Modifier.fillMaxSize()) {
                    translate(size.width / 2 - 55f.dp.toPx(), size.height / 2 - 75f.dp.toPx() - emptyText.size.height) {
                        with(emptyIcon) {
                            draw(Size(110f.dp.toPx(), 110f.dp.toPx()), colorFilter = ColorFilter.tint(ColorBox.text.copy(0.7f)))
                        }
                    }
                    drawText(
                        textLayoutResult = emptyText,
                        topLeft = Offset(size.width / 2 - emptyText.size.width / 2, size.height / 2 + 38f.dp.toPx()),
                        color = ColorBox.text.copy(0.7f)
                    )
                }
            } else {
                TableView(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp).fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    tableRows = listOf(
                        TableItemRow(
                            weight = .9f,
                            title = "Net Adapter${sortMark(0)}",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Ping (ms)${sortMark(1)}",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Jitter (ms)${sortMark(2)}",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Download\n(${unitSetting.value})${sortMark(3)}",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Upload\n(${unitSetting.value})${sortMark(4)}",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = 1f,
                            title = "Test Point${sortMark(5)}",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .8f,
                            title = "Date${sortMark(6)}",
                            textAlign = TextAlign.Start,
                        )
                    ),
                    columnCount = displayList.size,
                    onRowClick = {
                        selectedItem = displayList[it]
                    },
                    onHeaderClick = {
                        if (sortColumn == it) {
                            sortAscending = !sortAscending
                        } else {
                            sortColumn = it
                            sortAscending = false
                        }
                    }
                ) { column, row ->
                    val item = displayList[column]
                    return@TableView when(row) {
                        0 -> item.netAdapter
                        1 -> item.ping.toString()
                        2 -> item.jitter.toString()
                        3 -> item.download.toValidString()
                        4 -> item.upload.toValidString()
                        5 -> item.testPoint
                        6 -> item.date.formatToDate("dd MMM yyyy\nHH:mm")
                        else -> ""
                    }
                }
            }

        }
    }

    DialogDelete(
        title = "Clear History !",
        description = "Are you sure to want to clear history ?",
        show = showClearDialog,
        onDismiss = {
            showClearDialog = false
        },
        onOk = {
            showClearDialog = false
            Database.clearHistory()
            historyList.clear()
        }
    )

    DialogHistoryDetail(
        item = selectedItem,
        unit = unitSetting.value,
        onDismiss = {
            selectedItem = null
        }
    )

}

@Composable
private fun DialogHistoryDetail(item : ModelHistory?, unit : String, onDismiss : () -> Unit) {
    BaseDialog(
        expanded = item != null,
        onDismissRequest = onDismiss
    ) {
        if (item != null) {
            Column(modifier = Modifier.width(420.dp)) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = item.date.formatToDate("dd MMM yyyy  HH:mm:ss"),
                    color = ColorBox.text,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = Fonts.open_sans)
                )
                DetailRow("Test Point", item.testPoint)
                DetailRow("Network Adapter", item.netAdapter)
                DetailRow("Ping", "${item.ping} ms")
                DetailRow("Jitter", "${item.jitter} ms")
                DetailRow("Download", "${item.download.toValidString()} $unit")
                DetailRow("Upload", "${item.upload.toValidString()} $unit")
                DetailRow("ISP Info", item.ispInfo)
                Row(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                    item.shareUrl?.let { url ->
                        SimpleButton(
                            modifier = Modifier.weight(1f).padding(end = 10.dp),
                            text = "Open Result URL",
                            onClick = {
                                url.openInBrowser()
                            }
                        )
                    }
                    SimpleButton(
                        modifier = Modifier.weight(1f),
                        text = "Close",
                        onClick = {
                            onDismiss.invoke()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(title : String, value : String) {
    Row(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 7.dp)) {
        Text(
            modifier = Modifier.width(140.dp),
            text = title,
            color = ColorBox.text.copy(0.5f),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans)
        )
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            color = ColorBox.text,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = Fonts.open_sans)
        )
    }
}