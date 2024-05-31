package tree.towns

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import tree.towns.constants.TownRank
import tree.towns.database.DatabaseManager
import tree.towns.utils.Messages
import java.sql.SQLException
import java.util.*

public object Towns {
    internal val plugin: Plugin = TownsPlugin.instance
    var econ: Economy? = null
    fun getEconomy(): Economy? {
        return econ
    }
    fun createTown(town: String, mayor: Player, members: List<Player>, homeBlock: Chunk) {
        val insertQuery = "INSERT INTO towns (name, mayor, members, max_chunks) VALUES (?, ?, ?, ?)"
        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(insertQuery).use { statement ->
                statement.setString(1, town)
                statement.setString(2, mayor.uniqueId.toString())
                statement.setString(3, members.joinToString(",") { it.uniqueId.toString() })
                statement.executeUpdate()
            }
        }

        if (claimChunk(town, homeBlock)) {
            markChunkAsUnclaimable(town, homeBlock)
        }

        val baseChunks = Config.baseChunks
        val chunksPerMember = Config.chunksPerMember
        val townMembers = getTownMembers(town).size
        val maxChunks = baseChunks + chunksPerMember * townMembers
        setMaxChunksForTown(town, maxChunks)

        Config.assignRankPermissions(mayor.name, TownRank.MAYOR)
        addPlayerToRank(mayor.uniqueId.toString(), town, TownRank.MAYOR)
        members.forEach { member ->
            val memberId = member.uniqueId.toString()
            addPlayerToRank(memberId, town, TownRank.MEMBER)
        }

