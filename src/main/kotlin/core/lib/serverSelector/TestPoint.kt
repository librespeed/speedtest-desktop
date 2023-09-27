package core.lib.serverSelector

import util.json.JSONObject

data class TestPoint(
    var name: String? = null,
    var server: String? = null,
    var dlURL: String? = null,
    var ulURL: String? = null,
    var pingURL: String? = null,
    var getIpURL: String? = null,
    var ping : Float = -1f
) {

    fun fromJson (json: JSONObject) : TestPoint {
        try {
            name = json.getString("name")
            requireNotNull(name) { "Missing name field" }
            server = json.getString("server")
            requireNotNull(server) { "Missing server field" }
            dlURL = json.getString("dlURL")
            requireNotNull(dlURL) { "Missing dlURL field" }
            ulURL = json.getString("ulURL")
            requireNotNull(ulURL) { "Missing ulURL field" }
            pingURL = json.getString("pingURL")
            requireNotNull(pingURL) { "Missing pingURL field" }
            getIpURL = json.getString("getIpURL")
            requireNotNull(getIpURL) { "Missing getIpURL field" }
        } catch (t: Exception) {
            throw IllegalArgumentException("Invalid JSON")
        }
        return this
    }

}