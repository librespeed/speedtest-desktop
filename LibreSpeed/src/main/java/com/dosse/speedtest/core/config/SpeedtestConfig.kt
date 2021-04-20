package com.dosse.speedtest.core.config

import com.google.gson.JsonObject

class SpeedtestConfig {
    private var dl_ckSize = 100
    private var ul_ckSize = 20
    private var dl_parallelStreams = 3
    private var ul_parallelStreams = 3
    private var dl_streamDelay = 300
    private var ul_streamDelay = 300
    private var dl_graceTime = 1.5
    private var ul_graceTime = 1.5
    var dl_connectTimeout = 5000
    var dl_soTimeout = 10000
    var ul_connectTimeout = 5000
    var ul_soTimeout = 10000
    var ping_connectTimeout = 2000
    var ping_soTimeout = 5000
    var dl_recvBuffer = -1
    var dl_sendBuffer = -1
    var ul_recvBuffer = -1
    var ul_sendBuffer = 16384
    var ping_recvBuffer = -1
    var ping_sendBuffer = -1
    private var errorHandlingMode = ONERROR_ATTEMPT_RESTART
    private var time_dl_max = 15
    private var time_ul_max = 15
    var time_auto = true
    private var count_ping = 10
    var telemetry_extra = ""
    private var overheadCompensationFactor = 1.06
    var getIP_isp = true
    private var getIP_distance = DISTANCE_KM
    var useMebibits = false
    private var test_order = "IP_D_U"
    private fun check() {
        require(dl_ckSize >= 1) { "dl_ckSize must be at least 1" }
        require(ul_ckSize >= 1) { "ul_ckSize must be at least 1" }
        require(dl_parallelStreams >= 1) { "dl_parallelStreams must be at least 1" }
        require(ul_parallelStreams >= 1) { "ul_parallelStreams must be at least 1" }
        require(dl_streamDelay >= 0) { "dl_streamDelay must be at least 0" }
        require(ul_streamDelay >= 0) { "ul_streamDelay must be at least 0" }
        require(dl_graceTime >= 0) { "dl_graceTime must be at least 0" }
        require(ul_graceTime >= 0) { "ul_graceTime must be at least 0" }
        require(errorHandlingMode == ONERROR_FAIL || errorHandlingMode == ONERROR_ATTEMPT_RESTART || errorHandlingMode == ONERROR_MUST_RESTART) { "errorHandlingMode must be fail, attempt-restart, or must-restart" }
        require(time_dl_max >= 1) { "time_dl_max must be at least 1" }
        require(time_ul_max >= 1) { "time_ul_max must be at least 1" }
        require(count_ping >= 1) { "count_ping must be at least 1" }
        require(overheadCompensationFactor >= 1) { "overheadCompensationFactor must be at least 1" }
        require(getIP_distance == DISTANCE_NO || getIP_distance == DISTANCE_KM || getIP_distance == DISTANCE_MILES) { "getIP_distance must be no, km or miles" }
        for (c in test_order.toCharArray()) {
            require(c == 'I' || c == 'P' || c == 'D' || c == 'U' || c == '_') { "test_order can only contain characters I, P, D, U, _" }
        }
    }

    constructor() {
        check()
    }

    constructor(
        dl_ckSize: Int,
        ul_ckSize: Int,
        dl_parallelStreams: Int,
        ul_parallelStreams: Int,
        dl_streamDelay: Int,
        ul_streamDelay: Int,
        dl_graceTime: Double,
        ul_graceTime: Double,
        dl_connectTimeout: Int,
        dl_soTimeout: Int,
        ul_connectTimeout: Int,
        ul_soTimeout: Int,
        ping_connectTimeout: Int,
        ping_soTimeout: Int,
        dl_recvBuffer: Int,
        dl_sendBuffer: Int,
        ul_recvBuffer: Int,
        ul_sendBuffer: Int,
        ping_recvBuffer: Int,
        ping_sendBuffer: Int,
        errorHandlingMode: String,
        time_dl_max: Int,
        time_ul_max: Int,
        time_auto: Boolean,
        count_ping: Int,
        telemetry_extra: String,
        overheadCompensationFactor: Double,
        getIP_isp: Boolean,
        getIP_distance: String,
        useMebibits: Boolean,
        test_order: String
    ) {
        this.dl_ckSize = dl_ckSize
        this.ul_ckSize = ul_ckSize
        this.dl_parallelStreams = dl_parallelStreams
        this.ul_parallelStreams = ul_parallelStreams
        this.dl_streamDelay = dl_streamDelay
        this.ul_streamDelay = ul_streamDelay
        this.dl_graceTime = dl_graceTime
        this.ul_graceTime = ul_graceTime
        this.dl_connectTimeout = dl_connectTimeout
        this.dl_soTimeout = dl_soTimeout
        this.ul_connectTimeout = ul_connectTimeout
        this.ul_soTimeout = ul_soTimeout
        this.ping_connectTimeout = ping_connectTimeout
        this.ping_soTimeout = ping_soTimeout
        this.dl_recvBuffer = dl_recvBuffer
        this.dl_sendBuffer = dl_sendBuffer
        this.ul_recvBuffer = ul_recvBuffer
        this.ul_sendBuffer = ul_sendBuffer
        this.ping_recvBuffer = ping_recvBuffer
        this.ping_sendBuffer = ping_sendBuffer
        this.errorHandlingMode = errorHandlingMode
        this.time_dl_max = time_dl_max
        this.time_ul_max = time_ul_max
        this.time_auto = time_auto
        this.count_ping = count_ping
        this.telemetry_extra = telemetry_extra
        this.overheadCompensationFactor = overheadCompensationFactor
        this.getIP_isp = getIP_isp
        this.getIP_distance = getIP_distance
        this.useMebibits = useMebibits
        this.test_order = test_order
        check()
    }

