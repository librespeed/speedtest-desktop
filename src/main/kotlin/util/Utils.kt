package util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import core.ModelHistory
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

    fun String.openInBrowser() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI(this))
                return
            }
        } catch (_: Throwable) { }
        //Desktop.browse is not reliable everywhere; fall back to the platform opener
        try {
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            val command = when {
                os.contains("mac") -> listOf("open", this)
                os.contains("windows") -> listOf("rundll32", "url.dll,FileProtocolHandler", this)
                else -> listOf("xdg-open", this)
            }
            ProcessBuilder(command).start()
        } catch (_: Throwable) { }
    }

    private fun String.escapeCsvValue(): String = "\"${replace("\"", "\"\"")}\""
    suspend fun exportHistoryToCSV(input : List<ModelHistory>,onSuccess : (File) -> Unit) {
        withContext(Dispatchers.IO) {
            val exportFile = File("${System.getProperty("user.home")}${File.separator}Downloads","librespeed-history.csv")
            exportFile.parentFile.mkdirs()
            exportFile.createNewFile()
            //raw stored values and a fixed date format, so exports compare across UI settings and machine locales
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
            BufferedWriter(FileWriter(exportFile)).use {
                it.write("id,netAdapter,ping,jitter,download (mbps),upload (mbps),ispInfo,testPoint,date,shareUrl\n")
                for (model in input) {
                    it.write("${model.id},${model.netAdapter.escapeCsvValue()}," +
                            "${model.ping},${model.jitter},${model.download}," +
                            "${model.upload},${model.ispInfo.escapeCsvValue()}," +
                            "${model.testPoint.escapeCsvValue()},${dateFormat.format(Date(model.date))}," +
                            "${(model.shareUrl ?: "").escapeCsvValue()}\n")
                }
            }
            //the callback opens the file in an external viewer; it must not run before the writer is closed
            onSuccess.invoke(exportFile)
        }
    }

}

