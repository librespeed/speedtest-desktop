package core.lib.ping

import core.lib.base.Connection

abstract class Pinger(private val c: Connection, private val path: String) : Thread() {
    @Volatile
    private var stopASAP = false
    override fun run() {
        try {
            val s = path
            while (true) {
                if (stopASAP) break
                c.GET(s, true)
                if (stopASAP) break
                var t = System.nanoTime()
                var chunked = false
                var ok = false
                while (true) {
                    var l = c.readLineUnbuffered() ?: break
                    l = l.trim { it <= ' ' }.lowercase()
                    if (l == "transfer-encoding: chunked") chunked = true
                    if (l.startsWith("http/") && l.split(" ").getOrNull(1)?.startsWith("2") == true) ok = true
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
                //the full request-response time, as the web client and the CLI report it;
                //halving it used to cancel out a round trip that Nagle's algorithm added
                if (!onPong(t)) break
            }
            c.close()
        } catch (t: Throwable) {
            try {
                c.close()
            } catch (_: Throwable) {
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