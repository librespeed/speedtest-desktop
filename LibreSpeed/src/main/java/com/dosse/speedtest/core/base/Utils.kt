package com.dosse.speedtest.core.base

import java.net.URLEncoder

object Utils {
    fun urlEncode(s: String?): String? {
        return try {
            URLEncoder.encode(s, "utf-8")
        } catch (t: Throwable) {
            null
        }
    }

    fun sleep(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch (t: Throwable) {
        }
    }

    fun sleep(ms: Long, ns: Int) {
        try {
            Thread.sleep(ms, ns)
        } catch (t: Throwable) {
        }
    }

    fun url_sep(url: String): String {
        return if (url.contains("?")) "&" else "?"
    }
}