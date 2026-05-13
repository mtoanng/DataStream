package com.mtoanng.datastream.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val appConfig: AppConfig,
    private val rebuildApi: () -> ApiService,
) : ViewModel() {

    sealed class State {
        data object Idle : State()
        data object Loading : State()
        data object Success : State()
        data class Failure(val message: String) : State()
    }

    private val _state = MutableLiveData<State>(State.Idle)
    val state: LiveData<State> = _state

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = State.Failure("Username and password are required")
            return
        }
        _state.value = State.Loading
        viewModelScope.launch {
            when (val r = authRepository.login(username.trim(), password)) {
                is NetworkResult.Success -> _state.value = State.Success
                is NetworkResult.Error   -> _state.value = State.Failure(r.message)
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun isLoggedIn(): Boolean = authRepository.isLoggedIn()

    fun currentBaseUrl(): String = appConfig.baseUrl

    fun updateBaseUrl(url: String) {
        appConfig.baseUrl = url
        rebuildApi()
    }
}
