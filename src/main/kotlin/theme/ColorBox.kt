package theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import util.Settings

object ColorBox {

    val COLOR_PRIMARY = Color(0xFF6060AA)

    val COLOR_PRIMARY_DARK_NIGHT = Color(0xFF1f2733)
    val COLOR_CARD_NIGHT = Color(0xFF2A3341)
    val COLOR_TEXT_NIGHT = Color.White
    val COLOR_ERROR_NIGHT = Color(0xFFB00020)

    val COLOR_PRIMARY_DARK_DAY = Color(0xFFFFFFFF)
    val COLOR_CARD_DAY = Color(0xFFE3E3E3)
    val COLOR_TEXT_DAY = Color.Black
    val COLOR_ERROR_DAY = Color(0xFFCF6679)

    var isNightTheme by mutableStateOf(Settings.isNightTheme)

    var primary by mutableStateOf(COLOR_PRIMARY)
    var primaryDark by mutableStateOf(if (isNightTheme) COLOR_PRIMARY_DARK_NIGHT else COLOR_PRIMARY_DARK_DAY)
    var card by mutableStateOf(if (isNightTheme) COLOR_CARD_NIGHT else COLOR_CARD_DAY)
    var text by mutableStateOf(if (isNightTheme) COLOR_TEXT_NIGHT else COLOR_TEXT_DAY)
    var error by mutableStateOf(if (isNightTheme) COLOR_ERROR_NIGHT else COLOR_ERROR_DAY)

    fun switchTheme () {
        if (isNightTheme) {
            primaryDark = COLOR_PRIMARY_DARK_DAY
            card = COLOR_CARD_DAY
            text = COLOR_TEXT_DAY
            error = COLOR_ERROR_DAY
        } else {
            primaryDark = COLOR_PRIMARY_DARK_NIGHT
            card = COLOR_CARD_NIGHT
            text = COLOR_TEXT_NIGHT
            error = COLOR_ERROR_NIGHT
        }
        Settings.isNightTheme = !isNightTheme
        isNightTheme = !isNightTheme
    }

}