// File: MainActivity.kt
package com.mtoanng.datastream.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.databinding.ActivityMainBinding
import com.mtoanng.datastream.ui.login.LoginActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: DataStreamApp

    // Hứng sự kiện 401 từ Interceptor
    private val tokenExpiredReceiver = object : BroadcastReceiver() {
        // Sửa 'receive' thành 'onReceive'
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.mtoanng.datastream.ACTION_TOKEN_EXPIRED") {
                forceLogoutAndNavigateToLogin()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as DataStreamApp

        if (!app.tokenManager.isLoggedIn()) {
            forceLogoutAndNavigateToLogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHost.navController)

        // Đăng ký Receiver
        ContextCompat.registerReceiver(
            this,
            tokenExpiredReceiver,
            IntentFilter("com.mtoanng.datastream.ACTION_TOKEN_EXPIRED"),
            ContextCompat.RECEIVER_NOT_EXPORTED // Bảo mật
        )
    }

    override fun onResume() {
        super.onResume()
        checkAndSilentRefresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(tokenExpiredReceiver)
    }

    /** Gia hạn âm thầm nếu thời gian còn lại dưới 5 phút */
    private fun checkAndSilentRefresh() {
        val currentTime = System.currentTimeMillis()
        val expiresAt = app.tokenManager.expiresAtMillis()
        val timeRemaining = expiresAt - currentTime

        // 300,000 ms = 5 phút. Nếu còn dưới 5 phút và token vẫn còn hạn.
        if (timeRemaining in 1..300_000L) {
            lifecycleScope.launch {
                // Gọi thẳng AuthRepository (giả định bạn đã có instance)
                // Lưu ý: Không dùng logoutServer() nếu refresh lỗi ở đây,
                // cứ để tự nhiên, lần gọi API tiếp theo sẽ dính 401 và tự xử lý.
                app.authRepository().refreshToken()
            }
        } else if (timeRemaining <= 0) {
            // Hết hạn hẳn khi app đang ở background -> ép xuất
            forceLogoutAndNavigateToLogin()
        }
    }

    private fun forceLogoutAndNavigateToLogin() {
        // 1. Dọn dẹp Local
        app.authRepository().logout()

        // 2. Mở LoginActivity với cờ (flag)
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_SESSION_EXPIRED", true)
        }
        startActivity(intent)
        finish() // Đóng MainActivity
    }
}
