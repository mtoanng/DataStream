package com.mtoanng.datastream.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.FuelPriceHistoryDto
import com.mtoanng.datastream.data.dto.GridLoadHistoryDto
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.HistoryRepository
import kotlinx.coroutines.launch

/**
 * Backs the history/chart screens.
 *
 * Call [loadFuelPrices] or [loadGridLoad] from the Fragment/Activity;
 * observe the corresponding LiveData to feed MPAndroidChart datasets.
 *
 * Time filters use ISO-8601 strings, e.g. `"2025-01-01T00:00:00Z"`.
 * Pass `null` to use the server's default window (typically last 7 days).
 */
class HistoryViewModel(private val repo: HistoryRepository) : ViewModel() {

    // ── Fuel-price history ────────────────────────────────────────────────────

    private val _fuelHistory = MutableLiveData<List<FuelPriceHistoryDto>>(emptyList())
    val fuelHistory: LiveData<List<FuelPriceHistoryDto>> = _fuelHistory

    private val _fuelLoading = MutableLiveData(false)
    val fuelLoading: LiveData<Boolean> = _fuelLoading

    private val _fuelError = MutableLiveData<String?>(null)
    val fuelError: LiveData<String?> = _fuelError

    /**
     * Fetch historical fuel prices.
     *
     * @param fuelType  "COAL" | "GAS" | "OIL" | "LNG" | null (= all)
     * @param region    Region code | null (= all)
     * @param from      ISO-8601 start, e.g. "2025-01-01T00:00:00Z"
     * @param to        ISO-8601 end — null → server default (now)
     * @param limit     Max rows (default 200)
     */
    fun loadFuelPrices(
        fuelType: String? = null,
        region: String? = null,
        from: String? = null,
        to: String? = null,
        limit: Int = 200,
    ) {
        viewModelScope.launch {
            _fuelLoading.value = true
            _fuelError.value = null
            when (val r = repo.getFuelPriceHistory(fuelType, region, from, to, limit)) {
                is NetworkResult.Success -> _fuelHistory.value = r.data
                is NetworkResult.Error   -> _fuelError.value = r.message
                NetworkResult.Loading    -> Unit
            }
            _fuelLoading.value = false
        }
    }

    // ── Grid-load history ─────────────────────────────────────────────────────

    private val _gridHistory = MutableLiveData<List<GridLoadHistoryDto>>(emptyList())
    val gridHistory: LiveData<List<GridLoadHistoryDto>> = _gridHistory

    private val _gridLoading = MutableLiveData(false)
    val gridLoading: LiveData<Boolean> = _gridLoading

    private val _gridError = MutableLiveData<String?>(null)
    val gridError: LiveData<String?> = _gridError

    /**
     * Fetch historical grid load.
     *
     * @param regionCode Region code | null (= all)
     * @param from       ISO-8601 start
     * @param to         ISO-8601 end — null → server default
     * @param limit      Max rows (default 200)
     */
    fun loadGridLoad(
        regionCode: String? = null,
        from: String? = null,
        to: String? = null,
        limit: Int = 200,
    ) {
        viewModelScope.launch {
            _gridLoading.value = true
            _gridError.value = null
            when (val r = repo.getGridLoadHistory(regionCode, from, to, limit)) {
                is NetworkResult.Success -> _gridHistory.value = r.data
                is NetworkResult.Error   -> _gridError.value = r.message
                NetworkResult.Loading    -> Unit
            }
            _gridLoading.value = false
        }
    }
}
