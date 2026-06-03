package com.mtoanng.datastream.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.textfield.TextInputEditText
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.R
import com.mtoanng.datastream.databinding.ActivityLoginBinding
import com.mtoanng.datastream.ui.common.ViewModelFactory
import com.mtoanng.datastream.ui.main.MainActivity
import com.mtoanng.datastream.util.snack
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import timber.log.Timber

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val callbackManager = CallbackManager.Factory.create()

    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(application as DataStreamApp)
    }

    // Xử lý kết quả trả về từ Google
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.loginWithGoogle(idToken)
            } else {
                binding.root.snack("Không lấy được ID Token từ Google. Hãy chắc chắn bạn đã dùng WEB Client ID.", isError = true)
            }
        } catch (e: Exception) {
            val statusCode = (e as? com.google.android.gms.common.api.ApiException)?.statusCode
            when (statusCode) {
                10 -> binding.root.snack("Lỗi 10: Sai SHA-1 hoặc Client ID. Kiểm tra Console!", isError = true)
                12500 -> binding.root.snack("Lỗi 12500: Chưa cấu hình Email hỗ trợ hoặc Test User trong OAuth Consent Screen!", isError = true)
                7 -> binding.root.snack("Lỗi 7: Lỗi mạng hoặc Google Play Services.", isError = true)
                else -> binding.root.snack("Lỗi Google ($statusCode): ${e.message}", isError = true)
            }
            Timber.tag("GOOGLE_AUTH").e("Sign-In failed. Code: $statusCode. Message: ${e.message}")
            if (statusCode == 12500) {
                Timber.tag("GOOGLE_AUTH").e("Gợi ý sửa lỗi 12500: Vào OAuth Consent Screen -> Chọn Support Email -> Thêm email của bạn vào Test Users.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSocialLogins()
        printSignatureInfo() // Tự động in SHA-1 ra Logcat để dễ cấu hình Google

        // 1. Hứng cờ "Hết hạn" từ MainActivity
        val isExpired = intent.getBooleanExtra("EXTRA_SESSION_EXPIRED", false)
        if (isExpired) {
            binding.root.snack("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", isError = true)
            // Clear Google session as well
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(this, gso).signOut()
        }
        // 2. Nếu không có cờ và vẫn còn đăng nhập thì vào Main
        else if (viewModel.isLoggedIn()) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etUsername.text?.toString().orEmpty(),
                binding.etPassword.text?.toString().orEmpty(),
            )
        }

        binding.tvConfigureServer.setOnClickListener { showServerDialog() }

        viewModel.state.observe(this) { state ->
            when (state) {
                LoginViewModel.State.Idle -> Unit
                LoginViewModel.State.Loading -> {
                    binding.progress.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                    binding.btnGoogle.isEnabled = false
                    binding.btnFacebook.isEnabled = false
                }
                LoginViewModel.State.Success -> goToMain()
                is LoginViewModel.State.Failure -> {
                    binding.progress.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.btnGoogle.isEnabled = true
                    binding.btnFacebook.isEnabled = true
                    binding.root.snack(state.message, isError = true)
                }
            }
        }
    }

    private fun setupSocialLogins() {
        // Cấu hình Google
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.google_web_client_id))
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)

            binding.btnGoogle.setOnClickListener {
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Google Sign-In")
        }

        // Cấu hình Facebook
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.loginWithFacebook(result.accessToken.token)
            }
            override fun onCancel() {
                binding.root.snack("Hủy đăng nhập Facebook")
            }
            override fun onError(error: FacebookException) {
                binding.root.snack("Lỗi Facebook: ${error.message}", isError = true)
            }
        })

        binding.btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, callbackManager, listOf("email", "public_profile"))
        }
    }

    /**
     * In mã SHA-1 ra Logcat. Bạn chỉ cần Copy mã này dán vào Google Cloud Console.
     */
    private fun printSignatureInfo() {
        try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                info.signatures
            }

            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val sha1 = md.digest().joinToString(":") { "%02X".format(it) }
                Timber.tag("GOOGLE_AUTH_DEBUG").d("Mã SHA-1 của máy bạn là: $sha1")
                Timber.tag("GOOGLE_AUTH_DEBUG").d("Hãy copy mã trên dán vào Google Cloud Console (mục Android Client ID)")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error printing signature info")
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showServerDialog() {
        val input = TextInputEditText(this).apply {
            setText(viewModel.currentBaseUrl())
            setSelection(text?.length ?: 0)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_server_title)
            .setMessage(R.string.dialog_server_message)
            .setView(input)
            .setPositiveButton(R.string.action_save) { _, _ ->
                val newUrl = input.text?.toString().orEmpty()
                viewModel.updateBaseUrl(newUrl)
                binding.root.snack(getString(R.string.toast_server_saved, viewModel.currentBaseUrl()))
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }
}
