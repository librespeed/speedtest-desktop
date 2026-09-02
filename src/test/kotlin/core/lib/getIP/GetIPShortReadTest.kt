package core.lib.getIP

import core.lib.base.Connection
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetIPShortReadTest {

    /** The body arrives in two TCP segments on a keep-alive connection; a single
     * read must not hand back a truncated, NUL-padded result, and a multibyte
     * body must not hang the reader waiting for chars that never come. */
    @Test
    fun bodySplitAcrossReadsArrivesComplete() {
        val body = """{"processedString":"1.2.3.4 - Fürigen Networks"}"""
        val bytes = body.toByteArray(Charsets.UTF_8)
        val server = ServerSocket(0)
        val done = CountDownLatch(1)

        val serving = thread {
            try {
                server.accept().use { socket ->
                    val reader = socket.getInputStream().bufferedReader()
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isEmpty()) break
                    }
                    val out = socket.getOutputStream()
                    out.write("HTTP/1.1 200 OK\r\nContent-Length: ${bytes.size}\r\n\r\n".toByteArray())
                    out.write(bytes, 0, bytes.size / 2)
                    out.flush()
                    Thread.sleep(300)
                    out.write(bytes, bytes.size / 2, bytes.size - bytes.size / 2)
                    out.flush()
                    //keep-alive: the connection stays open, so the client cannot rely on EOF
                    done.await(10, TimeUnit.SECONDS)
                }
            } catch (_: Throwable) {
            }
        }

        var received: String? = null
        var error: String? = null
        val connection = Connection("http://127.0.0.1:${server.localPort}")
        object : GetIP(connection, "/getIP.php", false, null) {
            override fun onDataReceived(data: String?) {
                received = data
                done.countDown()
            }

            override fun onError(err: String?) {
                error = err
                done.countDown()
            }
        }

        try {
            assertTrue(done.await(10, TimeUnit.SECONDS), "getIP never completed")
            assertNull(error)
            assertEquals(body, received)
        } finally {
            server.close()
            serving.join(5_000)
        }
    }
}
