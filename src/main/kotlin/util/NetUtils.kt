package util

import java.io.File
import java.net.*
import java.util.*
import java.util.concurrent.TimeUnit

object NetUtils {

    fun parseMacAddress(mac: ByteArray): String {
        val sb = StringBuilder()
        for (i in mac.indices) {
            sb.append(String.format("%02X%s", mac[i], if ((i < mac.size - 1)) ":" else ""))
        }
        return sb.toString()
    }

    fun getDefaultNetworkInterface(): NetworkInterface? {
        val globalHost = "a.root-servers.net"
        var result: NetworkInterface? = null
        var remoteAddress: InetAddress? = null
        try {
            remoteAddress = InetAddress.getByName(globalHost)
        } catch (ignored: UnknownHostException) {
        }
        if (remoteAddress != null) {
            try {
                DatagramSocket().use { s ->
                    try {
                        s.connect(remoteAddress, 80)
                        result = NetworkInterface.getByInetAddress(s.localAddress)
                    } catch (e : Exception) {
                        return null
                    }
                }
            } catch (ignored: SocketException) {
                return null
            }
        }
        return result
    }

    private var cachedDescription: Pair<String, String>? = null

    /** "Wi-Fi 6 (802.11ax) • en0 (AA:BB:...)" / "Ethernet • en5 (...)" — falls back to the bare name. Blocking. */
    fun describeInterface(netInterface: NetworkInterface): String {
        val name = netInterface.name
        cachedDescription?.let { if (it.first == name) return it.second }
        val kind = try {
            detectKind(name, netInterface.displayName ?: name)
        } catch (_: Throwable) {
            null
        }
        val mac = try {
            netInterface.hardwareAddress?.let { parseMacAddress(it) }
        } catch (_: Throwable) {
            null
        }
        val description = buildString {
            if (kind != null) append("$kind • ")
            append(name)
            if (mac != null) append(" ($mac)")
        }
        cachedDescription = name to description
        return description
    }

    private fun detectKind(name: String, displayName: String): String? {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        return when {
            os.contains("mac") -> macKind(name)
            os.contains("linux") -> if (File("/sys/class/net/$name/wireless").exists()) "Wi-Fi" else "Ethernet"
            os.contains("windows") -> windowsKind(displayName)
            else -> null
        }
    }

    private fun macKind(device: String): String? {
        val output = runCommand(listOf("networksetup", "-listallhardwareports"), 5) ?: return null
        var port: String? = null
        var kind: String? = null
        output.lineSequence().forEach { line ->
            if (line.startsWith("Hardware Port:")) port = line.substringAfter(":").trim()
            if (line.startsWith("Device:") && line.substringAfter(":").trim() == device) kind = port
        }
        if (kind?.contains("Wi-Fi") != true) return kind
        val phy = runCommand(listOf("system_profiler", "SPAirPortDataType", "-detailLevel", "basic"), 10)
            ?.lineSequence()?.firstOrNull { it.trim().startsWith("PHY Mode:") }
            ?.substringAfter(":")?.trim()
        return if (phy != null) "${wifiGeneration(phy)} ($phy)" else "Wi-Fi"
    }

    //best effort: netsh labels are localized, but the adapter description and the
    //"802.11xx" radio values are not, so match on those instead of the labels
    private fun windowsKind(displayName: String): String? {
        val looksWifi = displayName.lowercase(Locale.getDefault()).let {
            it.contains("wi-fi") || it.contains("wireless") || it.contains("802.11")
        }
        val output = runCommand(listOf("netsh", "wlan", "show", "interfaces"), 5)
        if (output != null) {
            val phyRegex = Regex("""\b802\.11[a-z]+\b""")
            val adapterIndex = output.indexOf(displayName)
            val phy = if (adapterIndex >= 0) {
                phyRegex.find(output, adapterIndex)?.value
            } else if (looksWifi) {
                phyRegex.find(output)?.value
            } else null
            if (phy != null) return "${wifiGeneration(phy)} ($phy)"
        }
        return if (looksWifi) "Wi-Fi" else "Ethernet"
    }

    private fun wifiGeneration(phy: String): String {
        return when {
            phy.endsWith("be") -> "Wi-Fi 7"
            phy.endsWith("ax") -> "Wi-Fi 6"
            phy.endsWith("ac") -> "Wi-Fi 5"
            phy.endsWith("n") -> "Wi-Fi 4"
            else -> "Wi-Fi"
        }
    }

    private fun runCommand(command: List<String>, timeoutSeconds: Long): String? {
        return try {
            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            //read on a separate thread so a hung process cannot block past the timeout
            val output = StringBuilder()
            val reader = Thread {
                try {
                    process.inputStream.bufferedReader().forEachLine { output.appendLine(it) }
                } catch (_: Throwable) { }
            }
            reader.isDaemon = true
            reader.start()
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                return null
            }
            reader.join(1000)
            if (process.exitValue() == 0) output.toString() else null
        } catch (_: Throwable) {
            null
        }
    }

}
