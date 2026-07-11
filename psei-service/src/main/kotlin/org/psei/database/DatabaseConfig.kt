package org.psei.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.psei.config.AppConfig
import org.slf4j.LoggerFactory

/**
 * Database configuration and connection management.
 *
 * Features:
 *   - Connection pooling via HikariCP
 *   - Schema migration via Flyway
 *   - Transaction management via Exposed
 *   - Graceful connection lifecycle
 *
 * Configuration:
 *   - Min connections: 5
 *   - Max connections: 20
 *   - Connection timeout: 30s
 *   - Idle timeout: 10min
 */

private val logger = LoggerFactory.getLogger("Database")

object DatabaseConfig {

    private var dataSource: HikariDataSource? = null

    /**
     * Initialize the database connection pool and run migrations.
     */
    fun initialize() {
        try {
            // Configure HikariCP connection pool
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = AppConfig.databaseUrl
                username = AppConfig.databaseUser
                password = AppConfig.databasePassword
                driverClassName = AppConfig.databaseDriver

                // Connection pool settings
                minimumIdle = 5
                maximumPoolSize = 20
                connectionTimeout = 30000
                idleTimeout = 600000
                maxLifetime = 1800000
                connectionTestQuery = "SELECT 1"

                // Pool name for monitoring
                poolName = "PSEI-HikariCP"
            }

            dataSource = HikariDataSource(hikariConfig)

            // Connect Exposed to the datasource
            Database.connect(dataSource)

            // Run Flyway migrations
            runMigrations()

            // Verify connection
            transaction {
                logger.info("Database connection established successfully")
                logger.info("Database URL: ${AppConfig.databaseUrl}")
            }

        } catch (e: Exception) {
            logger.error("Failed to initialize database connection", e)
            throw e
        }
    }

    /**
     * Run Flyway database migrations.
     */
    private fun runMigrations() {
        try {
            val flyway = Flyway.configure()
                .dataSource(
                    AppConfig.databaseUrl,
                    AppConfig.databaseUser,
                    AppConfig.databasePassword
                )
                .locations("filesystem:src/main/resources/db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(AppConfig.isProduction)
                .load()

            val count = flyway.migrate()
            logger.info("Flyway migrations completed: ${count.migrationsExecuted} migrations applied")

        } catch (e: Exception) {
            logger.warn("Flyway migration failed: ${e.message}. Continuing with in-memory data.")
            logger.debug("Migration error details", e)
        }
    }

    /**
     * Close the connection pool gracefully.
     */
    fun shutdown() {
        dataSource?.close()
        logger.info("Database connection pool closed")
    }

    /**
     * Check if the database is reachable.
     */
    fun healthCheck(): Boolean {
        return try {
            transaction {
                org.jetbrains.exposed.sql.exec("SELECT 1")
                true
            }
        } catch (e: Exception) {
            logger.error("Database health check failed", e)
            false
        }
    }
}

/**
 * Ktor Application Lifecycle Integration.
 * Call from Application.module() to initialize the database.
 */
fun startDatabase() {
    DatabaseConfig.initialize()
}

/**
 * Call on application shutdown to close the connection pool.
 */
fun stopDatabase() {
    DatabaseConfig.shutdown()
}
