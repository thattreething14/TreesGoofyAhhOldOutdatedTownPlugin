package tree.towns.commands

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import tree.towns.Config
import tree.towns.Towns
import tree.towns.Towns.plugin
import tree.towns.TownsAPI
import tree.towns.constants.TownRank
import tree.towns.gui.GuiBuilder
import tree.towns.gui.GuiListener
import tree.towns.gui.ItemStackBuilder
import tree.towns.utils.Messages
import tree.towns.utils.enums.*
import java.util.*
import kotlin.math.ceil
@Suppress("DEPRECATION")
class TownCommand : CommandExecutor, TabCompleter {
    private val SUB_COMMANDS = arrayOf(
        "new",
        "delete",
        "rank",
        "invite",
        "accept",
        "deny",
        "claim",
        "unclaim",
        "leave",
        "help",
        "deposit",
        "withdraw",
        "info",
        "kick",
        "set",
        "list",
        "online",
        "ranklist",
        "reslist",
        "gui",
        "spawn"
    )
    private val SET_SUB_COMMANDS = arrayOf(
        "mayor",
        "name",
        "description",
        "spawn"
    )

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            Messages.error(sender, "Only players can use this command")
            return true
        }
        if (args.isEmpty()) {
            executeHelpCommand(sender, 1)
            return true
        }

        val subCommand = args[0].lowercase(Locale.getDefault())

        when (subCommand) {
            "new" -> executeNewTownCommand(sender, args)
            "delete" -> executeDeleteTownCommand(sender)
            "rank" -> executeRankCommand(sender, args)
            "invite" -> executeInviteCommand(sender, args)
            "accept" -> executeAcceptCommand(sender, args)
            "deny" -> executeDenyCommand(sender, args)
            "leave" -> executeLeaveCommand(sender)
            "help" -> {
                val page = if (args.size > 1) {
                    args[1].toIntOrNull() ?: 1
                } else {
                    1
                }
                executeHelpCommand(sender, page)
            }
            "deposit" -> executeDepositCommand(sender, args)
            "withdraw" -> executeWithdrawCommand(sender, args)
            "claim" -> executeClaimCommand(sender)
            "unclaim" -> executeUnclaimCommand(sender)
            "info" -> executeInfoCommand(sender, args)
            "kick" -> executeKickCommand(sender, args)
            "set" -> executeSetCommand(sender, args)
            "list" -> executeListCommand(sender, args)
            "online" -> executeOnlineCommand(sender, args)
            "ranklist" -> executeRankListCommand(sender, args)
            "reslist" -> executeResListCommand(sender, args)
            "gui" -> executeGuiCommand(sender, args)
            "spawn" -> executeSpawnCommand(sender, args)
            else -> {
                executeHelpCommand(sender, 1)
                return true
            }
        }

        return true
    }

    private fun executeSpawnCommand(sender: Player, args: Array<out String>) {
        if (args.size > 1) {
            val town = args[1]

            if (Towns.isTownExists(town)) {
                Towns.teleportToTownSpawn(sender, town)
            } else {
                Messages.error(sender, "Town '$town' does not exist.")
            }
        } else {
            val playerName = sender.uniqueId.toString()
            val playerTown = Towns.getTownByPlayer(playerName)

            if (playerTown != null) {
                Towns.teleportToTownSpawn(sender, playerTown)
            } else {
                Messages.error(sender, "You are not a member of any town.")
            }
        }
    }

    private fun executeHelpCommand(sender: Player, page: Int) {
        val commandsPerPage = 6
        val commandList = listOf(
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}new ${ChatColor.WHITE}<town> - ${ChatColor.AQUA}Create a new town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}delete - ${ChatColor.AQUA}Delete your town (mayor only).",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}rank ${ChatColor.WHITE}<add/remove> <rank> <player> - ${ChatColor.AQUA}Manage player ranks.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}invite ${ChatColor.WHITE}<player> - ${ChatColor.AQUA}Invite a player to your town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}accept ${ChatColor.WHITE}<town> - ${ChatColor.AQUA}Accept a town invitation.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}deny ${ChatColor.WHITE}<town> - ${ChatColor.AQUA}Deny a town invitation.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}leave - ${ChatColor.AQUA}Leave your current town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}help - ${ChatColor.AQUA}Show town commands.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}deposit ${ChatColor.WHITE}<amount> - ${ChatColor.AQUA}Deposit money into town balance.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}withdraw ${ChatColor.WHITE}<amount> - ${ChatColor.AQUA}Withdraw money from town balance.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}claim - ${ChatColor.AQUA}Claim the chunk you're standing in for your town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}unclaim - ${ChatColor.AQUA}Unclaim the chunk you're standing in for your town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}info ${ChatColor.WHITE}<town> - ${ChatColor.AQUA}Display information about a town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}ranklist ${ChatColor.WHITE}<town> - ${ChatColor.AQUA}Show all players' ranks in a town.",
            "${ChatColor.BLUE}/t ${ChatColor.AQUA}reslist - ${ChatColor.AQUA}Show all residents in your town.",
        )
        val totalPages = ceil(commandList.size / commandsPerPage.toDouble()).toInt()

        if (page < 1 || page > totalPages) {
            Messages.error(sender, "Invalid page number. Available pages: 1-$totalPages")
            return
        }

        Messages.print(sender, "${ChatColor.DARK_BLUE}Town Commands - Page $page:")
        val startIndex = (page - 1) * commandsPerPage
        val endIndex = minOf(startIndex + commandsPerPage, commandList.size)

        for (i in startIndex until endIndex) {
            Messages.print(sender, commandList[i], false)
        }

        val navigationText = TextComponent("${ChatColor.DARK_BLUE}[<<] Previous Page ${ChatColor.AQUA}| ")
        navigationText.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t help ${page - 1}")

        val nextNavigationText = TextComponent("${ChatColor.DARK_BLUE}Next Page [>>]")
        nextNavigationText.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t help ${page + 1}")
        sender.spigot().sendMessage(navigationText, nextNavigationText)
    }

    private fun executeGuiCommand(sender: Player, args: Array<out String>) {
        val town = if (args.isEmpty() || args.size < 2) {
            val playerTown = Towns.getTownByPlayer(sender.uniqueId.toString())
            if (playerTown != null) {
                playerTown.toString()
            } else {
                Messages.error(sender, "You are not in a town.")
                return
            }
        } else {
            args[1]
        }
        val townInfo = Towns.getTownInfo(town)

        if (townInfo != null) {
            val gui = createTownGui(town, townInfo, sender)
            gui.open(sender)
        } else {
            Messages.error(sender, "Town $town not found.")
        }
    }

    private fun createTownGui(town: String, townInfo: Towns.TownInfo, player: Player): GuiBuilder {
        val gui = GuiBuilder("${ChatColor.DARK_BLUE}Town Information - $town", 27)
        gui.allowItemClick = false
        val onlineMembers = getOnlineMembersInTown(town)
        val maxChunks = Towns.getMaxChunksForTown(town)
        val townRanking = getTownRanking(town)
        gui.setItem(
            10,
            ItemStackBuilder(Material.DIAMOND_SWORD).displayName("${ChatColor.BLUE}Mayor: ${ChatColor.AQUA}${townInfo.mayor}")
                .build()
        )
        gui.setItem(
            12,
            ItemStackBuilder(Material.GOLDEN_APPLE).displayName("${ChatColor.BLUE}Members [${onlineMembers.size}]: ${ChatColor.AQUA}${townInfo.members}")
                .build()
        )
        gui.setItem(
            13,
            ItemStackBuilder(Material.NETHERITE_BLOCK).displayName("${ChatColor.BLUE}Overall Power: ${ChatColor.AQUA}${townRanking.rank}")
                .build()
        )
        gui.setItem(
            14,
            ItemStackBuilder(Material.EMERALD).displayName("${ChatColor.BLUE}Balance: ${ChatColor.AQUA}${townInfo.balance}")
                .build()
        )
        gui.setItem(
            16,
            ItemStackBuilder(Material.GRASS_BLOCK).displayName("${ChatColor.BLUE}Claimed Chunks: ${ChatColor.AQUA}${townInfo.claimedChunks.size}/$maxChunks")
                .build()
        )

        Bukkit.getPluginManager().registerEvents(GuiListener(gui), plugin)
        gui.open(player)
        return gui
    }

    private fun getTownRanking(town: String): TownRanking {
        val townMembersCount = Towns.getTownMembers(town).size
        val allTowns = Towns.getAllTowns()

        // sorts towns by the number of members in descending order
        val sortedTowns = allTowns.sortedByDescending { Towns.getTownMembers(it).size }

        // finds the rank of the current town
        val townRank = sortedTowns.indexOf(town) + 1

        return TownRanking(townRank, townMembersCount)
    }

    data class TownRanking(val rank: Int, val membersCount: Int)

    private fun executeListCommand(sender: Player, args: Array<out String>) {
        if (args.size < 3 || args[1].lowercase(Locale.getDefault()) != "by") {
            Messages.error(sender, "Usage: /t list by <chunks|members|wealth>")
            return
        }

        when (val option = args[2].lowercase(Locale.getDefault())) {
            "chunks" -> listTownsByChunks(sender)
            "members" -> listTownsByMembers(sender)
            "wealth" -> listTownsByWealth(sender)
            else -> {
                Messages.error(sender, "Unknown option: $option")
                Messages.error(sender, "Usage: /t list by <chunks|members|wealth>")
            }
        }
    }

    private fun listTownsByChunks(sender: Player) {
        val towns = Towns.getAllTowns()

        val sortedTowns = towns.sortedByDescending { town ->
            Towns.getTownClaimedChunks(town).size
        }

        displayTownList(sender, "Chunks", sortedTowns) { town ->
            "${Towns.getTownClaimedChunks(town).size} Chunks"
        }
    }

    private fun executeRankListCommand(sender: Player, args: Array<out String>) {
        val town = if (args.isEmpty() || args.size < 2) {
            val playerTown = Towns.getTownByPlayer(sender.uniqueId.toString())
            if (playerTown != null) {
                playerTown.toString()
            } else {
                Messages.error(sender, "You are not in a town.")
                return
            }
        } else {
            args[1]
        }

        if (Towns.isTownExists(town)) {
            val townRanks = Towns.getTownRanks(town)

            if (townRanks.isNotEmpty()) {
                Messages.print(sender, "${ChatColor.DARK_BLUE}Ranks in Town ${ChatColor.AQUA}$town:", false)

                townRanks.forEach { rank ->
                    val playersWithRank = Towns.getPlayersWithRanks(town, rank)
                        .filterNot { it.first == TownRank.MEMBER || it.first == TownRank.MAYOR }
                        .flatMap { (_, names) ->
                            names.mapNotNull { name ->
                                Towns.getPlayerName(UUID.fromString(name))
                            }
                        }

                    if (playersWithRank.isNotEmpty()) {
                        Messages.print(
                            sender,
                            "${ChatColor.BLUE}${rank.name.substring(0, 1).toUpperCase()}${rank.name.substring(1).toLowerCase()}s: ${ChatColor.AQUA}${playersWithRank.joinToString(", ")}",
                            false
                        )
                    }
                }
            } else {
                Messages.print(sender, "${ChatColor.BLUE}No ranks found in the town $town.")
            }
        } else {
            Messages.error(sender, "The town $town does not exist.")
        }
    }

    private fun executeResListCommand(sender: Player, args: Array<out String>) {
        val town = if (args.isEmpty() || args.size < 2) {
            val playerTown = Towns.getTownByPlayer(sender.uniqueId.toString())
            if (playerTown != null) {
                playerTown.toString()
            } else {
                Messages.error(sender, "You are not in a town.")
                return
            }
        } else {
            args[1]
        }
        if (Towns.isTownExists(town)) {
            val residents = Towns.getTownMembers(town).mapNotNull { Towns.getPlayerName(UUID.fromString(it)) }
            val townSize = residents.size
            Messages.print(sender, "${ChatColor.DARK_BLUE}Residents in Town ${ChatColor.AQUA}$town [$townSize]:", false)

            if (residents.isNotEmpty()) {
                Messages.print(sender, "${ChatColor.AQUA}${residents.joinToString(", ")}", false)
            } else {
                Messages.print(sender, "${ChatColor.BLUE}No residents in the town.", false)
            }
        } else {
            Messages.error(sender, "The town $town does not exist.")
        }
    }

    private fun listTownsByMembers(sender: Player) {
        val towns = Towns.getAllTowns()

        val sortedTowns = towns.sortedByDescending { town ->
            Towns.getTownMembers(town).size
        }

        displayTownList(sender, "Members", sortedTowns) { town ->
            "${Towns.getTownMembers(town).size} Members"
        }
    }

    private fun listTownsByWealth(sender: Player) {
        val towns = Towns.getAllTowns()

        val sortedTowns = towns.sortedByDescending { town ->
            Towns.getTownBalance(town)
        }

        displayTownList(sender, "Wealth", sortedTowns) { town ->
            "${Towns.getTownBalance(town)} Wealth"
        }
    }

    private fun isValidtown(name: String): Boolean {
        val regex = Regex("^[a-zA-Z0-9_-]+$")
        return name.matches(regex) && !name.contains(" ") && name.length <= 20
    }

    private fun displayTownList(
        sender: Player,
        category: String,
        towns: List<String>,
        countProvider: (String) -> String
    ) {
        Messages.print(sender, "${ChatColor.DARK_BLUE}Top Towns by $category:", false)

        towns.take(10).forEachIndexed { index, town ->
            val townText = TextComponent("${ChatColor.AQUA}${index + 1}. $town - ${countProvider(town)}")
            townText.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t info $town")
            sender.sendMessage(townText)
        }
    }

    private fun executeKickCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.error(sender, "Usage: /t kick <playerName>")
            return
        }

        val targetPlayerName = args[1]
        val offlinePlayer = Bukkit.getOfflinePlayer(targetPlayerName)
        val targetPlayerUUID = offlinePlayer.uniqueId.toString()
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (town != null) {
            if (sender.hasPermission(TownPermissions.TOWN_KICK_PLAYER.permission)) {
                val targetPlayer = Bukkit.getPlayerExact(targetPlayerName)

                if (targetPlayer != null) {
                    val targetPlayerTown = Towns.getTownByPlayer(targetPlayerUUID)

                    if (targetPlayerTown == town) {
                        val senderRank = Towns.getPlayerRank(sender.uniqueId.toString(), town)
                        val targetRank = Towns.getPlayerRank(targetPlayerUUID, town)
                        if (senderRank != null && targetRank != null && senderRank.priority > targetRank.priority) {
                            Towns.removePlayerFromTown(targetPlayerUUID, town)
                            val townMembers = Towns.getTownMembers(town)
                            townMembers.forEach { member ->
                                val memberPlayer = Bukkit.getPlayerExact(member)
                                memberPlayer?.sendMessage("${ChatColor.DARK_GREEN}$targetPlayerName${ChatColor.GREEN} has been kicked from the town by ${sender.name}.")
                            }
                            targetPlayer.sendMessage("${ChatColor.RED}You have been kicked from the town by ${sender.name}.")
                        } else {
                            Messages.print(
                                sender,
                                "${ChatColor.RED}You don't have the authority to kick $targetPlayerName."
                            )
                        }
                        val townMembersAfterLeave = Towns.getTownMembers(town)
                        val baseChunks = Config.baseChunks
                        val chunksPerMember = Config.chunksPerMember
                        val maxChunksPerTown = (baseChunks + chunksPerMember * townMembersAfterLeave.size)
                        Towns.setMaxChunksForTown(town, maxChunksPerTown)
                    } else {
                        Messages.error(sender, "$targetPlayerName is not a member of your town.")
                    }
                } else {
                    Messages.error(sender, "Player $targetPlayerName is not online.")
                }
            } else {
                Messages.error(sender, "You do not have permission to kick players from the town.")
            }
        } else {
            Messages.error(sender, "You are not a member of any town.")
        }
    }
    private fun executeInfoCommand(sender: Player, args: Array<out String>) {
        val town = if (args.isEmpty() || args.size < 2) {
            val playerTown = Towns.getTownByPlayer(sender.uniqueId.toString())
            if (playerTown != null) {
                playerTown.toString()
            } else {
                Messages.error(sender, "You are not in a town.")
                return
            }
        } else {
            args[1]
        }
        val townInfo = Towns.getTownInfo(town)

        if (townInfo != null) {
            val maxChunksPerTown = Towns.getMaxChunksForTown(town)
            Messages.print(sender, "${ChatColor.DARK_BLUE}Town Information for ${ChatColor.AQUA}$town:", false)
            Messages.print(sender, "${ChatColor.BLUE}Mayor: ${ChatColor.AQUA}${townInfo.mayor}", false)

            // Check for online members only if needed
            val onlineMembers = if (townInfo.members.isNotEmpty()) getOnlineMembersInTown(town) else emptyList()

            Messages.print(
                sender,
                "${ChatColor.BLUE}Members [${onlineMembers.size}]: ${ChatColor.AQUA}${townInfo.members.joinToString(", ")}",
                false
            )
            Messages.print(sender, "${ChatColor.BLUE}Town Balance: ${ChatColor.AQUA}${townInfo.balance}", false)
            Messages.print(
                sender,
                "${ChatColor.BLUE}Claimed Chunks: ${ChatColor.AQUA}${townInfo.claimedChunks.size}/$maxChunksPerTown",
                false
            )

            val description = Towns.getTownDescription(town)
            if (description != null) {
                Messages.print(sender, "${ChatColor.BLUE}Description: ${ChatColor.AQUA}$description", false)
            } else {
                Messages.print(
                    sender,
                    "${ChatColor.BLUE}Description: ${ChatColor.AQUA}/t set description to set a town description!",
                    false
                )
            }

            val ranks = townInfo.ranks.filterNot { it == TownRank.MAYOR || it == TownRank.MEMBER }
            ranks.forEach { rank ->
                /** not explaining this every time but basically originally
                 * I coded it, so it saves playername instead of uuid, so I have to change
                 * it every time so uhh mb...*/
                val playersWithRanksUUID = Towns.getPlayersWithRanks(town, rank)

                if (playersWithRanksUUID.isNotEmpty()) {
                    val playerNames = playersWithRanksUUID.flatMap { (_, names) ->
                        names.mapNotNull { name ->
                            Towns.getPlayerName(UUID.fromString(name))
                        }
                    }
                    val message = if (playerNames.isNotEmpty()) {
                        "${ChatColor.BLUE}${rank.name.substring(0, 1).toUpperCase()}${rank.name.substring(1).toLowerCase()}s: ${ChatColor.AQUA}${playerNames.joinToString(", ")}"
                    } else {
                        "${ChatColor.BLUE}${rank.name.toLowerCase()}s: ${ChatColor.AQUA}None"
                    }
                    Messages.print(sender, message, false)
                } else {
                    Messages.print(sender, "${ChatColor.BLUE}${rank.name.toLowerCase()}s: ${ChatColor.AQUA}None", false)
                }
            }
        } else {
            Messages.error(sender, "Town $town not found.")
        }
    }
    private fun executeLeaveCommand(sender: Player) {
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (town != null) {
            val senderRank = Towns.getPlayerRank(sender.uniqueId.toString(), town)

            if (senderRank == TownRank.MAYOR) {
                Messages.print(
                    sender,
                    "${ChatColor.DARK_RED}Mayors cannot leave their own town. Use /town delete to disband the town."
                )
                return
            }

            if (sender.hasPermission(TownPermissions.LEAVE_TOWN.permission)) {
                val townMembersBeforeLeave = Towns.getTownMembers(town)

                Towns.removePlayerFromTown(sender.uniqueId.toString(), town)

                Messages.print(sender, "${ChatColor.GREEN}You have left the town.")
                val townMembersAfterLeave = Towns.getTownMembers(town)
                // updates the maximum claimed chunks after the player leaves
                val baseChunks = Config.baseChunks
                val chunksPerMember = Config.chunksPerMember
                val maxChunksPerTown = (baseChunks + chunksPerMember * townMembersAfterLeave.size)
                Towns.setMaxChunksForTown(town, maxChunksPerTown)
                townMembersBeforeLeave.forEach { member ->
                    if (!townMembersAfterLeave.contains(member)) {
                        val memberPlayer = Bukkit.getPlayerExact(member)
                        memberPlayer?.sendMessage("${ChatColor.DARK_GREEN}${sender.uniqueId}${ChatColor.GREEN} has left the town D: (sadge)!")
                    }
                }

            } else {
                Messages.print(sender, "${ChatColor.DARK_RED}You do not have permission to leave the town.")
            }
        } else {
            Messages.print(sender, "${ChatColor.DARK_RED}You are not a member of any town.")
        }
    }

    private fun executeNewTownCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.print(sender, TownCreateMessages.INVALID_USAGE.message)
            return
        }
        val town = args[1]
        if (!isValidtown(town)) {
            Messages.print(
                sender,
                "${ChatColor.RED}Invalid town name. Town name can only contain letters (a-z, A-Z), numbers (0-9), and underscore (_)."
            )
            return
        }
        if (Towns.isTownExists(town)) {
            Messages.print(sender, TownCreateMessages.ALREADY_EXISTS.message.format(town))
            return
        }

        val playerName = sender.uniqueId.toString()

        if (Towns.isPlayerInTown(playerName)) {
            val currentTown = Towns.getTownByPlayer(playerName)
            Messages.print(sender, TownCreateMessages.ALREADY_MEMBER.message.format(currentTown))
            return
        }
        val chunk = sender.location.chunk
        if (Towns.isChunkClaimed(chunk)) {
            Messages.error(sender, "Chunk you are in is already claimed by another town")
            return
        }
        val townCreateCost = Config.townCreateCost
        val economy = Towns.getEconomy()

        if (economy != null) {
            val playerBalance = economy.getBalance(sender)

            if (economy.has(sender, townCreateCost)) {
                economy.withdrawPlayer(sender, townCreateCost)

                Messages.print(
                    sender,
                    "${ChatColor.GREEN}Successfully created town! $townCreateCost dollars has been taken out of your bank."
                )
            } else {
                Messages.print(
                    sender,
                    "${ChatColor.RED}You need at least $townCreateCost to create a town. Your current balance: $playerBalance"
                )
                return
            }
        } else {
            Messages.error(sender, "Economy plugin not found. Unable to create town.")
            return
        }
        val members = mutableListOf(sender)

        Towns.createTown(town, sender, members, chunk)
        Bukkit.broadcastMessage(TownCreateMessages.SUCCESSFUL.message.format(town))
    }
    private fun executeDeleteTownCommand(sender: Player) {
        val playerName = sender.uniqueId.toString()
        val town = Towns.getTownByMayor(playerName)

        if (town != null) {
            if (sender.hasPermission(TownPermissions.DELETE_TOWN.permission)) {
                Towns.deleteTown(town)
                Bukkit.broadcastMessage(TownDeleteMessages.SUCCESSFUL.message.format(town))
            } else {
                Messages.print(sender, TownDeleteMessages.NO_PERMISSION.message)
            }
        } else {
            Messages.print(sender, TownDeleteMessages.NO_PERMISSION.message)
        }
    }
    private fun executeRankCommand(sender: Player, args: Array<out String>) {
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())
        if (town != null) {
            if (args.size < 4) {
                if (args.size == 3) {
                    val ranks = TownRank.entries.filter { it != TownRank.MEMBER && it != TownRank.MAYOR }
                        .sortedByDescending { it.priority }
                    Messages.print(sender, "Available ranks: ${ranks.joinToString(", ") { it.name.substring(0, 1).toUpperCase() + it.name.substring(1).toLowerCase() }}")
                } else {
                    Messages.print(sender, TownRankMessages.INVALID_USAGE.message)
                    return
                }
            } else {
                val targetPlayer = Bukkit.getOfflinePlayer(args[3])
                if (!targetPlayer.hasPlayedBefore()) {
                    Messages.error(sender, "Player ${args[3]} does not exist.")
                    return
                }

                val targetPlayerName = targetPlayer.uniqueId.toString()
                val targetsTown = if (targetPlayer.isOnline) { targetPlayer.player?.let { TownsAPI.getInstance().getResident(it) } } else { null }

                if (targetsTown == town) {
                    val targetRankName = args[2].uppercase(Locale.getDefault())
                    val action = args[1].lowercase(Locale.getDefault())

                    if (sender.hasPermission(TownPermissions.MANAGE_RANKS.permission)) {
                        val targetRank = try {
                            TownRank.valueOf(targetRankName)
                        } catch (e: IllegalArgumentException) {
                            Messages.print(sender, TownRankMessages.INVALID_USAGE.message)
                            return
                        }
                        val senderRank = Towns.getPlayerRank(sender.uniqueId.toString(), town)
                        // for error logging if sql ever does a goofy:
                        // println("Sender Rank: $senderRank, Target Rank: $targetRank")

                        if (senderRank != null && senderRank.priority >= targetRank.priority) {
                            when (action) {
                                "add" -> {
                                    if (!Towns.hasPlayerRank(targetPlayerName, town, targetRank)) {
                                        val existingRank = Towns.getPlayerRank(targetPlayerName, town)
                                        val existingPermissions = if (existingRank != null) {
                                            Config.getRankPermissions(existingRank)
                                        } else {
                                            emptyList()
                                        }

                                        Towns.addPlayerToRank(targetPlayerName, town, targetRank)

                                        // Combine existing and new permissions
                                        val newPermissions = existingPermissions + Config.getRankPermissions(
                                            targetRank
                                        )
                                        Config.assignRankPermissions(args[3], newPermissions)
                                        val townMembers = Towns.getTownMembers(town).mapNotNull { Towns.getPlayerName(UUID.fromString(it)) }
                                        townMembers.forEach { member ->
                                            val memberPlayer = Bukkit.getPlayerExact(member)
                                            memberPlayer?.sendMessage("${ChatColor.DARK_GREEN}${args[3]}${ChatColor.GREEN} has been promoted to ${targetRank.name.substring(0, 1).toUpperCase()}${targetRank.name.substring(1).toLowerCase()} in $town!")
                                        }
                                    } else {
                                        Messages.print(sender, TownRankMessages.ALREADY_HAS_RANK.message.format(args[3], "${targetRank.name.substring(0, 1).toUpperCase()}${targetRank.name.substring(1).toLowerCase()}"))
                                    }
                                }

                                "remove" -> {
                                    if (senderRank.priority > targetRank.priority) {
                                        if (Towns.hasPlayerRank(targetPlayerName, town, targetRank)) {
                                            // checks if the target rank isn't member or mayor (so you cant remove them bc uh obviously)
                                            if (targetRank != TownRank.MEMBER && targetRank != TownRank.MAYOR) {
                                                val currentRank = Towns.getPlayerRank(targetPlayerName, town)

                                                Towns.removePlayerFromRank(targetPlayerName, town, targetRank)
                                                val townMembers = Towns.getTownMembers(town)
                                                    .mapNotNull { Towns.getPlayerName(UUID.fromString(it)) }
                                                townMembers.forEach { member ->
                                                    val memberPlayer = Bukkit.getPlayerExact(member)
                                                    memberPlayer?.sendMessage("${ChatColor.DARK_GREEN}${args[3]}${ChatColor.GREEN} has been demoted from ${targetRank.name.substring(0, 1).toUpperCase()}${targetRank.name.substring(1).toLowerCase()} in $town!")
                                                }
                                                if (currentRank != null) {
                                                    val ranks = TownRank.entries
                                                        .filter { it != TownRank.MEMBER && it != TownRank.MAYOR }
                                                        .sortedByDescending { it.priority }
                                                    val secondToHighestRank = ranks.getOrNull(1)

                                                    if (secondToHighestRank != null) {
                                                        val permissions = Config.getRankPermissions(
                                                            secondToHighestRank
                                                        )
                                                        Config.assignRankPermissions(args[3], permissions)
                                                    } else {
                                                        Messages.print(
                                                            sender,
                                                            "Error: Unable to find second-to-highest rank."
                                                        )
                                                    }
                                                } else {
                                                    Messages.print(
                                                        sender,
                                                        "Error: Unable to determine current rank for the player."
                                                    )
                                                }
                                            } else {
                                                Messages.print(sender, TownRankMessages.INVALID_USAGE.message)
                                            }
                                        } else {
                                            Messages.print(
                                                sender,
                                                TownRankMessages.DOES_NOT_HAVE_RANK.message.format(
                                                    args[3],
                                                    "${
                                                        targetRank.name.substring(0, 1).toUpperCase()
                                                    }${targetRank.name.substring(1).toLowerCase()}"
                                                )
                                            )
                                        }
                                    } else {
                                        Messages.print(sender, TownRankMessages.NO_PERMISSION.message)
                                    }
                                }
                                    else -> {
                                        Messages.print(sender, TownRankMessages.INVALID_USAGE.message)
                                    }
                                }
                            } else {
                                Messages.print(sender, TownRankMessages.NO_PERMISSION.message)
                            }
                        } else {
                            Messages.print(sender, TownRankMessages.NO_PERMISSION.message)
                        }
                    } else {
                        Messages.print(sender, "The target player is not a resident of your town.")
                    }
                }
            } else {
                Messages.print(sender, TownRankMessages.NOT_MEMBER.message.format(sender.uniqueId.toString()))
            }
        }

    private fun executeInviteCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.error(sender, "Usage: /t invite <player>")
            return
        }
        val invitedPlayerName = args[1]
        val offlinePlayer = Bukkit.getOfflinePlayer(invitedPlayerName)
        val invitedPlayerUUID = offlinePlayer.uniqueId
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (town != null) {
            if (sender.hasPermission(TownPermissions.TOWN_INVITE_PLAYER.permission)) {
                val invitedPlayer = Bukkit.getPlayerExact(invitedPlayerName)

                if (invitedPlayer != null) {
                    val invitedPlayerTown = Towns.getTownByPlayer(invitedPlayerUUID.toString())

                    if (invitedPlayerTown == null) {
                        if (!Towns.isPlayerInvited(invitedPlayerUUID.toString(), town)) {
                            val success = Towns.storeInvitation(sender.uniqueId.toString(), invitedPlayerUUID.toString(), town)

                            if (success) {
                                val confirmText = TextComponent("${ChatColor.GREEN}[Confirm]")
                                confirmText.clickEvent =
                                    ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t accept $town")

                                val denyText = TextComponent("${ChatColor.RED}[Deny]")
                                denyText.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/t deny $town")
                                val invitationText =
                                    TextComponent("${ChatColor.GREEN}Invitation sent to $invitedPlayerName. They can: ")
                                invitationText.addExtra(confirmText)
                                invitationText.addExtra("${ChatColor.GREEN} or ")
                                invitationText.addExtra(denyText)
                                sender.sendMessage(invitationText)
                                val invitedPlayerMessage =
                                    TextComponent("${ChatColor.GREEN}You have been invited to join the town $town. You can: ")
                                invitedPlayerMessage.addExtra(confirmText)
                                invitedPlayerMessage.addExtra("${ChatColor.GREEN} or ")
                                invitedPlayerMessage.addExtra(denyText)
                                invitedPlayer.sendMessage(invitedPlayerMessage)
                            } else {
                                Messages.print(
                                    sender,
                                    "${ChatColor.RED}An error occurred while sending the invitation."
                                )
                            }
                        } else {
                            Messages.print(
                                sender,
                                "${ChatColor.RED}$invitedPlayerName has already been invited to join the town."
                            )
                        }
                    } else {
                        Messages.error(sender, "$invitedPlayerName is already a member of a town.")
                    }
                } else {
                    Messages.error(sender, "Player $invitedPlayerName is not online.")
                }
            } else {
                Messages.error(sender, "You do not have permission to invite players to the town.")
            }
        } else {
            Messages.error(sender, "You are not a member of any town.")
        }
    }

    private fun executeAcceptCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.print(sender, TownInvitationMessages.ACCEPT_USAGE.message)
            return
        }

        val town = args[1]

        val success = Towns.acceptInvitation(sender.uniqueId.toString(), town)
        val townMembers = Towns.getTownMembers(town)

        if (success) {
            Config.assignRankPermissions(sender.name, TownRank.MEMBER)
            Towns.addPlayerToRank(sender.uniqueId.toString(), town, TownRank.MEMBER)

            // Calculate max chunks based on the formula
            val baseChunks = Config.baseChunks
            val chunksPerMember = Config.chunksPerMember
            val townMembersCount = townMembers.size
            val maxChunks = (baseChunks + chunksPerMember * townMembersCount)
            Towns.setMaxChunksForTown(town, maxChunks)

            townMembers.forEach { member ->
                Bukkit.getPlayerExact(member)
                    ?.sendMessage("${ChatColor.DARK_GREEN}${sender.name}${ChatColor.GREEN} has joined the town.")
            }
            Messages.print(sender, TownInvitationMessages.JOIN_SUCCESSFUL.message.format(town))

        } else {
            Messages.print(sender, TownInvitationMessages.JOIN_FAILURE_FORMAT.message.format(town))
        }
    }

    private fun executeDenyCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.print(sender, TownInvitationMessages.DECLINE_USAGE.message)
            return
        }

        val town = args[1]

        val success = Towns.declineInvitation(sender.uniqueId.toString(), town)
        val townMembers = Towns.getTownMembers(town)
        if (success) {
            Messages.print(sender, TownInvitationMessages.DECLINE_SUCCESSFUL.message.format(town))
            townMembers.forEach { member ->
                Bukkit.getPlayerExact(member)
                    ?.sendMessage("${ChatColor.DARK_GREEN}${sender.name}${ChatColor.GREEN} has denied the invitation to join your town.")
            }
        } else {
            Messages.print(sender, TownInvitationMessages.DECLINE_FAILURE.message.format(town))
        }
    }

    private fun executeDepositCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.error(sender, "Usage: /t deposit <amount>")
            return
        }
        if (!sender.hasPermission(TownPermissions.TOWN_DEPOSIT.permission)) {
            Messages.print(sender, "${ChatColor.DARK_RED}You do not have permission to perform this action.")
            return
        }
        val playerName = sender.name
        val playerUUID = sender.uniqueId.toString()
        val town = Towns.getTownByPlayer(playerUUID)

        if (town == null) {
            Messages.error(sender, "You are not a member of any town.")
            return
        }

        val amountStr = args[1]

        // checks if the amount is a whole number
        val amount: Double = try {
            val parsedAmount = amountStr.toDouble()
            if (parsedAmount % 1 != 0.0 || parsedAmount < 0) {
                throw NumberFormatException()
            }
            parsedAmount
        } catch (e: NumberFormatException) {
            Messages.error(sender, "Invalid amount format. Please enter a non-negative whole number.")
            return
        }

        val economy = Towns.getEconomy()

        if (economy != null) {
            val personalBalance = economy.getBalance(sender)

            if (personalBalance < amount) {
                Messages.print(
                    sender,
                    "${ChatColor.RED}Insufficient funds. You don't have enough money in your personal account."
                )
                return
            }

            if (economy.withdrawPlayer(playerName, amount).transactionSuccess()) {
                val currentBalance = Towns.getTownBalance(town)
                val newBalance = currentBalance + amount

                Towns.updateTownBalance(town, newBalance)

                Messages.print(
                    sender,
                    "${ChatColor.GREEN}Deposited $amount to town $town. New town balance: $newBalance"
                )
            } else {
                Messages.error(sender, "Failed to deposit funds. Please check your balance.")
            }
        } else {
            Messages.error(sender, "Vault economy not found. Please check your server setup.")
        }
    }

    private fun executeWithdrawCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.error(sender, "Usage: /t withdraw <amount>")
            return
        }

        if (!sender.hasPermission(TownPermissions.TOWN_WITHDRAW.permission)) {
            Messages.print(sender, "${ChatColor.DARK_RED}You do not have permission to perform this action.")
            return
        }

        val playerName = sender.uniqueId.toString()
        val town = Towns.getTownByPlayer(playerName)

        if (town == null) {
            Messages.error(sender, "You are not a member of any town.")
            return
        }

        val amountStr = args[1]

        val amount: Double = try {
            val parsedAmount = amountStr.toDouble()
            if (parsedAmount % 1 != 0.0 || parsedAmount < 0) {
                throw NumberFormatException()
            }
            parsedAmount
        } catch (e: NumberFormatException) {
            Messages.error(sender, "Invalid amount format. Please enter a non-negative whole number.")
            return
        }

        val currentBalance = Towns.getTownBalance(town)

        if (currentBalance < amount) {
            Messages.error(sender, "Insufficient funds in town $town.")
            return
        }

        val economy = Towns.getEconomy()

        if (economy != null) {
            val depositResult = economy.depositPlayer(playerName, amount)

            if (depositResult.transactionSuccess()) {
                val newBalance = currentBalance - amount
                Towns.updateTownBalance(town, newBalance)

                Messages.print(
                    sender,
                    "${ChatColor.GREEN}Withdrawn $amount from town $town. New town balance: $newBalance"
                )
            } else {
                Messages.error(sender, "Failed to withdraw funds. Please check your balance.")
            }
        } else {
            Messages.error(sender, "Vault economy not found. Please check your server setup.")
        }
    }

    private fun executeClaimCommand(sender: Player) {

        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (!sender.hasPermission(TownPermissions.TOWN_CHUNK_CLAIM.permission)) {
            Messages.print(sender, TownMessages.NO_PERMISSION.message)
            return
        }

        if (town == null) {
            Messages.print(sender, TownMessages.NOT_IN_TOWN.message)
            return
        }

        val townBalance = Towns.getTownBalance(town)
        val chunkClaimCost = Config.townChunkCost

        if (townBalance <= chunkClaimCost) {
            Messages.print(
                sender,
                "${ChatColor.RED}You don't have $chunkClaimCost in your town bank to claim a chunk, you only have $townBalance in your town."
            )
            return
        }

        val chunk = sender.location.chunk
        val adjacentClaimed = Towns.getAdjacentClaimedChunks(town, chunk)

        if (adjacentClaimed.isEmpty()) {
            Messages.print(
                sender,
                "${ChatColor.RED}You can only claim a chunk if it's located right next to another claimed chunk."
            )
            return
        }

        if (Towns.isChunkClaimedByTown(town, chunk)) {
            Messages.print(sender, TownClaimMessages.CHUNK_IS_CLAIMED.message)
            return
        }
        if (Towns.isChunkClaimed(chunk)) {
            Messages.print(sender, TownClaimMessages.CHUNK_IS_CLAIMED.message)
            return
        }
        val maxChunksPerTown = Towns.getMaxChunksForTown(town)
        val claimedChunks = Towns.getTownClaimedChunks(town)
        if (claimedChunks.size >= maxChunksPerTown) {
            Messages.error(sender, "Your town has reached the maximum allowed chunks.")
            return
        }

        val newTownBalance = townBalance - chunkClaimCost

        if (Towns.claimChunk(town, chunk)) {
            Messages.print(sender, TownClaimMessages.CLAIM_SUCCESS.message.format(chunk.x, chunk.z, town))
            Towns.updateTownBalance(town, newTownBalance)
        }
    }

    private fun executeUnclaimCommand(sender: Player) {

        if (!sender.hasPermission(TownPermissions.TOWN_CHUNK_UNCLAIM.permission)) {
            Messages.print(sender, TownMessages.NO_PERMISSION.message)
            return
        }
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (town == null) {
            Messages.print(sender, TownMessages.NOT_IN_TOWN.message)
            return
        }

        val chunk = sender.location.chunk

        if (!Towns.isChunkClaimedByTown(town, chunk)) {
            Messages.print(sender, TownClaimMessages.CHUNK_NOT_CLAIMED_BY_TOWN.message)
            return
        }

        if (Towns.isChunkUnclaimable(town, chunk)) {
            Messages.print(sender, TownClaimMessages.CHUNK_UNCLAIMABLE.message)
            return
        }

        if (Towns.unclaimChunk(town, chunk)) {
            Messages.print(sender, TownClaimMessages.UNCLAIM_SUCCESS.message.format(chunk.x, chunk.z, town))
        } else {
            Messages.print(sender, TownClaimMessages.UNCLAIM_FAILURE.message)
        }
    }

    private fun executeOnlineCommand(sender: Player, args: Array<out String>) {
        val town = if (args.isEmpty() || args.size < 2) {
            val playerTown = Towns.getTownByPlayer(sender.uniqueId.toString())
            if (playerTown != null) {
                playerTown.toString()
            } else {
                Messages.error(sender, "You are not in a town.")
                return
            }
        } else {
            args[1]
        }
        if (!Towns.isTownExists(town)) {
            Messages.error(sender, "The town $town does not exist.")
            return
        }

        val onlineMembers = getOnlineMembersInTown(town)

        if (onlineMembers.isNotEmpty()) {
            Messages.print(sender, "${ChatColor.DARK_BLUE}Online Members in $town [${onlineMembers.size}]:")
            Messages.print(sender, "${ChatColor.AQUA}${onlineMembers.joinToString(", ")}", false)
        } else {
            Messages.print(sender, "${ChatColor.DARK_BLUE}No online members in $town.")
        }
    }
    private fun getOnlineMembersInTown(town: String): List<String> {
        val allMembersUUID = Towns.getTownMembers(town)
        val allMembers = allMembersUUID.map { Bukkit.getOfflinePlayer(UUID.fromString(it)) }
        val onlinePlayers = Bukkit.getOnlinePlayers().map { it.name }
        return allMembers.filter { onlinePlayers.contains(it.name) }.map { it.name.toString() }
    }



    private fun executeSetCommand(sender: Player, args: Array<out String>) {
        if (args.size < 2) {
            Messages.error(sender, "Usage: /town set <subcommand>")
            return
        }

        when (val subCommand = args[1].lowercase(Locale.getDefault())) {
            "mayor" -> executeSetMayorCommand(sender, args)
            "name" -> executeSetNameCommand(sender, args)
            "description" -> executeSetDescriptionCommand(sender, args)
            "spawn" -> executeSetSpawnCommand(sender, args)
            else -> {
                Messages.error(sender, "Unknown subcommand: $subCommand")
                Messages.error(sender, "Usage: /town set <subcommand>")
            }
        }
    }
    private fun executeSetSpawnCommand(sender: Player, args: Array<out String>) {
        val town = if (args.isEmpty() || args.size < 2) {
            val playerTown = TownsAPI.getInstance().getResident(sender)
            if (playerTown != null) {
                playerTown.toString()
            } else {
                Messages.print(sender, "You are not in a town.")
                return
            }
        } else {
            args[1]
        }
        if(!sender.hasPermission(TownPermissions.TOWN_SET_SPAWN.permission)) {
            Messages.error(sender, TownMessages.NO_PERMISSION.message)
            return
        }
        val chunk = sender.location.chunk
        if(!Towns.isChunkClaimedByTown(town, chunk)) {
            Messages.error(sender, "Chunk you are standing in is not claimed by your town.")
            return
        }
        Towns.deleteOldUnclaimableStatus(town)
        Towns.markChunkAsUnclaimable(town, chunk)// I did it :D
        Towns.setTownSpawn(town, sender.location)
    }
    @Deprecated("buggy as fuck")
    private fun executeSetMayorCommand(sender: Player, args: Array<out String>) {
        if (args.size < 3) {
            Messages.error(sender, "Usage: /town set mayor <player>")
            return
        }

        val playerName = args[2]
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (town == null) {
            Messages.error(sender, "You are not a member of any town.")
            return
        }

        val currentMayor = Towns.getTownMayor(town)

        if (sender.uniqueId.toString() != currentMayor) {
            Messages.error(sender, "Only the current mayor can set a new mayor.")
            return
        }

        if (Towns.getTownByPlayer(playerName) == null) {
            Messages.error(sender, "$$playerName is not a member of your town.")
            return
        }

        if (playerName.equals(currentMayor, ignoreCase = true)) {
            Messages.error(sender, "$playerName is already the mayor.")
            return
        }

        val targetPlayer = Bukkit.getPlayerExact(playerName)

        if (targetPlayer == null) {
            Messages.error(sender, "Player $playerName not found.")
            return
        }

        // Set the new mayor and give them the mayor rank
        Towns.setTownMayor(town, playerName)
        Config.assignRankPermissions(sender.uniqueId.toString(), TownRank.MEMBER)
        Config.assignRankPermissions(playerName, TownRank.MAYOR)
        Towns.addPlayerToRank(playerName, town, TownRank.MAYOR)
        Towns.removePlayerFromRank(sender.uniqueId.toString(), town, TownRank.MAYOR)
        Messages.print(sender, "${ChatColor.GREEN}$playerName has been set as the new mayor of the town.")
        targetPlayer.sendMessage("${ChatColor.GREEN}You are now the mayor of the town.")
    }

    private fun executeSetNameCommand(sender: Player, args: Array<out String>) {
        if (args.size < 3) {
            Messages.error(sender, "Usage: /town set name <newName>")
            return
        }
        if (!sender.hasPermission(TownPermissions.TOWN_SET_NAME.permission)) {
            Messages.print(sender, TownMessages.NO_PERMISSION.message)
            return
        }
        val newName = args[2]
        if (Towns.isTownExists(newName)) {
            Messages.error(sender, "The name '$newName' is already taken.")
            return
        }
        Towns.setTownName(sender.uniqueId.toString(), newName)
        Messages.print(sender, "${ChatColor.GREEN}Town name updated to: $newName.")
    }

    private fun executeSetDescriptionCommand(sender: Player, args: Array<out String>) {
        if (args.size < 3) {
            Messages.error(sender, "Usage: /town set description <description>")
            return
        }
        if (!sender.hasPermission(TownPermissions.TOWN_SET_DESCRIPTION.permission)) {
            Messages.print(sender, TownMessages.NO_PERMISSION.message)
            return
        }
        val description = args.sliceArray(2 until args.size).joinToString(" ")
        val town = Towns.getTownByPlayer(sender.uniqueId.toString())

        if (town == null) {
            Messages.error(sender, "You are not a member of any town.")
            return
        }

        if (sender.uniqueId.toString() != Towns.getTownMayor(town)) {
            Messages.error(sender, "Only the mayor can set the town description.")
            return
        }
        Towns.setTownDescription(town, description)
        Messages.print(sender, "${ChatColor.GREEN}Town description updated successfully.")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        if (sender !is Player) {
            return emptyList()
        }
        fun filterByStart(options: List<String>, prefix: String): List<String> {
            return options.filter { it.startsWith(prefix, ignoreCase = true) }
        }

        when {
            args.size == 1 -> return filterByStart(SUB_COMMANDS.toList(), args[0])
            args.size > 1 -> {
                when (args[0].lowercase()) {
                    "set" -> {
                        if (args.size == 2) {
                            return filterByStart(SET_SUB_COMMANDS.toList(), args[1])
                        }
                    }

                    "mayor" -> {
                        if (args.size == 3) {
                            return Bukkit.getOnlinePlayers().map { it.name }
                                .filter { it.startsWith(args[2], ignoreCase = true) }
                        }
                    }

                    "rank" -> {
                        if (args.size == 2) {
                            return listOf("add", "remove").filter { it.startsWith(args[1], ignoreCase = true) }
                        } else if (args.size == 3 && (args[1] == "add" || args[1] == "remove")) {
                            return TownRank.entries.filter { it != TownRank.MAYOR && it != TownRank.MEMBER }
                                .map { it.name.lowercase() }.filter { it.startsWith(args[2], ignoreCase = true) }
                        } else if (args.size == 4 && (args[1] == "add" || args[1] == "remove")) {
                            return Bukkit.getOnlinePlayers().map { it.name }
                                .filter { it.startsWith(args[3], ignoreCase = true) }
                        }
                    }

                    "list" -> {
                        if (args.size == 2) {
                            return listOf("by")
                        } else if (args.size == 3 && args[1] == "by") {
                            return listOf("chunks", "members", "wealth").filter {
                                it.startsWith(
                                    args[args.size - 1],
                                    ignoreCase = true
                                )
                            }
                        }
                    }

                    "info",
                    "online",
                    "ranklist",
                    "reslist",
                    "spawn",
                    "gui" -> {
                        if (args.size == 2) {
                            return Towns.getAllTowns().filter { it.startsWith(args[1], ignoreCase = true) }
                        }
                    }
                    // invite [playername]
                    "invite" -> {
                        if (args.size == 2) {
                            return Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
                        }
                    }
                    // kick [name]
                    "kick" -> {
                        if (args.size == 2) {
                            val town = Towns.getTownByPlayer(sender.uniqueId.toString())
                            if (town != null) {
                                return Towns.getTownMembers(town).mapNotNull {
                                    Towns.getPlayerName(
                                        UUID.fromString(
                                            it
                                        )
                                    )
                                }
                                    .filter { it.startsWith(args[1], ignoreCase = true) }
                            }
                        }
                    }
                }
            }
        }

        return emptyList()
    }
}