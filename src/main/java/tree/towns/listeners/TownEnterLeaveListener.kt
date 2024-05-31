package tree.towns.listeners

import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import tree.towns.Towns

class TownEnterLeaveListener : Listener {
    private val playerTownMap = mutableMapOf<String, String?>()

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val fromChunk = event.from.chunk
        val toChunk = event.to.chunk

        // checks if the player has moved from one chunk to another
        if (fromChunk != toChunk) {
                val fromTown = Towns.getTownAtLocation(fromChunk.world.name, event.from)
            val toTown = Towns.getTownAtLocation(toChunk.world.name, event.to)

            val previousTown = playerTownMap[player.name]

            // checks if the players town status has changed
            if (fromTown != previousTown || toTown != previousTown) {
                if (fromTown != null) {
                    player.sendTitle(
                        "${ChatColor.RED}Leaving Town",
                        "${ChatColor.GRAY}Goodbye, ${ChatColor.YELLOW}${fromTown}${ChatColor.GRAY}!",
                        10,
                        70,
                        20
                    )
                } else {
                    player.sendTitle(
                        "${ChatColor.RED}Leaving Wilderness",
                        "${ChatColor.GRAY}You are now outside of any town.",
                        10,
                        70,
                        20
                    )
                }

                if (toTown != null) {
                    player.sendTitle(
                        "${ChatColor.GREEN}Entering Town",
                        "${ChatColor.GRAY}Welcome to ${ChatColor.YELLOW}${toTown}${ChatColor.GRAY}!",
                        10,
                        70,
                        20
                    )
                } else {
                    player.sendTitle(
                        "${ChatColor.GREEN}Entering Wilderness",
                        "${ChatColor.GRAY}You are now in the wilderness.",
                        10,
                        70,
                        20
                    )
                }
                playerTownMap[player.name] = toTown
            }
        }
    }
}
