package core.lib

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Regression: the loader used to open the list URL twice per attempt, leaking the first stream. */
class ServerListLoaderSingleFetchTest {

    @Test
    fun serverListIsFetchedWithASingleRequest() {
        val body = """[{"name":"Local","server":"http://127.0.0.1/","dlURL":"garbage.php","ulURL":"empty.php","pingURL":"empty.php","getIpURL":"getIP.php"}]"""
        val requests = AtomicInteger()
        val server = ServerSocket(0)
        val accepting = thread {
            try {
                while (true) {
                    val socket = server.accept()
                    thread {
                        socket.use { sck ->
                            val reader = BufferedReader(InputStreamReader(sck.getInputStream()))
                            val requestLine = reader.readLine() ?: return@use
                            if (requestLine.startsWith("GET")) requests.incrementAndGet()
                            while (true) {
                                val line = reader.readLine() ?: break
                                if (line.isEmpty()) break
                            }
                            val payload = body.toByteArray()
                            val out = sck.getOutputStream()
                            out.write("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${payload.size}\r\nConnection: close\r\n\r\n".toByteArray())
                            out.write(payload)
                            out.flush()
                        }
                    }
                }
            } catch (_: Throwable) { } //closing the server socket ends the loop
        }
        try {
            val libreSpeed = LibreSpeed()
            assertTrue(libreSpeed.loadServerList("http://127.0.0.1:${server.localPort}/servers.json"))
            assertEquals(1, libreSpeed.testPoints.size)
            assertEquals(1, requests.get(), "the list endpoint must be hit exactly once")
        } finally {
            server.close()
            accepting.join(5000)
        }
    }
}
