package com.mtoanng.datastream.ui.alerts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.AlertRepository
import kotlinx.coroutines.launch

class AlertsViewModel(private val repo: AlertRepository) : ViewModel() {

    private val raw = MutableLiveData<List<AlertDto>>(emptyList())
    private val _filter = MutableLiveData("ALL")

    val filter: LiveData<String> = _filter

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _items = MutableLiveData<List<AlertDto>>(emptyList())
    val items: LiveData<List<AlertDto>> = _items

    fun setFilter(severity: String) {
        _filter.value = severity
        applyFilter()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.getActive(limit = 100)) {
                is NetworkResult.Success -> { raw.value = r.data; _error.value = null; applyFilter() }
                is NetworkResult.Error   -> _error.value = r.message
                NetworkResult.Loading    -> Unit
            }
            _isLoading.value = false
        }
    }

    private fun applyFilter() {
        val f = _filter.value ?: "ALL"
        val all = raw.value.orEmpty()
        _items.value = if (f == "ALL") all else all.filter { it.severity.equals(f, ignoreCase = true) }
    }
}
