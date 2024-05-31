package tree.towns.utils.enums
// made this before I copied messages class from phonon but to lazy to fix.
import org.bukkit.ChatColor
enum class TownMessages(val message: String) {
    UNKNOWN_SUBCOMMAND("${ChatColor.RED}Unknown subcommand. Do /town help for a list of commands."),
    NO_PERMISSION("${ChatColor.DARK_RED}You do not have permission to perform this action."),
    NOT_IN_TOWN("${ChatColor.RED}You are not a member of a town."),
}
enum class TownCreateMessages(val message: String) {
    ALREADY_EXISTS("${ChatColor.RED}Town '%s' already exists."),
    ALREADY_MEMBER("${ChatColor.RED}You are already a member of '%s'. You cannot create a new town."),
    SUCCESSFUL("${ChatColor.GREEN}Town '%s' has been created!"),
    INVALID_USAGE("${ChatColor.RED}Usage: /town new <town_name>"),
    COOLDOWN("${ChatColor.RED}You still have a town cooldown of '%s ."),
    CHUNK_OCCUPIED("${ChatColor.RED}The chunk is already occupied by the town '%s'. You cannot create a new town in the same chunk.")
}

enum class TownDeleteMessages(val message: String) {
    SUCCESSFUL("${ChatColor.GREEN}Town '%s' has been deleted."),
    NO_PERMISSION("${ChatColor.DARK_RED}You do not have permission to perform this action."),
    // naming of these made before concept was done lol so not_mayor function is kinda shitty name
    // NOT_MAYOR("${ChatColor.RED}You can only delete a town if you have the proper rank."),
    // NOT_MEMBER("${ChatColor.RED}You are not a member of any town.")
}
enum class TownRankMessages(val message: String) {
    ALREADY_HAS_RANK("${ChatColor.RED}Player '%s' already has rank '%s'."),
    DOES_NOT_HAVE_RANK("${ChatColor.RED}Player '%s' does not have rank '%s'."),
    NO_PERMISSION("${ChatColor.DARK_RED}You do not have permission to perform this action."),
    NOT_MEMBER("${ChatColor.RED}Player '%s' is not a member of your town."),
    PLAYER_NOT_FOUND("${ChatColor.RED}Player '%s' is not online."),
    INVALID_USAGE("${ChatColor.RED}Usage: /town rank <add/remove> <rank_name> <player_name>.")
}
enum class TownInvitationMessages(val message: String) {
    JOIN_SUCCESSFUL("${ChatColor.GREEN}You have successfully joined the town '%s'."),
    ACCEPT_USAGE("${ChatColor.RED}Usage: /town accept <town_name>"),
    JOIN_FAILURE_FORMAT("${ChatColor.RED}Failed to join the town. The invitation for '%s' may have expired or is invalid."),
    DECLINE_SUCCESSFUL("${ChatColor.GREEN}You have declined the invitation to join the town '%s'."),
    DECLINE_FAILURE("${ChatColor.RED}Failed to decline the invitation. The invitation may have expired or is invalid."),
    DECLINE_USAGE("${ChatColor.RED}Usage: /town deny <town_name>")
}
enum class TownClaimMessages(val message: String) {
    CLAIM_SUCCESS("${ChatColor.GREEN}Chunk ${ChatColor.DARK_GREEN}['%s','%s']${ChatColor.GREEN} claimed for town '%s'."),
    CHUNK_IS_CLAIMED("${ChatColor.RED}Chunk is already claimed by a town"),
    UNCLAIM_SUCCESS("${ChatColor.GREEN}Chunk ${ChatColor.DARK_GREEN}['%s','%s']${ChatColor.GREEN} unclaimed for town '%s'."),
    UNCLAIM_FAILURE("${ChatColor.RED}Chunk is not claimed by your town."),
    CHUNK_NOT_CLAIMED_BY_TOWN("${ChatColor.RED}Chunk is not claimed by your town or is claimed by a different one."),
    CHUNK_UNCLAIMABLE("${ChatColor.RED}Chunk is your towns homeblock and therefore cannot be unclaimed.")
}

