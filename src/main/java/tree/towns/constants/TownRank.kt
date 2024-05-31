package tree.towns.constants

/**
 * Enum class representing the hierarchical town ranks with associated priority levels.
 * Higher priority levels indicate higher rank privileges.
 * Priorities of 2 and over can bypass toggle claimprotection off for residents.
 * @property priority The priority level associated with each town rank.
 * @link Town
 */
enum class TownRank(val priority: Int) {
    MEMBER(0),
    OFFICER(1),
    COMAYOR(2),
    ASSISTANT(3),
    MAYOR(4),
}

