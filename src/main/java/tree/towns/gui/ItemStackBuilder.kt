package tree.towns.gui
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemStackBuilder(material: Material, amount: Int = 1) {

    private val itemStack: ItemStack = ItemStack(material, amount)
    private val itemMeta: ItemMeta = itemStack.itemMeta!!

    fun displayName(name: String): ItemStackBuilder {
        itemMeta.setDisplayName(name)
        return this
    }

    fun lore(lore: List<String>): ItemStackBuilder {
        itemMeta.lore = lore
        return this
    }

    fun enchantment(enchantment: Enchantment, level: Int): ItemStackBuilder {
        itemMeta.addEnchant(enchantment, level, true)
        return this
    }

    fun flags(vararg flags: ItemFlag): ItemStackBuilder {
        itemMeta.addItemFlags(*flags)
        return this
    }

    fun build(): ItemStack {
        itemStack.itemMeta = itemMeta
        return itemStack
    }
}
