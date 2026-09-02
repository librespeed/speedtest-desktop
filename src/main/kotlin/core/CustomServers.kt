package core

import core.lib.serverSelector.TestPoint
import util.Settings
import util.json.JSONArray
import util.json.JSONObject
import java.net.URI

object CustomServers {

    fun load(): List<TestPoint> {
        return try {
            val arr = JSONArray(Settings.customServers)
            (0 until arr.length()).mapNotNull {
                try { TestPoint().fromJson(arr.getJSONObject(it)) } catch (_: Exception) { null }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    @Throws(IllegalArgumentException::class)
    fun add(name: String, url: String) {
        require(name.isNotBlank()) { "Name cannot be empty" }
        val trimmed = url.trim().trimEnd('/')
        val uri = URI(if (trimmed.contains("://")) trimmed else "https://$trimmed")
        val host = uri.host ?: throw IllegalArgumentException("Invalid URL")
        //IPv6 literals must stay bracketed inside the URL
        val bracketedHost = if (host.contains(":") && !host.startsWith("[")) "[$host]" else host
        // Connection only uses host:port of "server"; any sub-path must live in the endpoint fields.
        // "//host" makes Connection try HTTPS first and fall back to HTTP.
        val scheme = when {
            trimmed.startsWith("http://") -> "http://"
            trimmed.startsWith("https://") -> "https://"
            else -> "//"
        }
        val server = buildString {
            append(scheme)
            append(bracketedHost)
            if (uri.port != -1) append(":${uri.port}")
        }
        val basePath = uri.path.trim('/')
        fun endpoint(file: String) = if (basePath.isEmpty()) file else "$basePath/$file"
        val json = JSONObject()
        json.put("name", name.trim())
        json.put("server", server)
        json.put("dlURL", endpoint("garbage.php"))
        json.put("ulURL", endpoint("empty.php"))
        json.put("pingURL", endpoint("empty.php"))
        json.put("getIpURL", endpoint("getIP.php"))
        val arr = try { JSONArray(Settings.customServers) } catch (_: Exception) { JSONArray() }
        for (i in 0 until arr.length()) {
            val existing = arr.getJSONObject(i)
            if (existing.getString("server") == server && existing.getString("name") == name.trim()) return
        }
        arr.put(json)
        Settings.customServers = arr.toString()
    }

    fun remove(testPoint: TestPoint) {
        val arr = try { JSONArray(Settings.customServers) } catch (_: Exception) { JSONArray() }
        val kept = JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            if (o.getString("name") != testPoint.name || o.getString("server") != testPoint.server) kept.put(o)
        }
        Settings.customServers = kept.toString()
    }

    fun isCustom(testPoint: TestPoint): Boolean =
        load().any { it.name == testPoint.name && it.server == testPoint.server }

}
