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
            throw UnsupportedOperationException("Unsupported operating system")
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
        val statement = connection.createStatement()
        try {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "netAdapter TEXT," +
                        "ping REAL," +
                        "jitter REAL," +
                        "download REAL," +
                        "upload REAL," +
                        "ispInfo TEXT," +
                        "testPoint TEXT," +
                        "date INTEGER" +
                        ")"
            )
        } catch (_: Exception) {
        } finally {
            statement.close()
        }
    }
    fun saveHistory(model: ModelHistory) {
        val statement = connection.prepareStatement("INSERT INTO history (netAdapter,ping,jitter,download,upload,ispInfo,testPoint,date) VALUES (?,?,?,?,?,?,?,?)")
        try {
            statement.setString(1, model.netAdapter)
            statement.setDouble(2, model.ping)
            statement.setDouble(3, model.jitter)
            statement.setDouble(4, model.download)
            statement.setDouble(5, model.upload)
            statement.setString(6, model.ispInfo)
            statement.setString(7, model.testPoint)
            statement.setLong(8, model.date)
            statement.executeUpdate()
        } catch (_ : Exception) {} finally {
            statement.close()
        }
    }
    fun readHistory() : MutableList<ModelHistory> {
        val statement = connection.prepareStatement("SELECT * FROM history ORDER BY id DESC")
        val resultSet = statement.executeQuery()
        val result = mutableListOf<ModelHistory>()
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
                date = resultSet.getLong("date"))
            )
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