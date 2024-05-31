package tree.towns.gui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class GuiBuilder(title: String, size: Int) {

    val inventory: Inventory = Bukkit.createInventory(null, size, title)
    private val itemActions = mutableMapOf<Int, (Player) -> Unit>()
    var allowItemClick: Boolean = true

    fun setItem(slot: Int, item: ItemStack, onClick: ((Player) -> Unit)? = null) {
        inventory.setItem(slot, item)
        if (onClick != null) {
            itemActions[slot] = onClick
        }
    }

    fun open(player: Player) {
        player.openInventory(inventory)
    }

    fun handleClick(event: InventoryClickEvent) {
        if (!allowItemClick) {
            event.isCancelled = true
        }

        val slot = event.slot
        val player = event.whoClicked as? Player ?: return
        val action = itemActions[slot] ?: return

        action.invoke(player)

        if (!allowItemClick) {
            event.isCancelled = true
        }
    }
}
