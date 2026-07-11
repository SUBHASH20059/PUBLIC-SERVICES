package org.psei.config

import io.github.cdimascio.dotenv.dotenv

/**
 * Centralized application configuration manager.
 *
 * Loads configuration from:
 *   1. Environment variables (production)
 *   2. .env file (development)
 *   3. Default fallback values
 *
 * All secrets and sensitive values are accessed through this object
 * to ensure consistent management and enable future migration to
 * dedicated secrets managers (Doppler, Infisical, AWS Secrets Manager).
 */
object AppConfig {

    private val dotenv = runCatching {
        dotenv {
            directory = System.getProperty("user.dir")
            ignoreIfMissing = true
        }
    }.getOrNull()

    /**
     * Resolve a configuration value from environment, .env file, or default.
     */
    private fun env(key: String, default: String? = null): String? =
        System.getenv(key) ?: dotenv?.get(key) ?: default

    // ── Application Identity ───────────────────────────────────────────────────
    val appName: String = env("APP_NAME", "PSEI Authority") ?: "PSEI Authority"
    val appVersion: String = env("APP_VERSION", "3.0.0") ?: "3.0.0"
    val appEnv: String = env("APP_ENV", "development") ?: "development"
    val port: Int = env("PORT", "8080")?.toIntOrNull() ?: 8080
    val host: String = env("HOST", "0.0.0.0") ?: "0.0.0.0"

    // ── Database ───────────────────────────────────────────────────────────────
    val databaseUrl: String = env("DATABASE_URL", "jdbc:postgresql://localhost:5432/psei") ?: "jdbc:postgresql://localhost:5432/psei"
    val databaseUser: String = env("DB_USER", "psei_user") ?: "psei_user"
    val databasePassword: String = env("DB_PASSWORD", "change-me") ?: "change-me"
    val databaseDriver: String = "org.postgresql.Driver"

    // ── JWT Authentication ────────────────────────────────────────────────────
    val jwtSecret: String = env("JWT_SECRET", "psei-dev-secret-change-in-prod") ?: "psei-dev-secret-change-in-prod"
    val jwtIssuer: String = env("JWT_ISSUER", "psei-authority") ?: "psei-authority"
    val jwtAudience: String = env("JWT_AUDIENCE", "psei-clients") ?: "psei-clients"
    val jwtExpiryMs: Long = env("JWT_EXPIRY_MS", "86400000")?.toLongOrNull() ?: 86400000L

    // ── CORS ───────────────────────────────────────────────────────────────────
    val allowedOrigins: List<String> = env("ALLOWED_ORIGINS", "http://localhost:3000")
        ?.split(",")?.map { it.trim() } ?: listOf("http://localhost:3000")

    // ── Rate Limiting ──────────────────────────────────────────────────────────
    val rateLimitRequests: Int = env("RATE_LIMIT_REQUESTS", "100")?.toIntOrNull() ?: 100
    val rateLimitPeriodSeconds: Int = env("RATE_LIMIT_PERIOD", "60")?.toIntOrNull() ?: 60

    // ── File Storage ──────────────────────────────────────────────────────────
    val storageProvider: String = env("STORAGE_PROVIDER", "local") ?: "local"
    val localStoragePath: String = env("STORAGE_PATH", "./uploads") ?: "./uploads"
    val s3BucketName: String? = env("S3_BUCKET_NAME")
    val s3Region: String? = env("S3_REGION")
    val cloudinaryCloudName: String? = env("CLOUDINARY_CLOUD_NAME")
    val cloudinaryApiKey: String? = env("CLOUDINARY_API_KEY")
    val cloudinaryApiSecret: String? = env("CLOUDINARY_API_SECRET")
    val maxFileSizeMb: Long = env("MAX_FILE_SIZE_MB", "10")?.toLongOrNull() ?: 10L
    val allowedFileTypes: List<String> = env("ALLOWED_FILE_TYPES", "pdf,jpg,png,docx,xlsx")
        ?.split(",")?.map { it.trim() } ?: listOf("pdf", "jpg", "png", "docx", "xlsx")

    // ── Caching (Redis) ───────────────────────────────────────────────────────
    val redisUrl: String? = env("REDIS_URL")
    val redisHost: String = env("REDIS_HOST", "localhost") ?: "localhost"
    val redisPort: Int = env("REDIS_PORT", "6379")?.toIntOrNull() ?: 6379
    val redisPassword: String? = env("REDIS_PASSWORD")

    // ── Background Jobs ───────────────────────────────────────────────────────
    val jobsEnabled: Boolean = env("JOBS_ENABLED", "true")?.toBoolean()
    val jobsWorkerCount: Int = env("JOBS_WORKER_COUNT", "4")?.toIntOrNull() ?: 4

    // ── Logging ────────────────────────────────────────────────────────────────
    val logLevel: String = env("LOG_LEVEL", "INFO") ?: "INFO"
    val logFormat: String = env("LOG_FORMAT", "json") ?: "json"

    // ── Monitoring ────────────────────────────────────────────────────────────
    val sentryDsn: String? = env("SENTRY_DSN")
    val grafanaEndpoint: String? = env("GRAFANA_ENDPOINT")

    // ── Email / Notifications ─────────────────────────────────────────────────
    val smtpHost: String? = env("SMTP_HOST")
    val smtpPort: Int = env("SMTP_PORT", "587")?.toIntOrNull() ?: 587
    val smtpUser: String? = env("SMTP_USER")
    val smtpPassword: String? = env("SMTP_PASSWORD")

    // ── External Services ──────────────────────────────────────────────────────
    val openaiApiKey: String? = env("OPENAI_API_KEY")
    val whatsappApiKey: String? = env("WHATSAPP_API_KEY")

    // ── Health ─────────────────────────────────────────────────────────────────
    val isProduction: Boolean = appEnv == "production"
    val isDevelopment: Boolean = appEnv == "development"
    val isTesting: Boolean = appEnv == "test"
}
