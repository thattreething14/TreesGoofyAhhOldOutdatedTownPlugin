package tree.towns.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import tree.towns.Towns
import java.io.File
import java.sql.Connection
import java.sql.SQLException

object DatabaseManager {
    private val plugin = Towns.plugin
    private val hikariConfig = HikariConfig().apply {
        driverClassName = "org.sqlite.JDBC"
        val dbFile = File(plugin.dataFolder, "towns.db")
        jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
        maximumPoolSize = 10
    }
    private val dataSource = HikariDataSource(hikariConfig)

    fun getConnection(): Connection {
        try {
            return dataSource.connection
        } catch (e: SQLException) {
            throw RuntimeException("Error getting a database connection", e)
        }
    }
    fun close() {
        dataSource.close()
    }
}
