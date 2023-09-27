package util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.math.BigDecimal
import java.math.RoundingMode

object Utils {

    fun Double.roundPlace (decimalPlace: Int): Double {
        var bd = BigDecimal(this.toString())
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun Float.roundPlace (decimalPlace: Int): Float {
        var bd = BigDecimal(this.toString())
        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP)
        return bd.toFloat()
    }

    fun Float.validate () : Float = if (this > 1f) 1f else this

    fun Double.toMegabyte(): Double = this * .125

    @Composable
    fun Modifier.clickable() = this.then(
        clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    )

}

