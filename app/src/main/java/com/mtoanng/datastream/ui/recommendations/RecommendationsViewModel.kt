package com.mtoanng.datastream.ui.recommendations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.RecommendationRepository
import kotlinx.coroutines.launch

class RecommendationsViewModel(private val repo: RecommendationRepository) : ViewModel() {

    private val raw = MutableLiveData<List<RecommendationDto>>(emptyList())
    private val _filter = MutableLiveData(0) // 0=ALL, 1..4 = pillar n
    val filter: LiveData<Int> = _filter

    private val _items = MutableLiveData<List<RecommendationDto>>(emptyList())
    val items: LiveData<List<RecommendationDto>> = _items

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _ackResult = MutableLiveData<String?>()
    val ackResult: LiveData<String?> = _ackResult

    fun setFilter(filter: Int) {
        _filter.value = filter
        applyFilter()
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.list(limit = 100)) {
                is NetworkResult.Success -> { raw.value = r.data; _error.value = null; applyFilter() }
                is NetworkResult.Error   -> _error.value = r.message
                NetworkResult.Loading    -> Unit
            }
            _isLoading.value = false
        }
    }

    fun acknowledge(id: Long, note: String?) {
        viewModelScope.launch {
            when (val r = repo.acknowledge(id, "ACKNOWLEDGED", note)) {
                is NetworkResult.Success -> {
                    _ackResult.value = "Acknowledged #${r.data.id}"
                    raw.value = raw.value.orEmpty().filter { it.id != id }
                    applyFilter()
                }
                is NetworkResult.Error   -> _ackResult.value = "Failed: ${r.message}"
                NetworkResult.Loading    -> Unit
            }
        }
    }

    private fun applyFilter() {
        val pillar = _filter.value ?: 0
        val all = raw.value.orEmpty()
        _items.value = if (pillar == 0) all else all.filter { it.pillar == pillar }
    }
}
