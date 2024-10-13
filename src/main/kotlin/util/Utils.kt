package util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import core.ModelHistory
import core.Service.toValidString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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

    fun Int.suffixItems() : String {
        return if (this == 0) "No Items" else if (this == 1) "1 Item" else "$this items"
    }

    fun Float.validate () : Float = if (this > 1f) 1f else this

    fun Double.toMegabyte(): Double = this * .125

    fun Long.formatToDate(pattern: String = "dd MMM yyyy\nHH:mm:ss"): String {
        val simple: DateFormat = SimpleDateFormat(pattern)
        val result = Date(this)
        return simple.format(result)
    }

    @Composable
    fun Modifier.clickable() = this.then(
        clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
    )

    fun File.systemOpenFile() {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(this)
        }
    }

    private fun String.escapeCsvValue(): String = "\"${replace("\"", "\"\"")}\""
    suspend fun exportHistoryToCSV(input : SnapshotStateList<ModelHistory>,onSuccess : (File) -> Unit) {
        withContext(Dispatchers.IO) {
            val exportFile = File("${System.getProperty("user.home")}${File.separator}Downloads","librespeed-history.csv")
            exportFile.parentFile.mkdirs()
            exportFile.createNewFile()
            BufferedWriter(FileWriter(exportFile)).use {
                it.write("id,netAdapter,ping,jitter,download,upload,ispInfo,testPoint,date\n")
                for (model in input) {
                    it.write("${model.id},${model.netAdapter.escapeCsvValue()}," +
                            "${model.ping},${model.jitter},${model.download.toValidString()}," +
                            "${model.upload.toValidString()},${model.ispInfo.escapeCsvValue()}," +
                            "${model.testPoint.escapeCsvValue()},${model.date.formatToDate("dd-MMM-yyyy HH:mm:ss")}\n")
                }
                onSuccess.invoke(exportFile)
            }
        }
    }

}

