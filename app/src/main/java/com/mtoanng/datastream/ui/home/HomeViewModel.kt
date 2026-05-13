package com.mtoanng.datastream.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.SecurityScoreDto
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.repository.SecurityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: SecurityRepository,
    private val appConfig: AppConfig,
) : ViewModel() {

    private val _score = MutableLiveData<SecurityScoreDto?>()
    val score: LiveData<SecurityScoreDto?> = _score

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var pollingJob: Job? = null

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.getScore()) {
                is NetworkResult.Success -> { _score.value = r.data; _error.value = null }
                is NetworkResult.Error   -> _error.value = r.message
                NetworkResult.Loading    -> Unit
            }
            _isLoading.value = false
        }
    }

    fun startAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refresh()
                delay(appConfig.refreshIntervalSeconds * 1000L)
            }
        }
    }

    fun stopAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        stopAutoRefresh()
        super.onCleared()
    }
}
