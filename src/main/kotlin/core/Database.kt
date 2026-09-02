package core

import java.io.File
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

object Database {

    private const val APP_NAME: String = "librespeed-desktop"
    private fun getDatabasePath(): String {
        val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
        return if (osName.contains("linux")) {
            Paths.get(System.getProperty("user.home"), ".local", "share", APP_NAME).toString()
        } else if (osName.contains("windows")) {
            System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + APP_NAME
        } else if (osName.contains("mac")) {
            Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_NAME).toString()
        } else {
            //unknown OS (e.g. FreeBSD): fall back to the XDG default rather than refusing to start
            Paths.get(System.getProperty("user.home"), ".local", "share", APP_NAME).toString()
        }
    }

    private lateinit var connection: Connection
    fun initDB() {
        File(getDatabasePath()).mkdirs()
        val dbFile = File(getDatabasePath(), "librespeed.db")
        Class.forName("org.sqlite.JDBC")
        connection = DriverManager.getConnection("jdbc:sqlite:$dbFile")
        createTables()
    }

    private fun createTables() {
        //a failed CREATE must reach initDB's caller: swallowing it here means saveHistory hits a missing table later
        connection.createStatement().use {
            it.executeUpdate(
                "CREATE TABLE IF NOT EXISTS history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "netAdapter TEXT," +
                        "ping REAL," +
                        "jitter REAL," +
                        "download REAL," +
                        "upload REAL," +
                        "ispInfo TEXT," +
                        "testPoint TEXT," +
                        "date INTEGER," +
                        "shareUrl TEXT" +
                        ")"
            )
        }
        //migration for databases created before the shareUrl column existed
        val migration = connection.createStatement()
        try {
            migration.executeUpdate("ALTER TABLE history ADD COLUMN shareUrl TEXT")
        } catch (_: Exception) {
        } finally {
            migration.close()
        }
    }
    fun saveHistory(model: ModelHistory) {
        try {
            connection.prepareStatement("INSERT INTO history (netAdapter,ping,jitter,download,upload,ispInfo,testPoint,date,shareUrl) VALUES (?,?,?,?,?,?,?,?,?)").use { statement ->
                statement.setString(1, model.netAdapter)
                statement.setDouble(2, model.ping)
                statement.setDouble(3, model.jitter)
                statement.setDouble(4, model.download)
                statement.setDouble(5, model.upload)
                statement.setString(6, model.ispInfo)
                statement.setString(7, model.testPoint)
                statement.setLong(8, model.date)
                statement.setString(9, model.shareUrl)
                statement.executeUpdate()
            }
        } catch (e: Exception) {
            System.err.println("Failed to save history entry: $e")
        }
    }
    fun readHistory() : MutableList<ModelHistory> {
        val result = mutableListOf<ModelHistory>()
        try {
            connection.prepareStatement("SELECT * FROM history ORDER BY id DESC").use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        result.add(
                            ModelHistory(
                            id = resultSet.getInt("id"),
                            netAdapter = resultSet.getString("netAdapter"),
                            ping = resultSet.getDouble("ping"),
                            jitter = resultSet.getDouble("jitter"),
                            download = resultSet.getDouble("download"),
                            upload = resultSet.getDouble("upload"),
                            ispInfo = resultSet.getString("ispInfo"),
                            testPoint = resultSet.getString("testPoint"),
                            date = resultSet.getLong("date"),
                            shareUrl = resultSet.getString("shareUrl"))
                        )
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println("Failed to read history: $e")
        }
        return result
    }
    fun clearHistory() {
        val statement = connection.createStatement()
        try {
            statement.executeUpdate("DELETE FROM history")
        } catch (_ : Exception) {} finally {
            statement.close()
        }
    }

}