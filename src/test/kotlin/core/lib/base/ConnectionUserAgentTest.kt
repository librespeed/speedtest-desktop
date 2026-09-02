package core.lib.base

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConnectionUserAgentTest {

    @Test
    fun userAgentNamesTheProductVersionAndPlatform() {
        val ua = Connection.USER_AGENT

        assertTrue(ua.startsWith("librespeed-desktop/"), "unexpected product: $ua")
        assertTrue(
            Regex("""^librespeed-desktop/\S+ \([a-z0-9._-]+; [a-z0-9._-]+\)$""").matches(ua),
            "unexpected shape: $ua",
        )
        // The header is one line or it splits the request it travels in.
        assertFalse(ua.contains('\r') || ua.contains('\n'), "line break in: $ua")
    }

    /** A packaged build passes -Dapp.version; an unpackaged run says so. */
    @Test
    fun versionComesFromTheBuild() {
        val previous = System.getProperty("app.version")
        try {
            System.setProperty("app.version", "1.2.3")
            assertTrue(
                Connection.buildUserAgent().startsWith("librespeed-desktop/1.2.3 ("),
                "build version ignored: ${Connection.buildUserAgent()}",
            )

            System.clearProperty("app.version")
            assertTrue(
                Connection.buildUserAgent().startsWith("librespeed-desktop/dev ("),
                "missing version should read dev: ${Connection.buildUserAgent()}",
            )
        } finally {
            if (previous != null) System.setProperty("app.version", previous)
            else System.clearProperty("app.version")
        }
    }

    /** The header the server actually receives, read off a real socket. */
    @Test
    fun userAgentReachesTheServerVerbatim() {
        val server = ServerSocket(0)
        var seen: String? = null

        val accepting = thread {
            server.accept().use { socket ->
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.isEmpty()) break
                    if (line.startsWith("User-Agent: ")) seen = line.removePrefix("User-Agent: ")
                }
                socket.getOutputStream().write("HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n".toByteArray())
            }
        }

        try {
            val connection = Connection("http://127.0.0.1:${server.localPort}")
            connection.GET("/empty", false)
            accepting.join(10_000)
            assertEquals(Connection.USER_AGENT, seen)
            connection.close()
        } finally {
            server.close()
        }
    }
}
