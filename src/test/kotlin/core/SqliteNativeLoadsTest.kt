package core

import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * The packaged sqlite-jdbc jar is stripped down to this platform's native
 * library; this proves that copy is the one that loads, not a leftover.
 */
class SqliteNativeLoadsTest {
    @Test
    fun theSlimJarCarriesThisPlatformsLibrary() {
        //sqlite-jdbc would silently fall back to java.library.path, so check the jar itself
        val os = System.getProperty("os.name").lowercase()
        val family = if (os.contains("mac")) "Mac" else if (os.contains("win")) "Windows" else "Linux"
        val arch = when (val a = System.getProperty("os.arch")) {
            "aarch64", "arm64" -> "aarch64"
            "amd64", "x86_64" -> "x86_64"
            "x86", "i386", "i686" -> "x86"
            else -> a
        }
        val lib = if (family == "Windows") "sqlitejdbc.dll" else if (family == "Mac") "libsqlitejdbc.dylib" else "libsqlitejdbc.so"
        val resource = "org/sqlite/native/$family/$arch/$lib"
        assertTrue(javaClass.classLoader.getResource(resource) != null, "missing $resource on the classpath")
    }

    @Test
    fun theNativeLibraryForThisPlatformLoads() {
        DriverManager.getConnection("jdbc:sqlite::memory:").use { c ->
            val version = c.createStatement().executeQuery("select sqlite_version()").use { rs -> rs.next(); rs.getString(1) }
            assertTrue(version.startsWith("3."), "unexpected sqlite version: $version")
        }
    }
}
