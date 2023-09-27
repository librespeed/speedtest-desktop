package core.lib.config

import util.json.JSONObject

class TelemetryConfig {
    var telemetryLevel = LEVEL_DISABLED
        private set
    var server: String? = null
        private set
    var path: String? = null
        private set
    var shareURL: String? = null
        private set

    private fun check() {
        require(telemetryLevel == LEVEL_DISABLED || telemetryLevel == LEVEL_BASIC || telemetryLevel == LEVEL_FULL) { "Telemetry level must be disabled, basic or full" }
    }

    constructor() {}
    constructor(telemetryLevel: String, server: String?, path: String?, shareURL: String?) {
        this.telemetryLevel = telemetryLevel
        this.server = server
        this.path = path
        this.shareURL = shareURL
        check()
    }

    constructor(json: JSONObject) {
        try {
            if (json.has("telemetryLevel")) telemetryLevel = json["telemetryLevel"].toString()
            if (json.has("server")) server = json["server"].toString()
            if (json.has("path")) path = json["path"].toString()
            if (json.has("shareURL")) shareURL = json["shareURL"].toString()
            check()
        } catch (t: Exception) {
            throw IllegalArgumentException("Invalid JSON ($t)")
        }
    }

    fun clone(): TelemetryConfig {
        return TelemetryConfig(telemetryLevel, server, path, shareURL)
    }

    companion object {
        const val LEVEL_DISABLED = "disabled"
        const val LEVEL_BASIC = "basic"
        const val LEVEL_FULL = "full"
    }
}