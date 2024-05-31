package tree.towns

import org.bukkit.Chunk
import org.bukkit.entity.Player
import tree.towns.Towns.TownInfo
import tree.towns.constants.TownRank

/**
 * API class providing methods to interact with town-related functionality.
 * Yes, I used GPT for commentation
 */
@Suppress("unused")
class TownsAPI {

    /**
     * Checks if a player is currently a resident in any town.
     *
     * @param player The name of the player to check.
     * @return True if the player is in a town, false otherwise.
     */
    fun isPlayerInTown(player: String): Boolean {
        return Towns.isPlayerInTown(player)
    }

    /**
     * Sets a new mayor for the specified town.
     *
     * @param town The name of the town.
     * @param newMayor The name of the new mayor.
     */
    fun setTownMayor(town: String, newMayor: String) {
        Towns.setTownMayor(town, newMayor)
    }

    /**
     * Checks if a player has been invited to a specific town.
     *
     * @param player The name of the player to check.
     * @param town The name of the town.
     * @return True if the player is invited, false otherwise.
     */
    fun isPlayerInvited(player: String, town: String): Boolean {
        return Towns.isPlayerInvited(player, town)
    }

    /**
     * Gets the town in which a player is a resident.
     *
     * @param player The name of the player.
     * @return The name of the town or null if not a resident.
     */
    fun getResident(player: Player): String? {
        return Towns.getTownByPlayer(player.uniqueId.toString())
    }

    /**
     * Gets a list of all residents across all towns.
     *
     * @return A list of resident player names.
     */
    fun getResidents(): List<String> {
        val towns = Towns.getAllTowns()
        val residents = mutableListOf<String>()
        for (town in towns) {
            val members = Towns.getTownMembers(town)
            residents.addAll(members) }
        return residents
    }

    /**
     * Gets information about a specific town.
     *
     * @param town The name of the town.
     * @return TownInfo object containing information about the town, or null if not found.
     */
    fun getTownInfo(town: String): TownInfo? {
        return Towns.getTownInfo(town)
    }

    /**
     * Gets a list of all town names.
     *
     * @return A list of town names.
     */
    fun getTowns(): List<String> {
        return Towns.getAllTowns()
    }

    /**
     * Deletes the specified town.
     *
     * @param town The name of the town to be deleted.
     */
    fun deleteTown(town: String) {
        Towns.deleteTown(town)
    }
    fun createTown(town: String, mayor: Player, members: List<Player>, homeBlock: Chunk) {
        return Towns.createTown(town, mayor, members, homeBlock)
    }
    /**
     * Gets the town where a specified player is the mayor.
     *
     * @param mayor The name of the mayor.
     * @return The name of the town or null if not found.
     */
    fun getTownByMayor(mayor: String): String? {
        return Towns.getTownByMayor(mayor)
    }

    /**
     * Stores an invitation for a player to join a town.
     *
     * @param sender The name of the player sending the invitation.
     * @param invitedPlayer The name of the player being invited.
     * @param town The name of the town.
     * @return True if the invitation is stored successfully, false otherwise.
     */
    fun storeInvitation(sender: String, invitedPlayer: String, town: String): Boolean {
        return Towns.storeInvitation(sender, invitedPlayer, town)
    }

    /**
     * Accepts an invitation for a player to join a town.
     *
     * @param player The name of the player accepting the invitation.
     * @param town The name of the town.
     * @return True if the invitation is accepted, false otherwise.
     */
    fun acceptInvitation(player: String, town: String): Boolean {
        return Towns.acceptInvitation(player, town)
    }

    /**
     * Declines an invitation for a player to join a town.
     *
     * @param player The name of the player declining the invitation.
     * @param town The name of the town.
     * @return True if the invitation is declined, false otherwise.
     */
    fun declineInvitation(player: String, town: String): Boolean {
        return Towns.declineInvitation(player, town)
    }

    /**
     * Gets the rank of a player in a specific town.
     *
     * @param player The name of the player.
     * @param town The name of the town.
     * @return TownRank enum representing the player's rank or null if not found.
     */
    fun getPlayerRank(player: String, town: String): TownRank? {
        return Towns.getPlayerRank(player, town)
    }

    /**
     * Adds a player to a specific rank in a town.
     *
     * @param player The name of the player.
     * @param town The name of the town.
     * @param rank The rank to assign to the player.
     */
    fun addPlayerToRank(player: String, town: String, rank: TownRank) {
        Towns.addPlayerToRank(player, town, rank)
    }

    /**
     * Removes a player from a specific rank in a town.
     *
     * @param player The name of the player.
     * @param town The name of the town.
     * @param rank The rank to remove from the player.
     */
    fun removePlayerFromRank(player: String, town: String, rank: TownRank) {
        Towns.removePlayerFromRank(player, town, rank)
    }

    /**
     * Gets a list of members in a specific town.
     *
     * @param town The name of the town.
     * @return A list of player names in the town.
     */
    fun getTownMembers(town: String): List<String> {
        return Towns.getTownMembers(town)
    }

    /**
     * Removes a player from a specific town.
     *
     * @param player The name of the player to be removed.
     * @param town The name of the town from which the player is to be removed.
     */
    fun removePlayerFromTown(player: String, town: String) {
        Towns.removePlayerFromTown(player, town)
    }

    /**
     * Gets the balance of a specific town.
     *
     * @param town The name of the town.
     * @return The current balance of the town.
     */
    fun getTownBalance(town: String): Double {
        return Towns.getTownBalance(town)
    }

    /**
     * Updates the balance of a specific town.
     *
     * @param town The name of the town.
     * @param newBalance The new balance to set for the town.
     */
    fun updateTownBalance(town: String, newBalance: Double) {
        Towns.updateTownBalance(town, newBalance)
    }
    companion object {
        private val instance = TownsAPI()
        fun getInstance(): TownsAPI {
            return instance
        }
    }
}
