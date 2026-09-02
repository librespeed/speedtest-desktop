package util

import core.ModelHistory
import core.Service
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Regressions: onSuccess used to fire before the writer flushed (the viewer opened an empty file),
 * and the exported speeds changed with the live unit toggle and were rounded to one decimal.
 */
class HistoryCsvExportTest {

    @Test
    fun fileIsCompleteWhenOnSuccessFiresAndValuesIgnoreTheUnitToggle() {
        val home = Files.createTempDirectory("csv-export-test").toFile()
        val previousHome = System.getProperty("user.home")
        val previousUnit = Service.unitSetting.value
        try {
            System.setProperty("user.home", home.absolutePath)
            Service.unitSetting.value = Service.UNIT_MBYTE //must not leak into the export
            val entries = listOf(
                ModelHistory(
                    id = 1,
                    netAdapter = "Wi-Fi \"casa\", 5GHz",
                    ping = 12.3,
                    jitter = 1.5,
                    download = 94.75,
                    upload = 23.5,
                    ispInfo = "203.0.113.7 - Example ISP",
                    testPoint = "Frankfurt, Germany",
                    date = 1700000000000L,
                    shareUrl = null
                )
            )
            var contentAtCallback: String? = null
            runBlocking {
                Utils.exportHistoryToCSV(entries, onSuccess = { contentAtCallback = it.readText() })
            }
            val content = contentAtCallback
            assertNotNull(content, "onSuccess was never invoked")
            val lines = content.trim().split("\n")
            assertEquals(2, lines.size, "header plus one row expected in: $content")
            assertEquals("id,netAdapter,ping,jitter,download (mbps),upload (mbps),ispInfo,testPoint,date,shareUrl", lines[0])
            assertTrue(lines[1].contains(",94.75,23.5,"), "raw mbps values expected in: ${lines[1]}")
            assertTrue(Regex(""",\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2},""").containsMatchIn(lines[1]), "fixed-format date expected in: ${lines[1]}")
            //the callback saw exactly what is on disk
            assertEquals(File(home, "Downloads${File.separator}librespeed-history.csv").readText(), content)
        } finally {
            System.setProperty("user.home", previousHome)
            Service.unitSetting.value = previousUnit
            home.deleteRecursively()
        }
    }
}
