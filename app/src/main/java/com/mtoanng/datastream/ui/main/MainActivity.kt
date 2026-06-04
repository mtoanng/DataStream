// File: MainActivity.kt
package com.mtoanng.datastream.ui.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mtoanng.datastream.DataStreamApp
import com.mtoanng.datastream.databinding.ActivityMainBinding
import com.mtoanng.datastream.ui.login.LoginActivity
import kotlinx.coroutines.launch
import kotlin.math.abs

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

        setupDraggableFab(navHost)

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
        // Chỉ logout nếu thực sự đang có session
        if (app.tokenManager.getToken() == null) return

        // 1. Dọn dẹp Local
        app.authRepository().logout()

        // 2. Sign out Google để lần sau được chọn lại tài khoản
        com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
            this,
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        ).signOut()

        // 3. Mở LoginActivity với cờ (flag)
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_SESSION_EXPIRED", true)
        }
        startActivity(intent)
        finish() // Đóng MainActivity
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupDraggableFab(navHost: NavHostFragment) {
        var dX = 0f
        var dY = 0f
        var downRawX = 0f
        var downRawY = 0f
        val clickThreshold = 10

        binding.fabChat.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    downRawX = event.rawX
                    downRawY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY

                    // Giới hạn trong màn hình (optional but recommended)
                    val parent = view.parent as View
                    val maxX = (parent.width - view.width).toFloat()
                    val maxY = (parent.height - view.height).toFloat()

                    view.x = newX.coerceIn(0f, maxX)
                    view.y = newY.coerceIn(0f, maxY)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val upRawX = event.rawX
                    val upRawY = event.rawY
                    val distance = abs(upRawX - downRawX) + abs(upRawY - downRawY)

                    if (distance < clickThreshold) {
                        navHost.navController.navigate(com.mtoanng.datastream.R.id.chatFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }
}
