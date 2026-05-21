package com.mtoanng.datastream.ui.export

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.ExportRequest
import com.mtoanng.datastream.data.dto.ExportStatusResponse
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.repository.ExportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * Drives the three-step async export flow from the UI:
 *
 * 1. User taps "Export" → [startExport]
 * 2. ViewModel polls the server every 2 s → [state] moves PENDING → PROCESSING → READY
 * 3. On READY, file is downloaded automatically → [downloadedFile] emits the saved [File]
 *
 * The Fragment only needs to observe [state], [downloadedFile], and [error].
 *
 * Example usage in Fragment:
 * ```kotlin
 * exportVm.startExport(ExportRequest(dataType = "FUEL_PRICES", format = "CSV"))
 * exportVm.state.observe(viewLifecycleOwner) { state ->
 *     progressBar.isVisible = state in listOf(ExportState.PENDING, ExportState.PROCESSING)
 *     exportButton.isEnabled = state == ExportState.IDLE
 * }
 * exportVm.downloadedFile.observe(viewLifecycleOwner) { file ->
 *     if (file != null) openFile(file)
 * }
 * ```
 */
class ExportViewModel(private val repo: ExportRepository) : ViewModel() {

    // ── State machine ─────────────────────────────────────────────────────────

    enum class ExportState { IDLE, PENDING, PROCESSING, READY, FAILED }

    private val _state = MutableLiveData(ExportState.IDLE)
    val state: LiveData<ExportState> = _state

    /** Non-null only when [state] == READY and download succeeded. */
    private val _downloadedFile = MutableLiveData<File?>(null)
    val downloadedFile: LiveData<File?> = _downloadedFile

    /** Latest raw status from the server (useful for progress % if backend adds it). */
    private val _statusDetail = MutableLiveData<ExportStatusResponse?>(null)
    val statusDetail: LiveData<ExportStatusResponse?> = _statusDetail

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Submit an export job and start polling.
     * No-op if an export is already in progress.
     *
     * @param request   Describes what to export (dataType, format, filters, …).
     */
    fun startExport(request: ExportRequest) {
        if (_state.value != ExportState.IDLE) return

        viewModelScope.launch {
            _error.value = null
            _downloadedFile.value = null
            _state.value = ExportState.PENDING

            // Step 1 — submit the job
            val submitResult = repo.requestExport(request)
            if (submitResult is NetworkResult.Error) {
                _error.value = submitResult.message
                _state.value = ExportState.FAILED
                return@launch
            }

            val jobId  = (submitResult as NetworkResult.Success).data.jobId
            val format = submitResult.data.format

            // Step 2 — poll until READY or FAILED (max 60 attempts = ~2 min)
            var attempts = 0
            while (attempts < 60) {
                delay(POLL_INTERVAL_MS)
                val pollResult = repo.pollStatus(jobId)

                if (pollResult is NetworkResult.Error) {
                    _error.value = pollResult.message
                    _state.value = ExportState.FAILED
                    return@launch
                }

                val status = (pollResult as NetworkResult.Success).data
                _statusDetail.value = status

                when (status.status) {
                    "READY" -> {
                        _state.value = ExportState.READY
                        downloadFile(jobId, format)
                        return@launch
                    }
                    "FAILED" -> {
                        _error.value = status.errorMessage ?: "Export failed server-side"
                        _state.value = ExportState.FAILED
                        return@launch
                    }
                    "PROCESSING" -> _state.value = ExportState.PROCESSING
                    // "PENDING" → keep waiting
                }
                attempts++
            }

            // Timed out
            _error.value = "Export timed out. The file may still be ready later."
            _state.value = ExportState.FAILED
        }
    }

    /** Reset back to IDLE so the user can trigger a new export. */
    fun reset() {
        _state.value = ExportState.IDLE
        _downloadedFile.value = null
        _statusDetail.value = null
        _error.value = null
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private suspend fun downloadFile(jobId: String, format: String) {
        when (val r = repo.downloadToFile(jobId, format)) {
            is NetworkResult.Success -> _downloadedFile.value = r.data
            is NetworkResult.Error   -> {
                _error.value = r.message
                _state.value = ExportState.FAILED
            }
            NetworkResult.Loading -> Unit
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 2_000L
    }
}
