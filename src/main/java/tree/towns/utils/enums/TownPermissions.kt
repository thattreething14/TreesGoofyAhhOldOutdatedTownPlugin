package tree.towns.utils.enums
 /** - These permissions can be configured or assigned in the plugin config.yml.
 *   Example usage in config.yml:
 *     ranks:
 *      FORTNITEBALLS:
 *         permissions:
 *           - "towns.delete"
 *                  - To see how to add ranks see more documentation from [tree.towns.constants.TownRank] class.
 *   - To see how the plugin adds ranks refer to the [tree.towns.Towns] class
 */
enum class TownPermissions(val permission: String) {
    DELETE_TOWN("towns.delete"),
    MANAGE_RANKS("towns.rank.addremove"),
    TOWN_INVITE_PLAYER("towns.invite"),
    TOWN_RESIDENT("towns.resident.info"),
    LEAVE_TOWN("towns.leave"),
    TOWN_CHUNK_CLAIM("towns.chunks.claim"),
    TOWN_CHUNK_UNCLAIM("towns.chunks.unclaim"),
    TOWN_DEPOSIT("towns.balance.deposit"),
    TOWN_WITHDRAW("towns.balance.withdraw"),
    TOWN_KICK_PLAYER("towns.kick"),
    TOWN_SET_NAME("towns.set.name"),
    TOWN_SET_DESCRIPTION("towns.set.description"),
    TOWN_SET_SPAWN("towns.set.spawn");
}