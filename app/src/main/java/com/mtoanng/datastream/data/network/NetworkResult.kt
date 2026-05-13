package com.mtoanng.datastream.data.network

/**
 * Lightweight result envelope for repository calls. Keeps ViewModels free of try/catch
 * blocks and lets the UI map states 1:1 to spinner / list / snackbar.
 */
sealed class NetworkResult<out T> {
    data object Loading : NetworkResult<Nothing>()
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val httpCode: Int? = null, val message: String) : NetworkResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    fun dataOrNull(): T? = (this as? Success)?.data
}