        setTownSpawn(town, mayor.location)
    }

    /**
     * literally used chatgpt for next two functions lol got lazy
     * */
    fun setTownSpawn(town: String, location: Location) {
        val updateQuery = "UPDATE towns SET spawn_x = ?, spawn_y = ?, spawn_z = ?, spawn_yaw = ?, spawn_pitch = ? WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(updateQuery).use { statement ->
                statement.setDouble(1, location.x)
                statement.setDouble(2, location.y)
                statement.setDouble(3, location.z)
                statement.setFloat(4, location.yaw)
                statement.setFloat(5, location.pitch)
                statement.setString(6, town)

                statement.executeUpdate()
            }
        }
    }
    fun teleportToTownSpawn(player: Player, town: String) {
        val selectQuery = "SELECT spawn_x, spawn_y, spawn_z, spawn_yaw, spawn_pitch FROM towns WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, town)

                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        val spawnLocation = Location(
                            player.world,
                            resultSet.getDouble("spawn_x"),
                            resultSet.getDouble("spawn_y"),
                            resultSet.getDouble("spawn_z"),
                            resultSet.getFloat("spawn_yaw"),
                            resultSet.getFloat("spawn_pitch")
                        )

                        player.teleport(spawnLocation)
                        Messages.print(player, "Teleported to the spawn of town $town.")
                    } else {
                        Messages.error(player, "Town $town not found or spawn not set.")
                    }
                }
            }
        }
    }
    fun getTownMayor(town: String): String? {
        val selectQuery = "SELECT mayor FROM towns WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, town)

                statement.executeQuery().use { resultSet ->
                    return if (resultSet.next()) {
                        resultSet.getString("mayor")
                    } else {
                        null
                    }
                }
            }
        }
    }

    fun isTownExists(town: String): Boolean {
        val query = "SELECT COUNT(*) FROM towns WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    val count = resultSet.getInt(1)
                    return count > 0
                }
            }
        }

        return false
    }

    fun isPlayerInTown(player: String): Boolean {
        val query = "SELECT COUNT(*) FROM towns WHERE members LIKE ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, "%$player%")

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    val count = resultSet.getInt(1)
                    return count > 0
                }
            }
        }

        return false
    }

    fun getTownByPlayer(player: String): String? {
        val query = "SELECT name FROM towns WHERE members LIKE ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, "%$player%")

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    return resultSet.getString("name")
                }
            }
        }
        return null
    }
    fun setTownMayor(town: String, newMayor: String) {
        val updateQuery = "UPDATE towns SET mayor = ? WHERE name = ?"
        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(updateQuery).use { preparedStatement ->
                try {
                    preparedStatement.setString(1, newMayor)
                    preparedStatement.setString(2, town)
                    preparedStatement.executeUpdate()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun isPlayerInvited(player: String, town: String): Boolean {
        val query = "SELECT COUNT(*) FROM pending_invitations WHERE invited_player_name = ? AND town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, player)
                statement.setString(2, town)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    val count = resultSet.getInt(1)
                    return count > 0
                }
            }
        }

        return false
    }
    fun getAllTowns(): List<String> {
        val towns = mutableListOf<String>()
        val query = "SELECT name FROM towns"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    val town = resultSet.getString("name")
                    towns.add(town)
                }
            }
        }

        return towns
    }
    fun deleteTown(town: String) {
        val deleteTownQuery = "DELETE FROM towns WHERE name = ?"
        val deleteRanksQuery = "DELETE FROM town_ranks WHERE town_name = ?"
        val deleteBalanceQuery = "DELETE FROM balances WHERE town_name = ?"
        val deleteClaimedChunkQuery = "DELETE FROM town_chunks WHERE town_name = ?"
        val deleteTownSetsQuery = "DELETE FROM town_sets WHERE town_name = ?"
        DatabaseManager.getConnection().use { connection ->
            // rank deletion
            connection.prepareStatement(deleteRanksQuery).use { deleteRanksStatement ->
                deleteRanksStatement.setString(1, town)
                deleteRanksStatement.executeUpdate()
            }
            // balance deletion
            connection.prepareStatement(deleteBalanceQuery).use { deleteBalanceStatement ->
                deleteBalanceStatement.setString(1, town)
                deleteBalanceStatement.executeUpdate()
            }
            // claimed chunk deletion
            connection.prepareStatement(deleteClaimedChunkQuery).use { deleteChunkStatement ->
                deleteChunkStatement.setString(1, town)
                deleteChunkStatement.executeUpdate()
            }
            // town sets deletion
            connection.prepareStatement(deleteTownSetsQuery).use { deleteTownSetsStatement ->
                deleteTownSetsStatement.setString(1, town)
                deleteTownSetsStatement.executeUpdate()
            }
            // town object deletion
            connection.prepareStatement(deleteTownQuery).use { deleteTownStatement ->
                deleteTownStatement.setString(1, town)
                deleteTownStatement.executeUpdate()
            }
        }
        // permission deletion
        val membersUUIDs =getTownMembers(town)
        membersUUIDs.forEach { memberUUID ->
            val member = getPlayerName(UUID.fromString(memberUUID))
            member?.let {
                Config.assignRankPermissions(member, TownRank.MEMBER)
            }
        }
    }

    fun getTownByMayor(mayorName: String): String? {
        val query = "SELECT name FROM towns WHERE mayor = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, mayorName)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    return resultSet.getString("name")
                }
            }
        }

        return null
    }

    private fun addPlayerToTown(player: String, town: String) {
        val updateQuery = "UPDATE towns SET members = members || ? WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(updateQuery).use { statement ->
                statement.setString(1, ",$player")
                statement.setString(2, town)

                statement.executeUpdate()
            }
        }
    }
    fun setTownName(player: String, newName: String) {
        val oldtown = getTownByPlayer(player) ?: return

        val query = "UPDATE towns SET name = ? WHERE mayor = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, player)

                statement.executeUpdate()
            }
        }
        updatetownInRanks(oldtown, newName)
        updatetownInBalances(oldtown, newName)
        updatetownInPendingInvitations(oldtown, newName)
        updatetownInTownChunks(oldtown, newName)
        updatetownInSets(oldtown, newName)
    }
    private fun updatetownInSets(oldName: String, newName: String) {
        val query = "UPDATE town_sets SET town_name = ? WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, oldName)

                statement.executeUpdate()
            }
        }
    }
    private fun updatetownInRanks(oldName: String, newName: String) {
        val query = "UPDATE town_ranks SET town_name = ? WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, oldName)

                statement.executeUpdate()
            }
        }
    }
    private fun updatetownInBalances(oldName: String, newName: String) {
        val query = "UPDATE balances SET town_name = ? WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, oldName)

                statement.executeUpdate()
            }
        }
    }
    private fun updatetownInPendingInvitations(oldName: String, newName: String) {
        val query = "UPDATE pending_invitations SET town_name = ? WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, oldName)

                statement.executeUpdate()
            }
        }
    }
    private fun updatetownInTownChunks(oldName: String, newName: String) {
        val query = "UPDATE town_chunks SET town_name = ? WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, newName)
                statement.setString(2, oldName)

                statement.executeUpdate()
            }
        }
    }

    fun storeInvitation(senderName: String, invitedplayer: String, town: String): Boolean {
        if (getInvitation(invitedplayer, town) != null) {
            return false
        }

        return try {
            val currentTimestamp = System.currentTimeMillis()

            DatabaseManager.getConnection().use { connection ->
                val statement =
                    connection.prepareStatement("INSERT INTO pending_invitations (invited_player_name, sender_name, town_name, timestamp) VALUES (?, ?, ?, ?)")
                statement.setString(1, invitedplayer)
                statement.setString(2, senderName)
                statement.setString(3, town)
                statement.setLong(4, currentTimestamp)
                statement.executeUpdate()
            }

            true
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    fun acceptInvitation(player: String, town: String): Boolean {
        val invitation = getInvitation(player, town)

        if (invitation != null) {
            deleteInvitation(player, town)
            addPlayerToTown(player, town)
            return true
        }

        return false
    }

    fun declineInvitation(player: String, town: String): Boolean {
        val invitation = getInvitation(player, town)

        if (invitation != null) {
            deleteInvitation(player, town)
            return true
        }

        return false
    }

    private fun getInvitation(player: String, town: String): Long? {
        val query = "SELECT timestamp FROM pending_invitations WHERE invited_player_name = ? AND town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, player)
                statement.setString(2, town)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    return resultSet.getLong("timestamp")
                }
            }
        }

        return null
    }

    private fun deleteInvitation(player: String, town: String) {
        val query = "DELETE FROM pending_invitations WHERE invited_player_name = ? AND town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, player)
                statement.setString(2, town)

                statement.executeUpdate()
            }
        }
    }

    fun hasPlayerRank(player: String, town: String, rank: TownRank): Boolean {
        val query = "SELECT COUNT(*) FROM town_ranks WHERE town_name = ? AND player_name = ? AND rank = ?"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                statement.setString(2, player)
                statement.setString(3, rank.name)

                val resultSet = statement.executeQuery()

                resultSet.next() && resultSet.getInt(1) > 0
            }
        }
    }

    fun getPlayerRank(player: String, town: String): TownRank? {
        val query = "SELECT rank FROM town_ranks WHERE town_name = ? AND player_name = ? ORDER BY priority DESC LIMIT 1"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                statement.setString(2, player)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    val rankName = resultSet.getString("rank")
                    return try {
                        TownRank.valueOf(rankName)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            }

        }

        return null
    }

    fun addPlayerToRank(player: String, town: String, rank: TownRank) {
        val insertQuery = "INSERT INTO town_ranks (town_name, player_name, rank, priority) VALUES (?, ?, ?, ?)"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(insertQuery).use { statement ->
                statement.setString(1, town)
                statement.setString(2, player)
                statement.setString(3, rank.name)
                statement.setInt(4, rank.priority)

                try {
                    statement.executeUpdate()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun removePlayerFromRank(player: String, town: String, rank: TownRank) {
        val deleteQuery = "DELETE FROM town_ranks WHERE town_name = ? AND player_name = ? AND rank = ?"

        DatabaseManager.getConnection().use { connection ->
            try {
                connection.prepareStatement(deleteQuery).use { statement ->
                    statement.setString(1, town)
                    statement.setString(2, player)
                    statement.setString(3, rank.name)
                    statement.executeUpdate()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    fun getTownMembers(town: String): List<String> {
        val query = "SELECT members FROM towns WHERE name = ?"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    val membersString = resultSet.getString("members")
                    return membersString.split(",").filter { it.isNotBlank() }.map { it.trim() }
                }
            }
            emptyList()
        }
    }
    fun removePlayerFromTown(player: String, town: String) {
        DatabaseManager.getConnection().use { connection ->
            try {
                // Update members by replacing the player name with an empty string
                val updateMembersQuery = "UPDATE towns SET members = REPLACE(members, ?, '') WHERE name = ?"
                connection.prepareStatement(updateMembersQuery).use { updateMembersStatement ->
                    updateMembersStatement.setString(1, player)
                    updateMembersStatement.setString(2, town)
                    updateMembersStatement.executeUpdate()
                }
                Config.assignRankPermissions(player, TownRank.MEMBER)
                val playerRanks = getAllPlayerRanks(player, town)
                playerRanks.forEach { removePlayerFromRank(player, town, it) }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }
    private fun getAllPlayerRanks(player: String, town: String): List<TownRank> {
        val query = "SELECT rank FROM town_ranks WHERE town_name = ? AND player_name = ?"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                statement.setString(2, player)

                val resultSet = statement.executeQuery()
                val ranks = mutableListOf<TownRank>()

                while (resultSet.next()) {
                    val rankName = resultSet.getString("rank")
                    val rank = TownRank.valueOf(rankName)
                    ranks.add(rank)
                }

                ranks
            }
        }
    }
    fun getTownBalance(town: String): Double {
        val query = "SELECT balance FROM balances WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    return resultSet.getDouble("balance")
                }
            }
        }

        return 0.0
    }

    fun updateTownBalance(town: String, newBalance: Double) {
        val query = "INSERT OR REPLACE INTO balances (town_name, balance) VALUES (?, ?)"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                statement.setDouble(2, newBalance)

                statement.executeUpdate()
            }
        }
    }
    fun claimChunk(town: String, chunk: Chunk): Boolean {
        try {
            val claimQuery = "INSERT INTO town_chunks (town_name, world_name, x, z) VALUES (?, ?, ?, ?)"

            DatabaseManager.getConnection().use { connection ->
                connection.prepareStatement(claimQuery).use { statement ->
                    statement.setString(1, town)
                    statement.setString(2, chunk.world.name)
                    statement.setInt(3, chunk.x)
                    statement.setInt(4, chunk.z)

                    statement.executeUpdate()
                }
            }

            return true
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    fun unclaimChunk(town: String, chunk: Chunk): Boolean {
        try {
            val unclaimQuery = "DELETE FROM town_chunks WHERE town_name = ? AND world_name = ? AND x = ? AND z = ?"

            DatabaseManager.getConnection().use { connection ->
                connection.prepareStatement(unclaimQuery).use { statement ->
                    statement.setString(1, town)
                    statement.setString(2, chunk.world.name)
                    statement.setInt(3, chunk.x)
                    statement.setInt(4, chunk.z)

                    statement.executeUpdate()
                }
            }

            return true
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }
    fun deleteOldUnclaimableStatus(town: String) {
        val updateQuery = "UPDATE town_chunks SET unclaimable = 0 WHERE town_name = ? AND unclaimable = 1"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(updateQuery).use { statement ->
                statement.setString(1, town)
                statement.executeUpdate()
            }
        }
    }
    fun isChunkClaimedByTown(town: String, chunk: Chunk): Boolean {
        val query = "SELECT COUNT(*) FROM town_chunks WHERE town_name = ? AND world_name = ? AND x = ? AND z = ?"

        return try {
            DatabaseManager.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, town)
                    statement.setString(2, chunk.world.name)
                    statement.setInt(3, chunk.x)
                    statement.setInt(4, chunk.z)

                    val resultSet = statement.executeQuery()

                    resultSet.next() && resultSet.getInt(1) > 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }
    fun isChunkClaimed(chunk: Chunk): Boolean {
        val query = "SELECT COUNT(*) FROM town_chunks WHERE world_name = ? AND x = ? AND z = ?"

        return try {
            DatabaseManager.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, chunk.world.name)
                    statement.setInt(2, chunk.x)
                    statement.setInt(3, chunk.z)

                    val resultSet = statement.executeQuery()

                    resultSet.next() && resultSet.getInt(1) > 0
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            false
        }
    }

    fun markChunkAsUnclaimable(town: String, chunk: Chunk) {
        val updateQuery = "UPDATE town_chunks SET unclaimable = 1 WHERE town_name = ? AND world_name = ? AND x = ? AND z = ? AND unclaimable = 0"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(updateQuery).use { statement ->
                statement.setString(1, town)
                statement.setString(2, chunk.world.name)
                statement.setInt(3, chunk.x)
                statement.setInt(4, chunk.z)

                statement.executeUpdate()
            }
        }
    }
    fun isChunkUnclaimable(town: String, chunk: Chunk): Boolean {
        val query = "SELECT unclaimable FROM town_chunks WHERE town_name = ? AND world_name = ? AND x = ? AND z = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                statement.setString(2, chunk.world.name)
                statement.setInt(3, chunk.x)
                statement.setInt(4, chunk.z)

                val resultSet = statement.executeQuery()

                return if (resultSet.next()) {
                    resultSet.getInt("unclaimable") == 1
                } else {
                    true
                }
            }
        }
    }
    fun getAdjacentClaimedChunks(town: String, chunk: Chunk): List<Chunk> {
        val claimedChunks = mutableListOf<Chunk>()
        val world = chunk.world
        val x = chunk.x
        val z = chunk.z
        val directions = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))
        for ((dx, dz) in directions) {
            val adjacentChunk = world.getChunkAt(x + dx, z + dz)
            if (isChunkClaimedByTown(town, adjacentChunk)) {
                claimedChunks.add(adjacentChunk)
            }
        }
        return claimedChunks
    }
    fun getTownClaimedChunks(town: String): List<Chunk> {
        val chunks = mutableListOf<Chunk>()

        val selectQuery = "SELECT world_name, x, z FROM town_chunks WHERE town_name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(selectQuery).use { statement ->
                statement.setString(1, town)

                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    val worldName = resultSet.getString("world_name")
                    val x = resultSet.getInt("x")
                    val z = resultSet.getInt("z")

                    val world = Bukkit.getWorld(worldName)
                    if (world != null) {
                        val chunk = world.getChunkAt(x, z)
                        chunks.add(chunk)
                    }
                }
            }
        }

        return chunks
    }
    fun gettownByLocation(chunk: Chunk): String? {
        val query = "SELECT town_name FROM town_chunks WHERE world_name = ? AND x = ? AND z = ?"

        return try {
            DatabaseManager.getConnection().use { connection ->
                connection.prepareStatement(query).use { statement ->
                    statement.setString(1, chunk.world.name)
                    statement.setInt(2, chunk.x)
                    statement.setInt(3, chunk.z)

                    val resultSet = statement.executeQuery()

                    if (resultSet.next()) {
                        resultSet.getString("town_name")
                    } else {
                        null
                    }
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
            null
        }
    }


    fun setMaxChunksForTown(town: String, maxChunks: Int) {
        val updateQuery = "UPDATE towns SET max_chunks = ? WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(updateQuery).use { statement ->
                statement.setInt(1, maxChunks)
                statement.setString(2, town)

                statement.executeUpdate()
            }
        }
    }
    fun getMaxChunksForTown(town: String): Int {
        val query = "SELECT max_chunks FROM towns WHERE name = ?"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)

                statement.executeQuery().use { resultSet ->
                    return if (resultSet.next()) {
                        resultSet.getInt("max_chunks")
                    } else {
                        0
                    }
                }
            }
        }
    }

    fun getPlayersWithRanks(town: String, vararg ranks: TownRank): List<Pair<TownRank, List<String>>> {
        val query = "SELECT rank, player_name FROM town_ranks WHERE town_name = ? AND rank IN (${ranks.joinToString { "?" }})"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                ranks.forEachIndexed { index, rank ->
                    statement.setString(index + 2, rank.name)
                }

                val resultSet = statement.executeQuery()

                val playersByRank = mutableMapOf<TownRank, MutableList<String>>()

                while (resultSet.next()) {
                    val rankName = resultSet.getString("rank")
                    val player = resultSet.getString("player_name")

                    val rank = try {
                        TownRank.valueOf(rankName)
                    } catch (e: IllegalArgumentException) {
                        continue
                    }

                    playersByRank.computeIfAbsent(rank) { mutableListOf() }.add(player)
                }

                playersByRank.toList()
            }
        }
    }

    fun getTownRanks(town: String): List<TownRank> {
        val query = "SELECT DISTINCT rank FROM town_ranks WHERE town_name = ? ORDER BY priority DESC"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)

                val resultSet = statement.executeQuery()
                val ranks = mutableListOf<TownRank>()

                while (resultSet.next()) {
                    val rankName = resultSet.getString("rank")
                    try {
                        val rank = TownRank.valueOf(rankName)
                        ranks.add(rank)
                    } catch (_: IllegalArgumentException) {
                    }
                }

                ranks
            }
        }
    }

    fun getTownInfo(town: String): TownInfo? {
        val mayorUUIDString = getTownMayor(town) ?: return null
        val mayor = getPlayerName(UUID.fromString(mayorUUIDString)) ?: return null

        val memberUUIDStrings = getTownMembers(town)
        val members = memberUUIDStrings.mapNotNull { getPlayerName(UUID.fromString(it)) }

        val balance = getTownBalance(town)
        val claimedChunks = getTownClaimedChunks(town)
        val ranks = getTownRanks(town)

        return TownInfo(mayor, members, balance, claimedChunks, ranks)
    }

    fun getPlayerName(uuid: UUID): String? {
        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
        return if (offlinePlayer.hasPlayedBefore()) {
            offlinePlayer.name
        } else {
            null
        }
    }

    data class TownInfo(
        val mayor: String,
        val members: List<String>,
        val balance: Double,
        val claimedChunks: List<Chunk>,
        val ranks: List<TownRank>
    )
    fun getTownAtLocation(worldName: String, location: Location): String? {
        val chunk = location.chunk
        val x = chunk.x
        val z = chunk.z

        val query = "SELECT town_name FROM town_chunks WHERE world_name = ? AND x = ? AND z = ?"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, worldName)
                statement.setInt(2, x)
                statement.setInt(3, z)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    return resultSet.getString("town_name")
                }
            }

            null
        }
    }
    fun setTownDescription(town: String, description: String) {
        val query = "INSERT OR REPLACE INTO town_sets(town_name, description) VALUES (?, ?)"

        DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)
                statement.setString(2, description)

                statement.executeUpdate()
            }
        }
    }
    fun getTownDescription(town: String): String? {
        val query = "SELECT description FROM town_sets WHERE town_name = ?"

        return DatabaseManager.getConnection().use { connection ->
            connection.prepareStatement(query).use { statement ->
                statement.setString(1, town)

                val resultSet = statement.executeQuery()

                if (resultSet.next()) {
                    resultSet.getString("description")
                } else {
                    null
                }
            }
        }
    }
}