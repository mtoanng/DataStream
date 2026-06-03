package com.mtoanng.datastream.data.repository

import com.mtoanng.datastream.data.dto.ErrorResponse
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.network.NetworkModule
import com.squareup.moshi.JsonDataException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException

/**
 * Shared `Response<T>` -> `NetworkResult<T>` bridge. Reads the Spring error envelope
 * when the call fails so error snackbars show the backend's actual message.
 */
internal suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
                ?: return NetworkResult.Error(response.code(), "Empty response body")
            NetworkResult.Success(body)
        } else {
            val raw = response.errorBody()?.string()
            val parsed = raw?.let { runCatching {
                NetworkModule.moshi.adapter(ErrorResponse::class.java).fromJson(it)
            }.getOrNull() }
            val msg = parsed?.message ?: response.message().ifBlank { "HTTP ${response.code()}" }
            Timber.w("API error %d: %s", response.code(), msg)
            NetworkResult.Error(response.code(), msg)
        }
    } catch (e: ConnectException) {
        val msg = if (e.message?.contains("10.0.2.2") == true) {
            "Cannot connect to host (10.0.2.2). Is the mock/backend server running on port 8090?"
        } else {
            "Connection refused: ${e.localizedMessage ?: "no connection"}"
        }
        Timber.e(e, "Connection failure")
        NetworkResult.Error(message = msg)
    } catch (e: SocketTimeoutException) {
        Timber.e(e, "Network timeout")
        NetworkResult.Error(message = "Server timed out. Check your connection or VPN.")
    } catch (e: IOException) {
        Timber.e(e, "Network IO failure")
        NetworkResult.Error(message = "Network error: ${e.localizedMessage ?: "no connection"}")
    } catch (e: JsonDataException) {
        Timber.e(e, "Invalid JSON")
        NetworkResult.Error(message = "Invalid response from server")
    } catch (e: Exception) {
        Timber.e(e, "Unexpected error")
        NetworkResult.Error(message = e.localizedMessage ?: "Unexpected error")
    }
}
