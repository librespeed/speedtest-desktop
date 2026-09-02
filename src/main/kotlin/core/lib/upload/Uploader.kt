package core.lib.upload

import core.lib.base.Connection
import java.util.*

abstract class Uploader(private val c: Connection, private val path: String, ckSize: Int) :
    Thread() {
    @Volatile
    private var stopASAP = false
    @Volatile
    private var resetASAP = false
    @Volatile
    private var totUploaded: Long = 0
    private val garbage: ByteArray
    override fun run() {
        try {
            val s = path
            var lastProgressEvent = System.currentTimeMillis()
            val out = c.outputStream
            while (true) {
                if (stopASAP) break
                c.POST(s, true, "application/octet-stream", garbage.size.toLong())
                var offset = 0
                while (offset < garbage.size) {
                    if (stopASAP) break
                    val l = if (offset + BUFFER_SIZE >= garbage.size) garbage.size - offset else BUFFER_SIZE
                    out!!.write(garbage, offset, l)
                    if (stopASAP) break
                    if (resetASAP) {
                        totUploaded = 0
                        resetASAP = false
                    }
                    totUploaded += l.toLong()
                    if (System.currentTimeMillis() - lastProgressEvent > 200) {
                        lastProgressEvent = System.currentTimeMillis()
                        onProgress(totUploaded)
                    }
                    offset += BUFFER_SIZE
                }
                if (stopASAP) break
                while (c.readLineUnbuffered()!!.trim { it <= ' ' }.isNotEmpty());
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

    fun stopASAP() {
        stopASAP = true
    }

    abstract fun onProgress(uploaded: Long)
    abstract fun onError(err: String?)
    fun resetUploadCounter() {
        resetASAP = true
    }

    val uploaded: Long
        get() = if (resetASAP) 0 else totUploaded

    companion object {
        private const val BUFFER_SIZE = 16384
        private var shared: ByteArray? = null

        //the payload is random so it cannot be compressed on the way; one copy per
        //process is enough -- filling 20 MB per stream and per restart stalled the
        //start of the upload phase and spiked the heap
        @Synchronized
        private fun sharedGarbage(ckSize: Int): ByteArray {
            val size = ckSize * 1048576
            return shared?.takeIf { it.size == size }
                ?: ByteArray(size).also { Random(System.nanoTime()).nextBytes(it); shared = it }
        }
    }

    init {
        garbage = sharedGarbage(ckSize)
        start()
    }
}