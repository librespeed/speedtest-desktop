package routes.scenes

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.MyIconButton
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
import routes.dialogs.DialogDelete
import theme.ColorBox
import theme.Fonts
import util.Utils
import util.Utils.formatToDate
import util.Utils.suffixItems
import util.Utils.systemOpenFile

@Composable
fun HistoryScene(navigator: Navigator) {

    val historyList = remember { SnapshotStateList<ModelHistory>() }
    val unitSetting = Service.unitSetting.observeAsState()

    val coroutineScope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        historyList.clear()
        historyList.addAll(Database.readHistory())
    }

    Box(modifier = Modifier.fillMaxSize().background(ColorBox.primaryDark), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.widthIn(max = 1200.dp).fillMaxSize().background(ColorBox.primaryDark)) {
            Row(modifier = Modifier.padding(start = 12.dp, end = 12.dp).fillMaxWidth().height(72.dp), verticalAlignment = Alignment.CenterVertically) {
                MyIconButton(
                    padding = PaddingValues(start = 6.dp),
                    icon = "icons/arrow-left.svg",
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
                    icon = "icons/export.svg",
                    enabled = historyList.isNotEmpty(),
                    onClick = {
                        App.showLoading.value = true
                        coroutineScope.launch {
                            Utils.exportHistoryToCSV(historyList, onSuccess = {
                                App.showLoading.value = false
                                it.systemOpenFile()
                            })
                        }
                    }
                )
                MyIconButton(
                    padding = PaddingValues(end = 8.dp),
                    icon = "icons/trash.svg",
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
                val emptyIcon = painterResource("icons/history.svg")
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
                            weight = 1f,
                            title = "Net Adapter",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Ping (ms)",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Jitter (ms)",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Download\n(${unitSetting.value})",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Upload\n(${unitSetting.value})",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = 1f,
                            title = "Test Point",
                            textAlign = TextAlign.Start,
                        ),
                        TableItemRow(
                            weight = .6f,
                            title = "Date",
                            textAlign = TextAlign.Start,
                        )
                    ),
                    columnCount = historyList.size
                ) { column, row ->
                    val item = historyList[column]
                    return@TableView when(row) {
                        0 -> item.netAdapter
                        1 -> item.ping.toString()
                        2 -> item.jitter.toString()
                        3 -> item.download.toValidString()
                        4 -> item.upload.toValidString()
                        5 -> item.testPoint
                        6 -> item.date.formatToDate()
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

}