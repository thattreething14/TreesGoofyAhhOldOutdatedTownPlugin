package tree.towns

import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import tree.towns.commands.TownCommand
import tree.towns.database.DatabaseManager
import tree.towns.database.SQLiteBackupTask
import tree.towns.database.SQLiteTables
import tree.towns.listeners.TownEnterLeaveListener
import tree.towns.utils.UpkeepManager

class TownsPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: Plugin
            private set
    }
    override fun onEnable() {

        instance = this

        if (!setupEconomy()) {
            logger.severe("- Disabled due to no Vault dependency found!")
            server.pluginManager.disablePlugin(this)
            return
        }

        createFolder()
        SQLiteTables.initialize()
        UpkeepManager.scheduleUpkeep()

        getCommand("town")?.setExecutor(TownCommand())
        getCommand("town")?.tabCompleter = TownCommand()

        server.pluginManager.registerEvents(TownEnterLeaveListener(), this)

        SQLiteBackupTask.scheduleHourlyBackup()
    }
    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy>? = server.servicesManager.getRegistration(Economy::class.java)
        Towns.econ = rsp?.provider
        return Towns.econ != null
    }
    private fun createFolder() {
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
    }
    override fun onDisable() {
            SQLiteBackupTask.backupDatabase()
            DatabaseManager.close()
        }
    }
