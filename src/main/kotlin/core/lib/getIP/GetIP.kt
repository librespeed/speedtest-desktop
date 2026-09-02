package core.lib.getIP

import core.lib.base.Connection
import core.lib.base.Utils.url_sep
import core.lib.config.SpeedtestConfig
import java.io.BufferedReader

abstract class GetIP(private val c: Connection, private val path: String, private val isp: Boolean, distance: String?) : Thread() {
    private val distance: String?
    override fun run() {
        try {
            var s = path
            if (isp) {
                s += url_sep(s) + "isp=true"
                if (distance != SpeedtestConfig.DISTANCE_NO) {
                    s += url_sep(s) + "distance=" + distance
                }
            }
            c.GET(s, true)
            val h: HashMap<String, String> = c.parseResponseHeaders()
            val br = BufferedReader(c.inputStreamReader)
            if (h["content-length"] != null) {
                //standard encoding. content-length counts bytes, the reader hands out chars;
                //a UTF-8 char is at least one byte, so the buffer is large enough, and the
                //body is complete once its chars encode back to content-length bytes --
                //waiting for buf.size chars instead would hang on any multibyte body
                val contentLength = h["content-length"]!!.toInt()
                val buf = CharArray(contentLength)
                var chars = 0
                while (chars < buf.size) {
                    val n = br.read(buf, chars, buf.size - chars)
                    if (n < 0) break
                    chars += n
                    if (String(buf, 0, chars).toByteArray(Charsets.UTF_8).size >= contentLength) break
                }
                val data = String(buf, 0, chars)
                onDataReceived(data)
            } else {
                //chunked encoding hack. TODO: improve this garbage with proper chunked support
                c.readLineUnbuffered() //ignore first line
                val data = c.readLineUnbuffered() //actual info we want
                c.readLineUnbuffered() //ignore last line (0)
                onDataReceived(data)
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

    abstract fun onDataReceived(data: String?)
    abstract fun onError(err: String?)

    init {
        require(distance == null || distance == SpeedtestConfig.DISTANCE_KM || distance == SpeedtestConfig.DISTANCE_MILES) { "Distance must be null, mi or km" }
        this.distance = distance
        start()
    }
}