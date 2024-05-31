package tree.towns.utils

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

public object Messages {
    private val PREFIX = "${ChatColor.GOLD}[Towns]"
    private val MSG = ChatColor.AQUA
    private val ERROR = ChatColor.RED
    // ip means includes prefix
    fun print(sender: Any?, s: String, ip: Boolean = true) {
        if (sender == null) {
            println(if (ip) "$PREFIX Message called without sender: $s" else s)
            return
        }

        val msg = if (ip) "$PREFIX ${MSG}${s}" else "${MSG}${s}"

        if (sender is Player) {
            sender.sendMessage(msg)
        } else {
            (sender as CommandSender).sendMessage(msg)
        }
    }

    fun broadcast(s: String, ip: Boolean = true) {
        val msg = if (ip) "$PREFIX ${MSG}${s}" else "${MSG}${s}"
        Bukkit.broadcastMessage(msg)
    }

    fun error(sender: Any?, s: String, ip: Boolean = true) {
        if (sender == null) {
            println(if (ip) "$PREFIX Message called without a sender: $s" else s)
            return
        }

        val msg = if (ip) "$PREFIX ${ERROR}${s}" else "${ERROR}${s}"

        if (sender is Player) {
            sender.sendMessage(msg)
        } else {
            (sender as CommandSender).sendMessage(msg)
        }
    }
}