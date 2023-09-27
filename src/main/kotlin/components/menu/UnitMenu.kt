package components.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import core.Service
import theme.ColorBox

@Composable
fun UnitMenu(
    show : Boolean = false,
    onDismissRequest : () -> Unit,
) {

    CustomDropdownMenu(
        expanded = show,
        onDismissRequest = onDismissRequest
    ) {
        MenuItem(
            text = "MB/s",
            onClick = {
                Service.unitSetting.value = Service.UNIT_MBYTE
                onDismissRequest.invoke()
            }
        )
        MenuItem(
            text = "Mbps",
            onClick = {
                Service.unitSetting.value = Service.UNIT_MBIT
                onDismissRequest.invoke()
            }
        )
    }

}

@Composable
fun MenuItem (text : String,onClick : () -> Unit) {
    Row(modifier = Modifier.height(40.dp).fillMaxWidth().widthIn(min = 160.dp).clickable { onClick.invoke() }, verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.padding(6.dp))
        Text(modifier = Modifier.padding(end = 16.dp),text = text, color = ColorBox.text.copy(0.7f), fontSize = 13.sp)
    }
}