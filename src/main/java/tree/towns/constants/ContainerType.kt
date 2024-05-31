/**

 * - container types for claim protection.
 * - for future use when I make the claim protection listener.
 */

package tree.towns.constants

import org.bukkit.Material
enum class ContainerType(val material: Material) {
    CHEST(Material.CHEST),
    TRAPPED_CHEST(Material.TRAPPED_CHEST),
    ENDER_CHEST(Material.ENDER_CHEST),
    BARREL(Material.BARREL),
    SHULKER_BOX(Material.SHULKER_BOX),
    DISPENSER(Material.DISPENSER),
    DROPPER(Material.DROPPER),
    HOPPER(Material.HOPPER),
    FURNACE(Material.FURNACE),
    BLAST_FURNACE(Material.BLAST_FURNACE),
    SMOKER(Material.SMOKER),
    BREWING_STAND(Material.BREWING_STAND);
    companion object {
        fun getByMaterial(material: Material): ContainerType? {
            return values().find { it.material == material }
        }
    }
}
