package com.mtoanng.datastream.ui.pillars

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.Pillar1SupplySecurityDto
import com.mtoanng.datastream.data.dto.Pillar2MarketResilienceDto
import com.mtoanng.datastream.data.dto.Pillar3GridReliabilityDto
import com.mtoanng.datastream.data.dto.Pillar4EnergyTransitionDto
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.PillarRepository
import kotlinx.coroutines.launch

class PillarsViewModel(private val repo: PillarRepository) : ViewModel() {

    val pillar1 = MutableLiveData<List<Pillar1SupplySecurityDto>>()
    val pillar2 = MutableLiveData<List<Pillar2MarketResilienceDto>>()
    val pillar3 = MutableLiveData<List<Pillar3GridReliabilityDto>>()
    val pillar4 = MutableLiveData<List<Pillar4EnergyTransitionDto>>()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            handle(repo.pillar1()) { pillar1.value = it }
            handle(repo.pillar2()) { pillar2.value = it }
            handle(repo.pillar3()) { pillar3.value = it }
            handle(repo.pillar4()) { pillar4.value = it }
            _isLoading.value = false
        }
    }

    private fun <T> handle(r: NetworkResult<T>, onSuccess: (T) -> Unit) {
        when (r) {
            is NetworkResult.Success -> onSuccess(r.data)
            is NetworkResult.Error   -> _error.value = r.message
            NetworkResult.Loading    -> Unit
        }
    }
}
