package core.lib.base

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals

/** A server URL with a path keeps that path in front of relative endpoints. */
class ConnectionBasePathTest {
    private fun requestLine(serverPath: String, endpoint: String): String {
        val server = ServerSocket(0); var line: String? = null
        val t = thread { server.accept().use { s ->
            line = BufferedReader(InputStreamReader(s.getInputStream())).readLine()
            s.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n".toByteArray()) } }
        Connection("http://127.0.0.1:${server.localPort}$serverPath").apply { GET(endpoint, false); close() }
        t.join(5000); server.close()
        return line ?: "no request"
    }

    @Test fun relativeEndpointLivesUnderTheBasePath() = assertEquals("GET /backend/empty.php HTTP/1.1", requestLine("/backend", "empty.php"))
    @Test fun trailingSlashDoesNotDouble() = assertEquals("GET /librespeed/empty.php HTTP/1.1", requestLine("/librespeed/", "empty.php"))
    @Test fun absoluteEndpointIgnoresTheBasePath() = assertEquals("GET /root.php HTTP/1.1", requestLine("/backend", "/root.php"))
    @Test fun noBasePathBehavesAsBefore() = assertEquals("GET /empty.php HTTP/1.1", requestLine("", "empty.php"))
}
