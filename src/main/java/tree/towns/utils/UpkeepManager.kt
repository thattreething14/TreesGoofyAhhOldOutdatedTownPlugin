package tree.towns.utils

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import tree.towns.Config
import tree.towns.Towns
import tree.towns.Towns.plugin
import tree.towns.constants.TownRank
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import java.util.*

object UpkeepManager {
    private val timer = Timer()

    fun scheduleUpkeep() {
        if (!Config.enableUpkeep) {
            return
        }

        val timeString = Config.upkeepTime
        val timeZoneString = Config.upkeepTimeZone
        val amount = Config.upkeepAmount

        val zoneId = ZoneId.of(timeZoneString)

        val upkeepTime = try {
            LocalTime.parse(timeString)
        } catch (e: DateTimeParseException) {
            plugin.logger.warning("Failed to parse upkeep time. Using default time (12:00).")
            LocalTime.parse("12:00")
        }

        val now = Calendar.getInstance(TimeZone.getTimeZone(zoneId))
        val nextUpkeepDateTime = Calendar.getInstance(TimeZone.getTimeZone(zoneId))
        nextUpkeepDateTime.set(Calendar.HOUR_OF_DAY, upkeepTime.hour)
        nextUpkeepDateTime.set(Calendar.MINUTE, upkeepTime.minute)

        // If the next upkeep time is before the current time, schedule it for the next day
        if (nextUpkeepDateTime.before(now)) {
            nextUpkeepDateTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val delayMillis = nextUpkeepDateTime.timeInMillis - now.timeInMillis

        plugin.logger.info("Scheduled upkeep task. Time: ${nextUpkeepDateTime.time}, Delay (milliseconds): $delayMillis")

        timer.schedule(object : TimerTask() {
            override fun run() {
                plugin.logger.info("Executing upkeep task at ${Calendar.getInstance(TimeZone.getTimeZone(zoneId)).time}")
                performUpkeep(amount)
            }
        }, delayMillis, 24 * 60 * 60 * 1000) // 24 hours period for daily tasks
    }

    private fun performUpkeep(amount: Double) {
        Bukkit.broadcastMessage("${ChatColor.GREEN}The daily upkeep has commenced gl \uD83D\uDE4F.")
        for (townName in Towns.getAllTowns()) {
            val townBalance = Towns.getTownBalance(townName)
            if (townBalance < amount) {
                Towns.deleteTown(townName)
                Messages.broadcast("${ChatColor.RED}Town $townName has been deleted due to insufficient funds during upkeep.")
            } else {
                Towns.updateTownBalance(townName, townBalance - amount)
            }
        }
    }
}
