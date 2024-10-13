package routes.sections.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import components.DayNightAnimationIcon
import components.MyIconButton
import components.SwitchUnit
import core.Service
import dev.icerock.moko.mvvm.livedata.compose.observeAsState
import routes.dialogs.BaseDialog
import routes.dialogs.DialogPrivacy
import theme.ColorBox

@Composable
fun Appbar(onHistoryClicked : () -> Unit) {

    var showPrivacyDialog by remember { mutableStateOf(false) }
    val unitSetting = Service.unitSetting.observeAsState()

    Row(modifier = Modifier.widthIn(max = 1200.dp).fillMaxWidth().height(76.dp), verticalAlignment = Alignment.CenterVertically) {
        Image(
            modifier = Modifier.padding(start = 16.dp).size(44.dp),
            painter = painterResource("icons/icon_app.svg"),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(start = 12.dp).weight(1f),
            text = "LibreSpeed",
            color = ColorBox.text,
            style = MaterialTheme.typography.titleMedium
        )
        MyIconButton(
            icon = "icons/history.svg",
            onClick = {
                onHistoryClicked.invoke()
            }
        )
        DayNightAnimationIcon(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).clickable {
                ColorBox.switchTheme()
            }.padding(10.dp),
            color = ColorBox.text,
            isDay = ColorBox.isNightTheme
        )
        MyIconButton(
            padding = PaddingValues(end = 8.dp),
            icon = "icons/security.svg",
            onClick = {
                showPrivacyDialog = true
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

    BaseDialog(
        expanded = showPrivacyDialog,
        onDismissRequest = {
            showPrivacyDialog = false
        },
        backgroundColor = ColorBox.primaryDark
    ) {
        DialogPrivacy {
            showPrivacyDialog = false
        }
    }

}