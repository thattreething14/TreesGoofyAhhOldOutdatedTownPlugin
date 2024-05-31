package tree.towns

import org.bukkit.entity.Player
import org.bukkit.configuration.file.YamlConfiguration
import tree.towns.constants.TownRank
import tree.towns.Towns.plugin
import java.io.File

object Config {
    private var config: File = File(plugin.dataFolder, "config.yml")
    public var enableRestartBackup: Boolean = true
    public var enableHourlyBackup: Boolean = true
    // TODO: implement cooldown for towns
    public var newTownCooldown: Int = 0
    public var townCreateCost: Double = 100.0
    public var townChunkCost: Double = 16.0
    public var baseChunks: Int = 8
    public var chunksPerMember: Int = 8
    public var enableUpkeep: Boolean = true
    public var upkeepTime: String = "12:00"
    public var upkeepTimeZone: String = "America/New_York"
    public var upkeepAmount: Double = 100.0
    private var rankConfigFile: File = File(plugin.dataFolder, "rank-config.yml")
    private var rankConfig: YamlConfiguration? = null
    init {
        if (!rankConfigFile.exists()) {
            plugin.saveResource("rank-config.yml", false)
        }
        if (!config.exists()) {
            saveConfig()
        }
        rankConfig = YamlConfiguration.loadConfiguration(rankConfigFile)
        loadConfig()
    }
    private fun loadConfig() {
        if (!config.exists()) {
            saveConfig()
        } else {
            val configData = YamlConfiguration.loadConfiguration(config)
            enableRestartBackup = configData.getBoolean("town.enableRestartBackup", enableRestartBackup)
            enableHourlyBackup = configData.getBoolean("town.enableHourlyBackup", enableHourlyBackup)
            newTownCooldown = configData.getInt("town.createcooldown", 0)
            townCreateCost = configData.getDouble("town.createcost", 0.0)
            townChunkCost = configData.getDouble("town.chunkcost", 0.0)
            baseChunks = configData.getInt("town.basechunks", 0)
            chunksPerMember = configData.getInt("town.chunkspermember", 0)
            enableUpkeep = configData.getBoolean("upkeep.enabled", true)
            upkeepTime = configData.getString("upkeep.time", "12:00") ?: "12:00"
            upkeepTimeZone = configData.getString("upkeep.timeZone", "America/New_York") ?: "America/New_York"
            upkeepAmount = configData.getDouble("upkeep.townamount", 100.0)
        }
    }
    private fun saveConfig() {
        val configData = YamlConfiguration()

        // TOWN SETTINGS
        configData.set("#################", "")
        configData.set("# TOWN SETTINGS #", "")
        configData.set("#################", "")
        configData.set("town.enableRestartBackup", enableRestartBackup)
        configData.set("town.enableHourlyBackup", enableHourlyBackup)
        configData.set("town.createcooldown", newTownCooldown)
        configData.set("town.createcost", townCreateCost)
        configData.set("town.chunkcost", townChunkCost)
        configData.set("town.basechunks", baseChunks)
        configData.set("town.chunkspermember", chunksPerMember)

        // TOWN UPKEEP SETTINGS
        configData.set("########################", "")
        configData.set("# TOWN UPKEEP SETTINGS #", "")
        configData.set("########################", "")
        configData.set("upkeep.enabled", enableUpkeep)
        configData.set("upkeep.time", upkeepTime)
        configData.set("upkeep.timeZone", upkeepTimeZone)
        configData.set("upkeep.townamount", upkeepAmount)

        configData.save(config)
    }
    fun assignRankPermissions(playerName: String, newRank: TownRank) {
        val player = plugin.server.getPlayerExact(playerName) ?: return
        clearPlayerPermissions(player)

        if (rankConfig?.isConfigurationSection("ranks") == true) {
            val ranksSection = rankConfig?.getConfigurationSection("ranks")
            val defaultRankPermissions = ranksSection?.getStringList("MEMBER.permissions") ?: emptyList()

            val rankPermissions = ranksSection?.getStringList("${newRank.name}.permissions") ?: defaultRankPermissions

            for (permission in rankPermissions) {
                player.addAttachment(plugin, permission, true)
            }
        }
    }
    fun assignRankPermissions(playerName: String, permissions: List<String>) {
        val player = plugin.server.getPlayerExact(playerName) ?: return
        clearPlayerPermissions(player)

        for (permission in permissions) {
            player.addAttachment(plugin, permission, true)
        }
    }

    private fun clearPlayerPermissions(player: Player) {
        val permissions = player.addAttachment(plugin)
        for (perm in permissions.permissions.keys) {
            permissions.unsetPermission(perm)
        }
        player.removeAttachment(permissions)
        player.addAttachment(plugin, 0)
    }
    fun getRankPermissions(rank: TownRank): List<String> {
        if (rankConfig?.isConfigurationSection("ranks") == true) {
            val ranksSection = rankConfig?.getConfigurationSection("ranks")
            return ranksSection?.getStringList("${rank.name}.permissions") ?: emptyList()
        }
        return emptyList()
    }
}