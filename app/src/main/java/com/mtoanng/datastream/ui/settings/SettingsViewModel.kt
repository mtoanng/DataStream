package com.mtoanng.datastream.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mtoanng.datastream.data.dto.UserDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.repository.AuthRepository
import com.mtoanng.datastream.data.repository.SecurityRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val securityRepository: SecurityRepository,
    private val appConfig: AppConfig,
    private val rebuildApi: () -> ApiService,
) : ViewModel() {

    private val _user = MutableLiveData<UserDto?>(authRepository.cachedUser())
    val user: LiveData<UserDto?> = _user

    private val _testResult = MutableLiveData<String?>()
    val testResult: LiveData<String?> = _testResult

    fun currentBaseUrl(): String = appConfig.baseUrl

    fun saveBaseUrl(url: String) {
        appConfig.baseUrl = url
        rebuildApi()
    }

    fun refreshUser() {
        viewModelScope.launch {
            when (val r = authRepository.me()) {
                is NetworkResult.Success -> _user.value = r.data
                is NetworkResult.Error   -> _testResult.value = "Profile fetch failed: ${r.message}"
                NetworkResult.Loading    -> Unit
            }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            when (val r = securityRepository.getHealth()) {
                is NetworkResult.Success -> _testResult.value = "OK — backend status ${r.data.status}"
                is NetworkResult.Error   -> _testResult.value = "Failed (${r.httpCode}): ${r.message}"
                NetworkResult.Loading    -> Unit
            }
        }
    }
    /**
     * Thực hiện đăng xuất: Gửi yêu cầu vô hiệu hóa token lên server,
     * đồng thời xóa session ở local và cập nhật trạng thái UI.
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logoutServer()
            _user.value = null
            // Bạn có thể kích hoạt thêm một LiveData sự kiện ở đây để Fragment biết
            // và tự động chuyển hướng (Navigate) về LoginActivity/LoginFragment
        }
    }
}
