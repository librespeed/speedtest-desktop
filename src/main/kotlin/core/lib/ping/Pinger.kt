package core.lib.ping

import core.lib.base.Connection

abstract class Pinger(private val c: Connection, private val path: String) : Thread() {
    private var stopASAP = false
    override fun run() {
        try {
            val s = path
            val `in` = c.inputStream
            while (true) {
                if (stopASAP) break
                c.GET(s, true)
                if (stopASAP) break
                var t = System.nanoTime()
                var chunked = false
                var ok = false
                while (true) {
                    var l = c.readLineUnbuffered() ?: break
                    l = l.trim { it <= ' ' }.toLowerCase()
                    if (l == "transfer-encoding: chunked") chunked = true
                    if (l.contains("200 ok")) ok = true
                    if (l.trim { it <= ' ' }.isEmpty()) {
                        if (chunked) {
                            c.readLineUnbuffered()
                            c.readLineUnbuffered()
                        }
                        break
                    }
                }
                if (!ok) throw Exception("Did not get a 200")
                t = System.nanoTime() - t
                if (stopASAP) break
                if (!onPong(t / 2)) break
            }
            c.close()
        } catch (t: Throwable) {
            try {
                c.close()
            } catch (t1: Throwable) {
            }
            onError(t.toString())
        }
    }

    abstract fun onPong(ns: Long): Boolean
    abstract fun onError(err: String?)
    fun stopASAP() {
        stopASAP = true
    }

    init {
        start()
    }
}