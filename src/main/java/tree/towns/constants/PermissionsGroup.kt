package tree.towns.constants
// guess what this does retard
enum class PermissionsGroup {
    MEMBERS,
    NATION,
    ALLY,
    OUTSIDER,
    TRUSTED;
    companion object {
        val values: Array<PermissionsGroup> = entries.toTypedArray()
    }
}