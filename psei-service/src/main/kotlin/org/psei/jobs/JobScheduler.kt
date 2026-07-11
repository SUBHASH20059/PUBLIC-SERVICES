package org.psei.jobs

import org.psei.config.AppConfig
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.Date

/**
 * Background job scheduler powered by Quartz.
 *
 * Provides scheduled execution of maintenance tasks, notifications,
 * data cleanup, and periodic sync operations. Jobs can be triggered
 * by cron expressions, intervals, or one-shot schedules.
 *
 * Features:
 *   - Cron-based scheduling
 *   - Interval-based scheduling
 *   - One-time delayed execution
 *   - Job grouping and naming for organization
 *   - Pause/resume capabilities
 *   - Graceful shutdown
 */

private val logger = LoggerFactory.getLogger("JobScheduler")

/**
 * Job group constants for organizing scheduled tasks.
 */
object JobGroups {
    const val MAINTENANCE = "maintenance"
    const val NOTIFICATIONS = "notifications"
    const val CLEANUP = "cleanup"
    const val SYNC = "sync"
    const val REPORTS = "reports"
}

/**
 * Job names for identifiable scheduled tasks.
 */
object JobNames {
    const val DB_CLEANUP = "database-cleanup"
    const val CACHE_REFRESH = "cache-refresh"
    const val NOTIFICATION_BATCH = "notification-batch"
    const val GRANT_MILESTONE_CHECK = "grant-milestone-check"
    const val PATENT_EXPIRY_CHECK = "patent-expiry-check"
    const val SESSION_CLEANUP = "session-cleanup"
    const val AUDIT_ARCHIVE = "audit-archive"
    const val SCHEME_SYNC = "scheme-sync"
    const val HEALTH_CHECK_JOB = "health-check"
}

// ─── Core Scheduler ──────────────────────────────────────────────────────────

object JobScheduler {
    private var scheduler: Scheduler? = null
    private val jobClasses = mutableMapOf<String, Class<out Job>>()

