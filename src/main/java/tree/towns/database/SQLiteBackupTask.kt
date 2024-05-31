package tree.towns.database

import tree.towns.Config
import tree.towns.Towns
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.SimpleDateFormat
import java.util.*

object SQLiteBackupTask {
    private val plugin = Towns.plugin
    private val timer = Timer()
    fun scheduleHourlyBackup() {
        if (Config.enableHourlyBackup) {
            val oneHourInMillis: Long = 60 * 60 * 1000
            val currentTime = System.currentTimeMillis()
            val hourlyBackupDir = File(plugin.dataFolder, "hourly_backups")
            hourlyBackupDir.mkdirs()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val backupFileName = "towns_hourly_backup_${dateFormat.format(Date())}.db"

            val hourlyBackupFile = File(hourlyBackupDir, backupFileName)

            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        val dbFile = File(plugin.dataFolder, "towns.db")
                        Files.copy(dbFile.toPath(), hourlyBackupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    } catch (e: Exception) {
                        plugin.logger.warning("Error while creating hourly backup: ${e.message}")
                    }
                }
            }, currentTime + oneHourInMillis, oneHourInMillis)
        }
    }

    fun backupDatabase() {
        try {
            if (Config.enableRestartBackup) {
                val dbFile = File(plugin.dataFolder, "towns.db")
                val backupDir = File(plugin.dataFolder, "backups")
                backupDir.mkdirs()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val backupFileName = "towns_backup_${dateFormat.format(Date())}.db"

                val backupFile = File(backupDir, backupFileName)

                Files.copy(dbFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        } catch (e: Exception) {
            plugin.logger.warning("Error while creating a backup of the database: ${e.message}")
        }
    }
}
