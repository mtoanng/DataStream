package com.mtoanng.datastream.ui.paged

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.AlertDto
import com.mtoanng.datastream.data.dto.PagedResponse
import com.mtoanng.datastream.data.dto.RecommendationDto
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.PagedRepository
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// PagedAlertsViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Supports infinite-scroll (append) and fresh-load (reset) for the filtered
 * Alerts list.
 *
 * Usage pattern in Fragment:
 * ```kotlin
 * vm.setFilters(severity = "CRITICAL")   // resets to page 0 automatically
 * vm.loadNextPage()                       // call from RecyclerView.addOnScrollListener
 * vm.items.observe(viewLifecycleOwner) { adapter.submitList(it) }
 * ```
 */
class PagedAlertsViewModel(private val repo: PagedRepository) : ViewModel() {

    // ── Pagination state ──────────────────────────────────────────────────────

    private var currentPage = 0
    private var totalPages  = 1
    private val pageSize    = 20

    val hasNextPage get() = currentPage < totalPages - 1

    // ── Filters ───────────────────────────────────────────────────────────────

    var severity:   String? = null; private set
    var metricType: String? = null; private set
    var region:     String? = null; private set
    var from:       String? = null; private set
    var to:         String? = null; private set

    /** Apply new filters and reset to page 0. */
    fun setFilters(
        severity:   String? = null,
        metricType: String? = null,
        region:     String? = null,
        from:       String? = null,
        to:         String? = null,
    ) {
        this.severity   = severity
        this.metricType = metricType
        this.region     = region
        this.from       = from
        this.to         = to
        reset()
    }

    // ── Exposed LiveData ──────────────────────────────────────────────────────

    /** Accumulated rows across all loaded pages. */
    private val _items = MutableLiveData<List<AlertDto>>(emptyList())
    val items: LiveData<List<AlertDto>> = _items

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    /** Paging metadata from last successful response. */
    private val _meta = MutableLiveData<PagedResponse<AlertDto>?>(null)
    val meta: LiveData<PagedResponse<AlertDto>?> = _meta

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Reload from page 0, clearing existing rows. */
    fun reset() {
        currentPage = 0
        totalPages  = 1
        _items.value = emptyList()
        loadNextPage()
    }

    /** Append the next page. No-op if already on the last page or loading. */
    fun loadNextPage() {
        if (_isLoading.value == true) return
        if (currentPage > 0 && !hasNextPage) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val r = repo.getAlertsPaged(
                page       = currentPage,
                size       = pageSize,
                severity   = severity,
                metricType = metricType,
                region     = region,
                from       = from,
                to         = to,
            )) {
                is NetworkResult.Success -> {
                    _meta.value  = r.data
                    totalPages   = r.data.totalPages
                    _items.value = _items.value.orEmpty() + r.data.content
                    currentPage++
                }
                is NetworkResult.Error -> _error.value = r.message
                NetworkResult.Loading  -> Unit
            }
            _isLoading.value = false
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PagedRecommendationsViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Paginated + filtered Recommendations list.
 *
 * Same append/reset pattern as [PagedAlertsViewModel].
 */
class PagedRecommendationsViewModel(private val repo: PagedRepository) : ViewModel() {

    // ── Pagination state ──────────────────────────────────────────────────────

    private var currentPage = 0
    private var totalPages  = 1
    private val pageSize    = 20

    val hasNextPage get() = currentPage < totalPages - 1

    // ── Filters ───────────────────────────────────────────────────────────────

    var pillar:   Int?    = null; private set
    var severity: String? = null; private set
    var status:   String? = null; private set

    /** Apply new filters and reset to page 0. */
    fun setFilters(
        pillar:   Int?    = null,
        severity: String? = null,
        status:   String? = null,
    ) {
        this.pillar   = pillar
        this.severity = severity
        this.status   = status
        reset()
    }

    // ── Exposed LiveData ──────────────────────────────────────────────────────

    private val _items = MutableLiveData<List<RecommendationDto>>(emptyList())
    val items: LiveData<List<RecommendationDto>> = _items

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _meta = MutableLiveData<PagedResponse<RecommendationDto>?>(null)
    val meta: LiveData<PagedResponse<RecommendationDto>?> = _meta

    // ── Actions ───────────────────────────────────────────────────────────────

    fun reset() {
        currentPage = 0
        totalPages  = 1
        _items.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value == true) return
        if (currentPage > 0 && !hasNextPage) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val r = repo.getRecommendationsPaged(
                page     = currentPage,
                size     = pageSize,
                pillar   = pillar,
                severity = severity,
                status   = status,
            )) {
                is NetworkResult.Success -> {
                    _meta.value  = r.data
                    totalPages   = r.data.totalPages
                    _items.value = _items.value.orEmpty() + r.data.content
                    currentPage++
                }
                is NetworkResult.Error -> _error.value = r.message
                NetworkResult.Loading  -> Unit
            }
            _isLoading.value = false
        }
    }
}
