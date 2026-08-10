package util

import util.json.JSONObject
import java.awt.Desktop
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

object UpdateChecker {

    private const val LATEST_RELEASE_API = "https://api.github.com/repos/librespeed/speedtest-desktop/releases/latest"
    private const val RELEASES_PAGE = "https://github.com/librespeed/speedtest-desktop/releases/latest"
    const val PROJECT_PAGE = "https://github.com/librespeed/speedtest-desktop"

    // set via -Dapp.version / -Dapp.build.date in build.gradle.kts; null when not launched through gradle/jpackage
    val currentVersion: String? = System.getProperty("app.version")
    val buildDate: String? = System.getProperty("app.build.date")

    @Volatile private var checked = false
    @Volatile private var result: String? = null

    /** Returns the latest version if it is newer than the running one, null otherwise. Blocking. */
    fun findUpdate(): String? {
        if (checked) return result
        val current = currentVersion ?: return null
        try {
            val conn = URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "LibreSpeed-Desktop")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val latest = JSONObject(body).getString("tag_name").removePrefix("v")
            result = if (isNewer(latest, current)) latest else null
            //only a parsed answer settles the question; a transient failure may be retried later
            checked = true
        } catch (_: Throwable) {
        }
        return result
    }

    fun openReleasesPage() {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI(RELEASES_PAGE))
        } catch (_: Throwable) { }
    }

    private fun isNewer(latest: String, current: String): Boolean {
        val l = latest.split(".").map { it.toIntOrNull() ?: return false }
        val c = current.split(".").map { it.toIntOrNull() ?: return false }
        for (i in 0 until maxOf(l.size, c.size)) {
            val a = l.getOrElse(i) { 0 }
            val b = c.getOrElse(i) { 0 }
            if (a != b) return a > b
        }
        return false
    }

}
