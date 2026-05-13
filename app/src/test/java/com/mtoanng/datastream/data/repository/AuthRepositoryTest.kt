package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.UserDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.TokenManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AuthRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)

        tokenManager = mock()
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun `login success persists session and returns Success`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody(LOGIN_BODY))

        val repo = AuthRepository(api, tokenManager)
        val result = repo.login("admin", "admin")

        assertTrue("Expected NetworkResult.Success but got $result", result is NetworkResult.Success)
        val data = (result as NetworkResult.Success).data
        assertEquals("eyJhbGciOiJIUzI1NiJ9.MOCK", data.accessToken)
        assertEquals(28_800_000L, data.expiresInMs)
        assertEquals("admin", data.user.username)
        verify(tokenManager).saveSession(eq("eyJhbGciOiJIUzI1NiJ9.MOCK"), eq(28_800_000L), any<UserDto>())
    }

    @Test
    fun `login 401 returns Error and does not persist`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody(ERROR_401_BODY))

        val repo = AuthRepository(api, tokenManager)
        val result = repo.login("admin", "wrong")

        assertTrue(result is NetworkResult.Error)
        val err = result as NetworkResult.Error
        assertEquals(401, err.httpCode)
        assertNotNull(err.message)
    }

    @Test
    fun `network failure returns Error`() = runBlocking {
        server.shutdown() // simulate no connectivity

        val repo = AuthRepository(api, tokenManager)
        val result = repo.login("admin", "admin")

        assertTrue("Expected Error on network failure, got $result", result is NetworkResult.Error)
    }

    companion object {
        private const val LOGIN_BODY = """
            {
              "accessToken": "eyJhbGciOiJIUzI1NiJ9.MOCK",
              "expiresInMs": 28800000,
              "user": {
                "id": 1,
                "username": "admin",
                "fullName": "Mock Administrator",
                "email": "admin@ves.local",
                "role": "ADMIN",
                "enabled": true
              }
            }
        """

        private const val ERROR_401_BODY = """
            {
              "timestamp": "2026-05-13T10:30:00Z",
              "status": 401,
              "error": "Unauthorized",
              "message": "Bad credentials",
              "path": "/api/auth/login"
            }
        """
    }
}
