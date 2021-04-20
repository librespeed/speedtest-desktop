package com.dosse.speedtest.core.serverSelector

import com.dosse.speedtest.widget.json.JSONObject

class TestPoint {
    val name: String?
    val server: String?
    val dlURL: String?
    val ulURL: String?
    val pingURL: String?
    val getIpURL: String?
    var ping = -1f

    constructor(name: String?, server: String?, dlURL: String?, ulURL: String?, pingURL: String?, getIpURL: String?) {
        this.name = name
        this.server = server
        this.dlURL = dlURL
        this.ulURL = ulURL
        this.pingURL = pingURL
        this.getIpURL = getIpURL
    }

    constructor(json: com.dosse.speedtest.widget.json.JSONObject) {
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
    }
}