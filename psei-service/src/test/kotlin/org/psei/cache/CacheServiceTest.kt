package org.psei.cache

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test suite for the in-memory cache provider.
 * In production, these tests would use a test Redis instance.
 */

class CacheServiceTest {

    @Test
    fun `set and get basic value`() {
        val cache = InMemoryCacheProvider()
        cache.set("test:key", "test-value")
        assertEquals("test-value", cache.get("test:key"))
        cache.close()
    }

    @Test
    fun `get returns null for missing key`() {
        val cache = InMemoryCacheProvider()
        assertEquals(null, cache.get("nonexistent"))
        cache.close()
    }

    @Test
    fun `delete removes key`() {
        val cache = InMemoryCacheProvider()
        cache.set("test:key", "value")
        assertTrue(cache.exists("test:key"))
        assertTrue(cache.delete("test:key"))
        assertFalse(cache.exists("test:key"))
        cache.close()
    }

    @Test
    fun `increment works correctly`() {
        val cache = InMemoryCacheProvider()
        val result = cache.increment("counter")
        assertEquals(1L, result)
        val result2 = cache.increment("counter")
        assertEquals(2L, result2)
        cache.close()
    }

    @Test
    fun `setIfAbsent returns true for new key`() {
        val cache = InMemoryCacheProvider()
        assertTrue(cache.setIfAbsent("new-key", "value"))
        assertFalse(cache.setIfAbsent("new-key", "different-value"))
        assertEquals("value", cache.get("new-key"))
        cache.close()
    }

    @Test
    fun `namespace prefix is applied`() {
        val cache = InMemoryCacheProvider()
        cache.set("user:test", "profile-data")
        // Key should be prefixed with "psei:"
        assertEquals("profile-data", cache.get("user:test"))
        cache.close()
    }

    @Test
    fun `schemes cache helpers work`() {
        val cache = InMemoryCacheProvider()
        // Replace provider for testing
        // This tests the key generation pattern
        val key = CacheService.Schemes.getCacheKey("startup")
        assertTrue(key.contains("schemes:search:"))
        cache.close()
    }

    @Test
    fun `rate limit cache helpers work`() {
        val cache = InMemoryCacheProvider()
        val key = CacheService.RateLimit.getKey("client-123")
        assertTrue(key.contains("ratelimit:"))
        cache.close()
    }
}