    fun initialize() {
        if (!AppConfig.jobsEnabled) {
            logger.info("Background jobs disabled via configuration")
            return
        }

        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler()

            // Register built-in jobs
            registerJob(JobNames.DB_CLEANUP, DatabaseCleanupJob::class.java)
            registerJob(JobNames.CACHE_REFRESH, CacheRefreshJob::class.java)
            registerJob(JobNames.NOTIFICATION_BATCH, NotificationBatchJob::class.java)
            registerJob(JobNames.GRANT_MILESTONE_CHECK, GrantMilestoneCheckJob::class.java)
            registerJob(JobNames.PATENT_EXPIRY_CHECK, PatentExpiryCheckJob::class.java)
            registerJob(JobNames.SESSION_CLEANUP, SessionCleanupJob::class.java)
            registerJob(JobNames.AUDIT_ARCHIVE, AuditArchiveJob::class.java)
            registerJob(JobNames.SCHEME_SYNC, SchemeSyncJob::class.java)
            registerJob(JobNames.HEALTH_CHECK_JOB, HealthCheckJob::class.java)

            // Schedule all jobs
            scheduleAllJobs()

            scheduler?.start()
            logger.info("JobScheduler initialized with ${jobClasses.size} jobs")
        } catch (e: Exception) {
            logger.error("Failed to initialize JobScheduler", e)
        }
    }

    fun shutdown() {
        try {
            scheduler?.shutdown(true)
            logger.info("JobScheduler shut down gracefully")
        } catch (e: Exception) {
            logger.error("Error during JobScheduler shutdown", e)
        }
    }

    fun pauseJob(jobName: String, group: String = JobGroups.MAINTENANCE) {
        scheduler?.pauseJob(JobKey.jobKey(jobName, group))
    }

    fun resumeJob(jobName: String, group: String = JobGroups.MAINTENANCE) {
        scheduler?.resumeJob(JobKey.jobKey(jobName, group))
    }

    // ─── Scheduling Helpers ──────────────────────────────────────────────────

    private fun scheduleAllJobs() {
        // Daily database cleanup at 3:00 AM
        scheduleCron(JobNames.DB_CLEANUP, JobGroups.CLEANUP, "0 0 3 * * ?")

        // Cache refresh every 30 minutes
        scheduleCron(JobNames.CACHE_REFRESH, JobGroups.MAINTENANCE, "0 */30 * * * ?")

        // Notification batch processing every 15 minutes
        scheduleCron(JobNames.NOTIFICATION_BATCH, JobGroups.NOTIFICATIONS, "0 */15 * * * ?")

        // Grant milestone check daily at 9:00 AM
        scheduleCron(JobNames.GRANT_MILESTONE_CHECK, JobGroups.MAINTENANCE, "0 0 9 * * ?")

        // Patent expiry check daily at 8:00 AM
        scheduleCron(JobNames.PATENT_EXPIRY_CHECK, JobGroups.MAINTENANCE, "0 0 8 * * ?")

        // Session cleanup every hour
        scheduleCron(JobNames.SESSION_CLEANUP, JobGroups.CLEANUP, "0 0 * * * ?")

        // Audit archive weekly on Sunday at 2:00 AM
        scheduleCron(JobNames.AUDIT_ARCHIVE, JobGroups.CLEANUP, "0 0 2 ? * SUN")

        // Scheme sync daily at 6:00 AM
        scheduleCron(JobNames.SCHEME_SYNC, JobGroups.SYNC, "0 0 6 * * ?")

        // Health check every 5 minutes
        scheduleCron(JobNames.HEALTH_CHECK_JOB, JobGroups.MAINTENANCE, "0 */5 * * * ?")
    }

    private fun scheduleCron(jobName: String, group: String, cronExpression: String) {
        val jobClass = jobClasses[jobName] ?: return
        try {
            val jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobName, group)
                .withDescription("PSEI scheduled task: $jobName")
                .build()

            val trigger = TriggerBuilder.newTrigger()
                .withIdentity("${jobName}-trigger", group)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build()

            scheduler?.scheduleJob(jobDetail, trigger)
            logger.info("Scheduled job: $jobName with cron: $cronExpression")
        } catch (e: Exception) {
            logger.error("Failed to schedule job: $jobName", e)
        }
    }

    // Public method to schedule a one-time job
    fun scheduleOneTime(jobName: String, group: String, jobClass: Class<out Job>, delaySeconds: Long) {
        val jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobName, group)
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("${jobName}-trigger", group)
            .startAt(Date(System.currentTimeMillis() + delaySeconds * 1000))
            .build()

        scheduler?.scheduleJob(jobDetail, trigger)
    }

    // Public method to schedule a repeating interval job
    fun scheduleInterval(jobName: String, group: String, jobClass: Class<out Job>, intervalSeconds: Int, repeatCount: Int = -1) {
        val jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobName, group)
            .build()

        val trigger = TriggerBuilder.newTrigger()
            .withIdentity("${jobName}-trigger", group)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(intervalSeconds)
                .withRepeatCount(repeatCount))
            .build()

        scheduler?.scheduleJob(jobDetail, trigger)
    }

    private fun registerJob(name: String, jobClass: Class<out Job>) {
        jobClasses[name] = jobClass
    }
}

// ─── Built-in Job Implementations ────────────────────────────────────────────

class DatabaseCleanupJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing database cleanup job")
        // TODO: Wire to actual database cleanup logic
        // - Remove expired sessions
        // - Archive old audit logs
        // - Clean up orphaned records
    }
}

class CacheRefreshJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing cache refresh job")
        // TODO: Refresh cached scheme data, user profiles, etc.
    }
}

class NotificationBatchJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing notification batch job")
        // TODO: Process queued notifications, send emails/SMS
    }
}

class GrantMilestoneCheckJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing grant milestone check job")
        // TODO: Check grant milestones, notify students of deadlines
    }
}

class PatentExpiryCheckJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing patent expiry check job")
        // TODO: Check for patents nearing expiry, notify applicants
    }
}

class SessionCleanupJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing session cleanup job")
        // TODO: Clean up expired sessions and tokens
    }
}

class AuditArchiveJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing audit archive job")
        // TODO: Archive old audit logs to cold storage
    }
}

class SchemeSyncJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Executing scheme sync job")
        // TODO: Sync government schemes from external APIs
    }
}

class HealthCheckJob : Job {
    override fun execute(context: JobExecutionContext) {
        logger.info("Health check: OK at ${LocalDateTime.now()}")
        // TODO: Report health metrics to monitoring service
    }
}

// ─── Ktor Application Lifecycle Integration ──────────────────────────────────

/**
 * Call this from Application.module() to initialize the scheduler.
 */
fun startJobScheduler() {
    JobScheduler.initialize()
}

/**
 * Call this on application shutdown to gracefully stop the scheduler.
 */
fun stopJobScheduler() {
    JobScheduler.shutdown()
}
