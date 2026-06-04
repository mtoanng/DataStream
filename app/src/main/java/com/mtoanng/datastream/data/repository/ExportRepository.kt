package com.mtoanng.datastream.data.repository

import android.content.Context
import com.mtoanng.datastream.data.dto.ExportJobResponse
import com.mtoanng.datastream.data.dto.ExportRequest
import com.mtoanng.datastream.data.dto.ExportStatusResponse
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Drives the three-step async export flow:
 *
 * 1. [requestExport]  → server returns a 'jobId'
 * 2. [pollStatus]     → call repeatedly until `status == READY`
 * 3. [downloadToFile] → stream binary to disk
 *
 * All steps run on [Dispatchers.IO]. The ViewModel should launch
 * these in a coroutine scope tied to the user's session.
 *
 * @param context   Needed to resolve the app's cache/files directory.
 */
class ExportRepository(
    private val api: ApiService,
    private val context: Context,
) {

    /**
     * Submit an export job.
     *
     * Example — export last 30 days of fuel prices as CSV:
     * ```kotlin
     * exportRepo.requestExport(
     *     ExportRequest(
     *         dataType = "FUEL_PRICES",
     *         format   = "CSV",
     *         from     = "2025-04-01T00:00:00Z",
     *         filters  = mapOf("fuelType" to "GAS"),
     *     )
     * )
     * ```
     */
    suspend fun requestExport(request: ExportRequest): NetworkResult<ExportJobResponse> =
        safeApiCall { api.requestExport(request) }

    /**
     * Poll the status of an existing job.
     * The caller is responsible for the polling loop, e.g.:
     * ```kotlin
     * while (true) {
     *     val status = exportRepo.pollStatus(jobId)
     *     if (status is NetworkResult.Success && status.data.status == "READY") break
     *     delay(2_000)
     * }
     * ```
     */
    suspend fun pollStatus(jobId: String): NetworkResult<ExportStatusResponse> =
        safeApiCall { api.exportStatus(jobId) }

    /**
     * Stream the finished export file to disk and return the [File].
     *
     * The file is written to `context.cacheDir/exports/<jobId>.<ext>`.
     * The caller should use [android.content.Intent.ACTION_VIEW] or the
     * Downloads provider to surface the file to the user.
     *
     * @param jobId   The job id returned by [requestExport].
     * @param format  "CSV" or "PDF" — used to derive the file extension.
     * @return [NetworkResult.Success] wrapping the saved [File], or
     *         [NetworkResult.Error] on any IO or HTTP failure.
     */
    suspend fun downloadToFile(jobId: String, format: String): NetworkResult<File> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.downloadExport(jobId)
                if (!response.isSuccessful || response.body() == null) {
                    return@withContext NetworkResult.Error(
                        httpCode = response.code(),
                        message = "Download failed: HTTP ${response.code()}",
                    )
                }

                val ext = format.lowercase()  // "csv" or "pdf"
                val dir = File(context.cacheDir, "exports").also { it.mkdirs() }
                val file = File(dir, "export_$jobId.$ext")

                response.body()!!.byteStream().use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output, bufferSize = 8 * 1024)
                    }
                }

                Timber.d("Export saved to ${file.absolutePath} (${file.length()} bytes)")
                NetworkResult.Success(file)
            } catch (e: IOException) {
                Timber.e(e, "Export download IO error")
                NetworkResult.Error(message = "Download error: ${e.localizedMessage}")
            } catch (e: Exception) {
                Timber.e(e, "Export download unexpected error")
                NetworkResult.Error(message = e.localizedMessage ?: "Unexpected error")
            }
        }
}

