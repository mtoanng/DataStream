package com.mtoanng.datastream.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Verifies that [NetworkModule] hands out a delegating proxy that re-resolves the
 * underlying Retrofit instance on every method call. This guarantees that
 * Repositories built before a `baseUrl` change start hitting the new URL the moment
 * `recreate(...)` (or in tests `setRetrofitForTest(...)`) is invoked — no app
 * restart, no stale `ApiService` references.
 */
class NetworkModuleTest {

    private lateinit var server1: MockWebServer
    private lateinit var server2: MockWebServer

    @Before
    fun setUp() {
        server1 = MockWebServer().apply { start() }
        server2 = MockWebServer().apply { start() }
        NetworkModule.resetForTest()
    }

    @After
    fun tearDown() {
        NetworkModule.resetForTest()
        server1.shutdown()
        server2.shutdown()
    }

    @Test
    fun `proxy redirects subsequent calls after retrofit instance is swapped`() = runBlocking {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val retrofit1 = Retrofit.Builder()
            .baseUrl(server1.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val retrofit2 = Retrofit.Builder()
            .baseUrl(server2.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        // Initial wiring → proxy bound to server1.
        val capturedProxy = NetworkModule.setRetrofitForTest(retrofit1)
        server1.enqueue(MockResponse().setResponseCode(200).setBody(HEALTH_BODY_1))

        val first = capturedProxy.health()
        assertTrue("Expected 2xx, got $first", first.isSuccessful)
        assertEquals("server-1", first.body()?.service)

        // Caller keeps using the SAME proxy reference (simulates a Repository that was
        // captured at ViewModel construction time, before the user changed the URL).
        val proxyAfterSwap = NetworkModule.setRetrofitForTest(retrofit2)
        assertSame(
            "Proxy reference must not change — repositories already hold it",
            capturedProxy,
            proxyAfterSwap,
        )

        server2.enqueue(MockResponse().setResponseCode(200).setBody(HEALTH_BODY_2))
        val second = capturedProxy.health()
        assertTrue("Expected 2xx after swap, got $second", second.isSuccessful)
        assertEquals("server-2", second.body()?.service)

        // server1 should have only ever seen the first call; server2 only the second.
        assertEquals(1, server1.requestCount)
        assertEquals(1, server2.requestCount)
    }

    @Test
    fun `proxy throws clear error when used before any retrofit is bound`() {
        NetworkModule.resetForTest()

        // Reaching the lazy proxy directly is fine; it only throws on first method call.
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server1.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val proxy = NetworkModule.setRetrofitForTest(retrofit)
        assertNotNull(proxy)
        // Re-clear so the next assertion exercises the failure path.
        NetworkModule.resetForTest()

        var threw = false
        try {
            runBlocking { proxy.health() }
        } catch (e: IllegalStateException) {
            threw = true
            assertTrue(
                "Expected message to mention apiService(...), got: ${e.message}",
                e.message?.contains("apiService") == true,
            )
        }
        assertTrue("Expected IllegalStateException when retrofit is null", threw)
    }

    companion object {
        private const val HEALTH_BODY_1 = """
            {
              "service": "server-1",
              "timestamp": "2026-05-15T10:30:00Z",
              "db": "UP",
              "status": "UP"
            }
        """

        private const val HEALTH_BODY_2 = """
            {
              "service": "server-2",
              "timestamp": "2026-05-15T10:31:00Z",
              "db": "UP",
              "status": "UP"
            }
        """
    }
}
