package com.mtoanng.datastream.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.R
import com.mtoanng.datastream.databinding.ActivityLoginBinding
import com.mtoanng.datastream.ui.common.ViewModelFactory
import com.mtoanng.datastream.ui.main.MainActivity
import com.mtoanng.datastream.util.snack

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        ViewModelFactory(application as DataStreamApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Hứng cờ "Hết hạn" từ MainActivity
        val isExpired = intent.getBooleanExtra("EXTRA_SESSION_EXPIRED", false)
        if (isExpired) {
            binding.root.snack("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", isError = true)
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
                }
                LoginViewModel.State.Success -> goToMain()
                is LoginViewModel.State.Failure -> {
                    binding.progress.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    binding.root.snack(state.message, isError = true)
                }
            }
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