    constructor(json: JsonObject) {
        try {
            if (json.has("dl_ckSize")) dl_ckSize = json["dl_ckSize"].asInt
            if (json.has("ul_ckSize")) ul_ckSize = json["ul_ckSize"].asInt
            if (json.has("dl_parallelStreams")) dl_parallelStreams = json["dl_parallelStreams"].asInt
            if (json.has("ul_parallelStreams")) ul_parallelStreams = json["ul_parallelStreams"].asInt
            if (json.has("dl_streamDelay")) dl_streamDelay = json["dl_streamDelay"].asInt
            if (json.has("ul_streamDelay")) ul_streamDelay = json["ul_streamDelay"].asInt
            if (json.has("dl_graceTime")) dl_graceTime = json["dl_graceTime"].asInt.toDouble()
            if (json.has("ul_graceTime")) ul_graceTime = json["ul_graceTime"].asInt.toDouble()
            if (json.has("dl_connectTimeout")) dl_connectTimeout = json["dl_connectTimeout"].asInt
            if (json.has("ul_connectTimeout")) ul_connectTimeout = json["ul_connectTimeout"].asInt
            if (json.has("ping_connectTimeout")) ping_connectTimeout = json["ping_connectTimeout"].asInt
            if (json.has("dl_soTimeout")) dl_soTimeout = json["dl_soTimeout"].asInt
            if (json.has("ul_soTimeout")) ul_soTimeout = json["ul_soTimeout"].asInt
            if (json.has("ping_soTimeout")) ping_soTimeout = json["ping_soTimeout"].asInt
            if (json.has("dl_recvBuffer")) dl_recvBuffer = json["dl_recvBuffer"].asInt
            if (json.has("ul_recvBuffer")) ul_recvBuffer = json["ul_recvBuffer"].asInt
            if (json.has("ping_recvBuffer")) ping_recvBuffer = json["ping_recvBuffer"].asInt
            if (json.has("dl_sendBuffer")) dl_sendBuffer = json["dl_sendBuffer"].asInt
            if (json.has("ul_sendBuffer")) ul_sendBuffer = json["ul_sendBuffer"].asInt
            if (json.has("ping_sendBuffer")) ping_sendBuffer = json["ping_sendBuffer"].asInt
            if (json.has("errorHandlingMode")) errorHandlingMode = json["errorHandlingMode"].asString
            if (json.has("time_dl_max")) time_dl_max = json["time_dl_max"].asInt
            if (json.has("time_ul_max")) time_ul_max = json["time_ul_max"].asInt
            if (json.has("count_ping")) count_ping = json["count_ping"].asInt
            if (json.has("telemetry_extra")) telemetry_extra = json["telemetry_extra"].asString
            if (json.has("overheadCompensationFactor")) overheadCompensationFactor =
                json["overheadCompensationFactor"].asDouble
            if (json.has("getIP_isp")) getIP_isp = json["getIP_isp"].asBoolean
            if (json.has("getIP_distance")) getIP_distance = json["getIP_distance"].asString
            if (json.has("test_order")) test_order = json["test_order"].asString
            if (json.has("useMebibits")) useMebibits = json["useMebibits"].asBoolean
            check()
        } catch (t: Exception) {
            throw IllegalArgumentException("Invalid JSON ($t)")
        }
    }

    fun getDl_ckSize(): Int {
        return dl_ckSize
    }

    fun getUl_ckSize(): Int {
        return ul_ckSize
    }

    fun getDl_parallelStreams(): Int {
        return dl_parallelStreams
    }

    fun getUl_parallelStreams(): Int {
        return ul_parallelStreams
    }

    fun getDl_streamDelay(): Int {
        return dl_streamDelay
    }

    fun getUl_streamDelay(): Int {
        return ul_streamDelay
    }

    fun getDl_graceTime(): Double {
        return dl_graceTime
    }

    fun getUl_graceTime(): Double {
        return ul_graceTime
    }

    fun getErrorHandlingMode(): String {
        return errorHandlingMode
    }

    fun getTime_dl_max(): Int {
        return time_dl_max
    }

    fun getTime_ul_max(): Int {
        return time_ul_max
    }

