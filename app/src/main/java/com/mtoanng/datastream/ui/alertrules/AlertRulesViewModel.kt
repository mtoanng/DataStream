// ─────────────────────────────────────────────────────────────────────────────
// AlertRulesViewModel.kt
// ─────────────────────────────────────────────────────────────────────────────
package com.mtoanng.datastream.ui.alertrules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.AlertRuleDto
import com.mtoanng.datastream.data.dto.AlertRulePatchRequest
import com.mtoanng.datastream.data.dto.AlertRuleRequest
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.AlertRuleRepository
import kotlinx.coroutines.launch

/**
 * Backs the Alert-Rules management screen (list + CRUD actions).
 *
 * UI binds to [items] for the RecyclerView and observes [event] for
 * one-shot feedback (snackbar, navigation, etc.).
 */
class AlertRulesViewModel(private val repo: AlertRuleRepository) : ViewModel() {

    // ── State ────────────────────────────────────────────────────────────────

    private val _items = MutableLiveData<List<AlertRuleDto>>(emptyList())
    val items: LiveData<List<AlertRuleDto>> = _items

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /** One-shot UI events: success confirmation or error message. */
    private val _event = MutableLiveData<UiEvent?>(null)
    val event: LiveData<UiEvent?> = _event

    // ── Read ─────────────────────────────────────────────────────────────────

    fun loadRules() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.getRules()) {
                is NetworkResult.Success -> _items.value = r.data
                is NetworkResult.Error   -> _event.value = UiEvent.Error(r.message ?: "Load failed")
                NetworkResult.Loading    -> Unit
            }
            _isLoading.value = false
        }
    }

    // ── Write ────────────────────────────────────────────────────────────────

    fun createRule(request: AlertRuleRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.createRule(request)) {
                is NetworkResult.Success -> {
                    _items.value = _items.value.orEmpty() + r.data
                    _event.value = UiEvent.Success("Rule \"${r.data.name}\" created")
                }
                is NetworkResult.Error -> _event.value = UiEvent.Error(r.message ?: "Create failed")
                NetworkResult.Loading  -> Unit
            }
            _isLoading.value = false
        }
    }

    fun updateRule(id: Long, request: AlertRuleRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.updateRule(id, request)) {
                is NetworkResult.Success -> {
                    replaceInList(r.data)
                    _event.value = UiEvent.Success("Rule updated")
                }
                is NetworkResult.Error -> _event.value = UiEvent.Error(r.message ?: "Update failed")
                NetworkResult.Loading  -> Unit
            }
            _isLoading.value = false
        }
    }

    /** Toggle enabled/disabled without re-sending the full form. */
    fun toggleEnabled(rule: AlertRuleDto) {
        viewModelScope.launch {
            when (val r = repo.toggleEnabled(rule.id, !rule.enabled)) {
                is NetworkResult.Success -> {
                    replaceInList(r.data)
                    val state = if (r.data.enabled) "enabled" else "disabled"
                    _event.value = UiEvent.Success("\"${r.data.name}\" $state")
                }
                is NetworkResult.Error -> _event.value = UiEvent.Error(r.message ?: "Toggle failed")
                NetworkResult.Loading  -> Unit
            }
        }
    }

    fun patchRule(id: Long, patch: AlertRulePatchRequest) {
        viewModelScope.launch {
            when (val r = repo.patchRule(id, patch)) {
                is NetworkResult.Success -> replaceInList(r.data)
                is NetworkResult.Error   -> _event.value = UiEvent.Error(r.message ?: "Patch failed")
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repo.deleteRule(id)) {
                is NetworkResult.Success -> {
                    _items.value = _items.value.orEmpty().filter { it.id != id }
                    _event.value = UiEvent.Success("Rule deleted")
                }
                is NetworkResult.Error -> _event.value = UiEvent.Error(r.message ?: "Delete failed")
                NetworkResult.Loading  -> Unit
            }
            _isLoading.value = false
        }
    }

    fun consumeEvent() { _event.value = null }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun replaceInList(updated: AlertRuleDto) {
        _items.value = _items.value.orEmpty().map { if (it.id == updated.id) updated else it }
    }

    sealed interface UiEvent {
        data class Success(val message: String) : UiEvent
        data class Error(val message: String) : UiEvent
    }
}
