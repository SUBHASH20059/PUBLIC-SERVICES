package org.psei.cache

import org.psei.config.AppConfig
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache abstraction layer supporting both Redis (production) and in-memory (development).
 *
 * Features:
 *   - TTL-based expiration
 *   - Configurable cache prefixes for namespace isolation
 *   - Graceful fallback to in-memory when Redis is unavailable
 *   - Thread-safe operations
 */
interface CacheProvider {
    fun get(key: String): String?
    fun set(key: String, value: String, ttlSeconds: Int = 3600)
    fun delete(key: String): Boolean
    fun exists(key: String): Boolean
    fun increment(key: String, amount: Int = 1): Long?
    fun setIfAbsent(key: String, value: String, ttlSeconds: Int = 3600): Boolean
    fun close()
}

/**
 * Redis-backed cache provider for production use.
 */
class RedisCacheProvider : CacheProvider {
    private val pool: JedisPool

    init {
        val config = JedisPoolConfig().apply {
            maxTotal = 20
            maxIdle = 10
            minIdle = 5
        }

        pool = if (AppConfig.redisUrl != null) {
            JedisPool(config, AppConfig.redisUrl, 2000)
        } else {
            JedisPool(
                config,
                AppConfig.redisHost,
                AppConfig.redisPort,
                2000,
                AppConfig.redisPassword
            )
        }
    }

    override fun get(key: String): String? = pool.resource.use { it.get(key) }
    override fun set(key: String, value: String, ttlSeconds: Int) = pool.resource.use {
        it.setex(key, ttlSeconds.toLong(), value)
    }
    override fun delete(key: String): Boolean = pool.resource.use { it.del(key) > 0 }
    override fun exists(key: String): Boolean = pool.resource.use { it.exists(key) }
    override fun increment(key: String, amount: Int): Long? = pool.resource.use {
        if (amount == 1) it.incr(key) else it.incrBy(key, amount.toLong())
    }
    override fun setIfAbsent(key: String, value: String, ttlSeconds: Int): Boolean = pool.resource.use {
        it.setnx(key, value) && it.expire(key, ttlSeconds.toLong()) > 0
    }
    override fun close() = pool.close()
}

/**
 * In-memory cache provider for development/testing.
 * Uses ConcurrentHashMap with TTL tracking.
 */
class InMemoryCacheProvider : CacheProvider {
    private val store = ConcurrentHashMap<String, CacheEntry>()

    data class CacheEntry(val value: String, val expiresAt: Long)

    override fun get(key: String): String? {
        val entry = store[key] ?: return null
        if (System.currentTimeMillis() > entry.expiresAt) {
            store.remove(key)
            return null
        }
        return entry.value
    }

    override fun set(key: String, value: String, ttlSeconds: Int) {
        store[key] = CacheEntry(value, System.currentTimeMillis() + ttlSeconds * 1000L)
    }

    override fun delete(key: String): Boolean = store.remove(key) != null
    override fun exists(key: String): Boolean = get(key) != null

    override fun increment(key: String, amount: Int): Long? {
        val current = get(key)?.toLongOrNull() ?: 0L
        val newValue = current + amount
        set(key, newValue.toString())
        return newValue
    }

    override fun setIfAbsent(key: String, value: String, ttlSeconds: Int): Boolean {
        if (store.containsKey(key)) return false
        set(key, value, ttlSeconds)
        return true
    }

    override fun close() = store.clear()
}

/**
 * Cache facade with namespaced key prefixes.
 * Automatically selects Redis (production) or in-memory (development).
 */
object CacheService {
    private val provider: CacheProvider = if (AppConfig.isProduction && AppConfig.redisUrl != null) {
        RedisCacheProvider()
    } else {
        InMemoryCacheProvider()
    }

    private val prefix = "psei:"

    private fun fullKey(key: String) = "$prefix$key"

    fun get(key: String): String? = provider.get(fullKey(key))
    fun set(key: String, value: String, ttlSeconds: Int = 3600) = provider.set(fullKey(key), value, ttlSeconds)
    fun delete(key: String): Boolean = provider.delete(fullKey(key))
    fun exists(key: String): Boolean = provider.exists(fullKey(key))
    fun increment(key: String, amount: Int = 1): Long? = provider.increment(fullKey(key), amount)
    fun setIfAbsent(key: String, value: String, ttlSeconds: Int = 3600): Boolean = provider.setIfAbsent(fullKey(key), value, ttlSeconds)

    // ── Domain-specific cache helpers ────────────────────────────────────────

    object Schemes {
        fun getCacheKey(query: String) = "schemes:search:$query"
        fun cacheResult(query: String, result: String, ttlSeconds: Int = 300) = set(getCacheKey(query), result, ttlSeconds)
        fun getCachedResult(query: String) = get(getCacheKey(query))
    }

    object Users {
        fun getCacheKey(userId: String) = "user:profile:$userId"
        fun cacheProfile(userId: String, profile: String, ttlSeconds: Int = 600) = set(getCacheKey(userId), profile, ttlSeconds)
        fun getCachedProfile(userId: String) = get(getCacheKey(userId))
        fun invalidateProfile(userId: String) = delete(getCacheKey(userId))
    }

    object RateLimit {
        fun getKey(clientId: String) = "ratelimit:$clientId"
        fun checkAndIncrement(clientId: String, limit: Int, periodSeconds: Int): Boolean {
            val key = getKey(clientId)
            val count = provider.increment(key) ?: return false
            if (count == 1L) provider.set(key, "1", periodSeconds)
            return count.toInt() <= limit
        }
    }

    fun shutdown() = provider.close()
}