    fun getCount_ping(): Int {
        return count_ping
    }

    fun getOverheadCompensationFactor(): Double {
        return overheadCompensationFactor
    }

    fun getGetIP_distance(): String {
        return getIP_distance
    }

    fun getTest_order(): String {
        return test_order
    }

    fun setDl_ckSize(dl_ckSize: Int) {
        require(dl_ckSize >= 1) { "dl_ckSize must be at least 1" }
        this.dl_ckSize = dl_ckSize
    }

    fun setUl_ckSize(ul_ckSize: Int) {
        require(ul_ckSize >= 1) { "ul_ckSize must be at least 1" }
        this.ul_ckSize = ul_ckSize
    }

    fun setDl_parallelStreams(dl_parallelStreams: Int) {
        require(dl_parallelStreams >= 1) { "dl_parallelStreams must be at least 1" }
        this.dl_parallelStreams = dl_parallelStreams
    }

    fun setUl_parallelStreams(ul_parallelStreams: Int) {
        require(ul_parallelStreams >= 1) { "ul_parallelStreams must be at least 1" }
        this.ul_parallelStreams = ul_parallelStreams
    }

    fun setDl_streamDelay(dl_streamDelay: Int) {
        require(dl_streamDelay >= 0) { "dl_streamDelay must be at least 0" }
        this.dl_streamDelay = dl_streamDelay
    }

    fun setUl_streamDelay(ul_streamDelay: Int) {
        require(ul_streamDelay >= 0) { "ul_streamDelay must be at least 0" }
        this.ul_streamDelay = ul_streamDelay
    }

    fun setDl_graceTime(dl_graceTime: Double) {
        require(dl_graceTime >= 0) { "dl_graceTime must be at least 0" }
        this.dl_graceTime = dl_graceTime
    }

    fun setUl_graceTime(ul_graceTime: Double) {
        require(ul_graceTime >= 0) { "ul_graceTime must be at least 0" }
        this.ul_graceTime = ul_graceTime
    }

    fun setErrorHandlingMode(errorHandlingMode: String) {
        require(errorHandlingMode == ONERROR_FAIL || errorHandlingMode == ONERROR_ATTEMPT_RESTART || errorHandlingMode == ONERROR_MUST_RESTART) { "errorHandlingMode must be fail, attempt-restart, or must-restart" }
        this.errorHandlingMode = errorHandlingMode
    }

    fun setTime_dl_max(time_dl_max: Int) {
        require(time_dl_max >= 1) { "time_dl_max must be at least 1" }
        this.time_dl_max = time_dl_max
    }

    fun setTime_ul_max(time_ul_max: Int) {
        require(time_ul_max >= 1) { "time_ul_max must be at least 1" }
        this.time_ul_max = time_ul_max
    }

    fun setCount_ping(count_ping: Int) {
        require(count_ping >= 1) { "count_ping must be at least 1" }
        this.count_ping = count_ping
    }

    fun setOverheadCompensationFactor(overheadCompensationFactor: Double) {
        require(overheadCompensationFactor >= 1) { "overheadCompensationFactor must be at least 1" }
        this.overheadCompensationFactor = overheadCompensationFactor
    }

    fun setGetIP_distance(getIP_distance: String) {
        require(getIP_distance == DISTANCE_NO || getIP_distance == DISTANCE_KM || getIP_distance == DISTANCE_MILES) { "getIP_distance must be no, km or miles" }
        this.getIP_distance = getIP_distance
    }

    fun setTest_order(test_order: String) {
        for (c in test_order.toCharArray()) {
            require(c == 'I' || c == 'P' || c == 'D' || c == 'U' || c == '_') { "test_order can only contain characters I, P, D, U, _" }
        }
        this.test_order = test_order
    }

    fun clone(): SpeedtestConfig {
        return SpeedtestConfig(
            dl_ckSize,
            ul_ckSize,
            dl_parallelStreams,
            ul_parallelStreams,
            dl_streamDelay,
            ul_streamDelay,
            dl_graceTime,
            ul_graceTime,
            dl_connectTimeout,
            dl_soTimeout,
            ul_connectTimeout,
            ul_soTimeout,
            ping_connectTimeout,
            ping_soTimeout,
            dl_recvBuffer,
            dl_sendBuffer,
            ul_recvBuffer,
            ul_sendBuffer,
            ping_recvBuffer,
            ping_sendBuffer,
            errorHandlingMode,
            time_dl_max,
            time_ul_max,
            time_auto,
            count_ping,
            telemetry_extra,
            overheadCompensationFactor,
            getIP_isp,
            getIP_distance,
            useMebibits,
            test_order
        )
    }

    companion object {
        const val ONERROR_FAIL = "fail"
        const val ONERROR_ATTEMPT_RESTART = "attempt-restart"
        const val ONERROR_MUST_RESTART = "must-restart"
        const val DISTANCE_NO = "no"
        const val DISTANCE_MILES = "mi"
        const val DISTANCE_KM = "km"
    }
}