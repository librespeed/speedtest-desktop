package core.lib.base

import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.util.*
import javax.net.SocketFactory
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class Connection @JvmOverloads constructor(
    url: String,
    connectTimeout: Int = DEFAULT_CONNECT_TIMEOUT,
    soTimeout: Int = DEFAULT_SO_TIMEOUT,
    recvBuffer: Int = -1,
    sendBuffer: Int = -1
) {
    private var socket: Socket? = null
    //the part of the server URL after the host: a server list may point at
    //"https://host/backend", and every relative endpoint then lives under it
    private var basePath = ""
    private var host: String? = null
    private var port = 0
    private var mode = MODE_NOT_SET
    val isIPv6: Boolean
        get() = socket?.inetAddress is java.net.Inet6Address
    val inputStream: InputStream?
        get() = try {
            socket!!.getInputStream()
        } catch (t: Throwable) {
            null
        }
    val outputStream: OutputStream?
        get() {
            return try {
                socket!!.getOutputStream()
            } catch (t: Throwable) {
                null
            }
        }
    private var ps: PrintStream? = null
    val printStream: PrintStream?
        get() {
            if (ps == null) {
                ps = try {
                    PrintStream(outputStream, false, "utf-8")
                } catch (t: Throwable) {
                    null
                }
            }
            return ps
        }
    private var isr: InputStreamReader? = null
    val inputStreamReader: InputStreamReader?
        get() {
            if (isr == null) {
                isr = try {
                    InputStreamReader(inputStream, "utf-8")
                } catch (t: Throwable) {
                    null
                }
            }
            return isr
        }

    private fun resolvePath(path: String) = if (path.startsWith("/")) path else "$basePath/$path"

    @Throws(Exception::class)
    fun GET(path: String, keepAlive: Boolean) {
        val path2 = resolvePath(path)
        try {
            //one write per request: with Nagle's algorithm every further small write
            //waits for the ACK of the previous one, which added a whole round trip to every ping
            val request = buildString {
                append("GET $path2 HTTP/1.1\r\n")
                append("Host: $host\r\n")
                append("User-Agent: $USER_AGENT\r\n")
                append("Connection: ${if (keepAlive) "keep-alive" else "close"}\r\n")
                append("Accept-Encoding: identity\r\n")
                if (Locale.getDefault() != null) append("Accept-Language: ${Locale.getDefault()}\r\n")
                append("\r\n")
            }
            val ps = printStream
            ps!!.print(request)
            ps.flush()
        } catch (t: Throwable) {
            throw Exception("Failed to send GET request")
        }
    }

    @Throws(Exception::class)
    fun POST(path: String, keepAlive: Boolean, contentType: String?, contentLength: Long) {
        val path2 = resolvePath(path)
        try {
            val request = buildString {
                append("POST $path2 HTTP/1.1\r\n")
                append("Host: $host\r\n")
                append("User-Agent: $USER_AGENT\r\n")
                append("Connection: ${if (keepAlive) "keep-alive" else "close"}\r\n")
                append("Accept-Encoding: identity\r\n")
                if (Locale.getDefault() != null) append("Accept-Language: ${Locale.getDefault()}\r\n")
                if (contentType != null) append("Content-Type: $contentType\r\n")
                append("Content-Encoding: identity\r\n")
                if (contentLength >= 0) append("Content-Length: $contentLength\r\n")
                append("\r\n")
            }
            val ps = printStream
            ps!!.print(request)
            ps.flush()
        } catch (t: Throwable) {
            throw Exception("Failed to send POST request")
        }
    }

    fun readLineUnbuffered(): String? {
        return try {
            val `in` = inputStreamReader
            val sb = StringBuilder()
            while (true) {
                val c = `in`!!.read()
                if (c == -1) break
                sb.append(c.toChar())
                if (c == '\n'.code) break
            }
            sb.toString()
        } catch (t: Throwable) {
            null
        }
    }

    @Throws(Exception::class)
    fun parseResponseHeaders(): HashMap<String, String> {
        return try {
            val ret = HashMap<String, String>()
            var s = readLineUnbuffered()
            val statusCode = s!!.trim { it <= ' ' }.split(" ").getOrNull(1)
            if (statusCode == null || !statusCode.startsWith("2")) throw Exception("Did not receive an HTTP 2xx (" + s.trim { it <= ' ' } + ")")
            while (true) {
                s = readLineUnbuffered()
                if (s!!.trim { it <= ' ' }.isEmpty()) break
                if (s.contains(":")) {
                    ret[s.substring(0, s.indexOf(":")).trim { it <= ' ' }.lowercase()] =
                        s.substring(s.indexOf(":") + 1).trim { it <= ' ' }
                }
            }
            ret
        } catch (t: Throwable) {
            throw Exception("Failed to get response headers ($t)")
        }
    }

    fun close() {
        try {
            socket!!.close()
        } catch (_: Throwable) { }
        socket = null
    }

    companion object {
        private const val MODE_NOT_SET = 0
        private const val MODE_HTTP = 1
        private const val MODE_HTTPS = 2
        /**
         * Product and version, then the platform -- the shape the LibreSpeed
         * CLIs and the Android client send, so a server sees one family across
         * the clients. The version comes from the build rather than a
         * constant, which would keep reporting a release the binary is not.
         */
        internal val USER_AGENT: String = buildUserAgent()

        internal fun buildUserAgent(): String {
            val version = System.getProperty("app.version")?.takeIf { it.isNotBlank() } ?: "dev"
            return "librespeed-desktop/$version (${osName()}; ${tag(System.getProperty("os.arch"))})"
        }

        /** `Mac OS X` and `Windows 11` become the names the other clients use. */
        private fun osName(): String {
            val raw = System.getProperty("os.name").orEmpty().lowercase()
            return when {
                raw.startsWith("mac") -> "macos"
                raw.startsWith("windows") -> "windows"
                else -> tag(raw.substringBefore(' '))
            }
        }

        /** Bounded and reduced to a conservative alphabet: this goes into a
         * header, where a stray line break would split the request itself. */
        private fun tag(value: String?): String =
            value.orEmpty()
                .lowercase()
                .filter { it in 'a'..'z' || it in '0'..'9' || it in "._-" }
                .take(24)
                .ifEmpty { "unknown" }
        private const val DEFAULT_CONNECT_TIMEOUT = 2000
        private const val DEFAULT_SO_TIMEOUT = 5000
    }

    init {
        var tryHTTP = false
        var tryHTTPS = false
        Locale.getDefault().toString()
        if (url.startsWith("http://")) {
            tryHTTP = true
            try {
                val u = URL(url)
                host = u.host
                port = u.port
                basePath = u.path.trimEnd('/')
            } catch (t: Throwable) {
                throw IllegalArgumentException("Malformed URL (HTTP)")
            }
        } else if (url.startsWith("https://")) {
            tryHTTPS = true
            try {
                val u = URL(url)
                host = u.host
                port = u.port
                basePath = u.path.trimEnd('/')
            } catch (t: Throwable) {
                throw IllegalArgumentException("Malformed URL (HTTPS)")
            }
        } else if (url.startsWith("//")) {
            tryHTTP = true
            tryHTTPS = true
            try {
                val u = URL("http:$url")
                host = u.host
                port = u.port
                basePath = u.path.trimEnd('/')
            } catch (t: Throwable) {
                throw IllegalArgumentException("Malformed URL (HTTP/HTTPS)")
            }
        } else {
            throw IllegalArgumentException("Malformed URL (Unknown or unspecified protocol)")
        }
        try {
            if (mode == MODE_NOT_SET && tryHTTPS) {
                val factory = SSLSocketFactory.getDefault()
                val ssl = factory.createSocket() as SSLSocket
                //an unconnected SSLSocket does not verify the peer's hostname unless told to;
                //must be set before the (lazy) handshake or any CA-valid cert is accepted
                val sp = ssl.sslParameters
                sp.endpointIdentificationAlgorithm = "HTTPS"
                ssl.sslParameters = sp
                socket = ssl
                if (connectTimeout > 0) {
                    socket!!.connect(InetSocketAddress(host, if (port == -1) 443 else port), connectTimeout)
                } else {
                    socket!!.connect(InetSocketAddress(host, if (port == -1) 443 else port))
                }
                mode = MODE_HTTPS
            }
        } catch (_: Throwable) {
            //a failed connect can leave the fd open (e.g. UnknownHostException on JDK's NioSocketImpl)
            try { socket?.close() } catch (_: Throwable) { }
            socket = null
        }
        try {
            if (mode == MODE_NOT_SET && tryHTTP) {
                val factory = SocketFactory.getDefault()
                socket = factory.createSocket()
                if (connectTimeout > 0) {
                    socket!!.connect(InetSocketAddress(host, if (port == -1) 80 else port), connectTimeout)
                } else {
                    socket!!.connect(InetSocketAddress(host, if (port == -1) 80 else port))
                }
                mode = MODE_HTTP
            }
        } catch (_: Throwable) {
            try { socket?.close() } catch (_: Throwable) { }
            socket = null
        }
        check(mode != MODE_NOT_SET) { "Failed to connect" }
        if (soTimeout > 0) {
            try {
                socket!!.soTimeout = soTimeout
            } catch (_: Throwable) { }
        }
        if (recvBuffer > 0) {
            try {
                socket!!.receiveBufferSize = recvBuffer
            } catch (_: Throwable) { }
        }
        if (sendBuffer > 0) {
            try {
                socket!!.sendBufferSize = sendBuffer
            } catch (_: Throwable) { }
        }
    }
}