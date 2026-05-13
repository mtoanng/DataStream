package com.mtoanng.datastream.ui.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mtoanng.datastream.data.dto.LoginResponse
import com.mtoanng.datastream.data.dto.UserDto
import com.mtoanng.datastream.data.network.ApiService
import com.mtoanng.datastream.data.network.NetworkResult
import com.mtoanng.datastream.data.prefs.AppConfig
import com.mtoanng.datastream.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule val rule = InstantTaskExecutorRule()

    private val authRepo: AuthRepository = mock()
    private val appConfig: AppConfig = mock()
    private val rebuildApi: () -> ApiService = mock()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(appConfig.baseUrl).thenReturn("http://10.0.2.2:8090/")
        viewModel = LoginViewModel(authRepo, appConfig, rebuildApi)
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `login with blank credentials emits Failure synchronously`() = runTest {
        viewModel.login("", "")
        val state = viewModel.state.value
        assertTrue(state is LoginViewModel.State.Failure)
    }

    @Test
    fun `login emits Success when repo returns Success`() = runTest {
        val user = UserDto(1, "admin", "Admin", "a@b", "ADMIN", true)
        whenever(authRepo.login(any(), any()))
            .thenReturn(NetworkResult.Success(LoginResponse("token", 1000L, user)))

        viewModel.login("admin", "admin")

        assertEquals(LoginViewModel.State.Success, viewModel.state.value)
    }

    @Test
    fun `login emits Failure when repo returns Error`() = runTest {
        whenever(authRepo.login(any(), any()))
            .thenReturn(NetworkResult.Error(401, "Bad credentials"))

        viewModel.login("admin", "wrong")

        val state = viewModel.state.value
        assertTrue(state is LoginViewModel.State.Failure)
        assertEquals("Bad credentials", (state as LoginViewModel.State.Failure).message)
    }

    @Test
    fun `updateBaseUrl writes to AppConfig and rebuilds API`() {
        viewModel.updateBaseUrl("http://192.168.1.5:8090")
        org.mockito.kotlin.verify(appConfig).baseUrl = "http://192.168.1.5:8090"
        org.mockito.kotlin.verify(rebuildApi).invoke()
    }
}
