package tree.towns.database

import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException
object SQLiteTables {
    private val logger = LoggerFactory.getLogger(SQLiteTables::class.java)

    private fun initializeBalancesTable(connection: Connection) {
        val createBalancesTableQuery =
            "CREATE TABLE IF NOT EXISTS balances(" +
                    "town_name TEXT PRIMARY KEY," +
                    "balance DOUBLE DEFAULT 0)"
        connection.prepareStatement(createBalancesTableQuery).use { statement ->
            statement.executeUpdate()
        }

        logger.info("Balances table created successfully.")
    }
    private fun initializeRanksTable(connection: Connection) {
        val createRanksTableQuery =
            "CREATE TABLE IF NOT EXISTS town_ranks(" +
                    "town_name TEXT," +
                    "player_name TEXT," +
                    "rank TEXT," +
                    "priority INTEGER," +
                    "PRIMARY KEY (town_name, player_name, rank))"
        connection.prepareStatement(createRanksTableQuery).use { statement ->
            statement.executeUpdate()
        }

        logger.info("Ranks table created successfully.")
    }

    private fun initializeTownsTable(connection: Connection) {
        val createTownsTableQuery =
            "CREATE TABLE IF NOT EXISTS towns(" +
                    "name TEXT PRIMARY KEY," +
                    "mayor TEXT," +
                    "members TEXT," +
                    "max_chunks INT," +
                    "spawn_x DOUBLE," +
                    "spawn_y DOUBLE," +
                    "spawn_z DOUBLE," +
                    "spawn_yaw FLOAT," +
                    "spawn_pitch FLOAT)"
        connection.prepareStatement(createTownsTableQuery).use { statement ->
            statement.executeUpdate()
        }

        logger.info("Towns table created successfully.")
    }

    private fun initializePendingInvitationsTable(connection: Connection) {
        val createPendingInvitationsTableQuery =
            "CREATE TABLE IF NOT EXISTS pending_invitations(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "invited_player_name TEXT NOT NULL," +
                    "sender_name TEXT NOT NULL," +
                    "town_name TEXT NOT NULL," +
                    "timestamp INTEGER NOT NULL DEFAULT 0)"
        connection.prepareStatement(createPendingInvitationsTableQuery).use { statement ->
            statement.executeUpdate()
        }

        logger.info("Pending Invitations table created successfully.")
    }
    private fun initializeChunkClaimedTable(connection: Connection) {
        val createChunkClaimedTableQuery =
            "CREATE TABLE IF NOT EXISTS town_chunks(" +
                    "town_name TEXT," +
                    "world_name TEXT," +
                    "x INTEGER," +
                    "z INTEGER," +
                    "unclaimable BOOLEAN DEFAULT 0," +
                    "PRIMARY KEY (town_name, world_name, x, z))"
        connection.prepareStatement(createChunkClaimedTableQuery).use { statement ->
            statement.executeUpdate()
        }

        logger.info("Chunk Claimed table created successfully.")
    }
    private fun initializeTownSetsTable(connection: Connection) {
        val createTownSetsTableQuery =
            "CREATE TABLE IF NOT EXISTS town_sets(" +
                    "town_name TEXT PRIMARY KEY," +
                    "description TEXT)"

        connection.prepareStatement(createTownSetsTableQuery).use { statement ->
            statement.executeUpdate()
        }

        logger.info("Town Sets table created successfully.")
    }
    fun initialize() {
        try {
            DatabaseManager.getConnection().use { connection ->
                this.initializeRanksTable(connection)
                this.initializeTownsTable(connection)
                this.initializeTownSetsTable(connection)
                this.initializeChunkClaimedTable(connection)
                this.initializePendingInvitationsTable(connection)
                this.initializeBalancesTable(connection)
            }
        } catch (e: SQLException) {
            logger.error("Error initializing database", e)
            throw RuntimeException("Error initializing database", e)
        }
    }
}
