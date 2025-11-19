package com.example.progr3ss.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.progr3ss.MainActivity
import com.example.progr3ss.repository.AuthRepository
import com.example.progr3ss.utils.SessionManager
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.progr3ss.R.layout.activity_splash)

        val sessionManager = SessionManager(applicationContext)
        val refreshToken = sessionManager.fetchRefreshToken()

        if (refreshToken.isNullOrEmpty()) {
            goToMainAsLogin()
            return
        }

        val repository = AuthRepository(applicationContext)
        lifecycleScope.launch {
            try {
                val response = repository.refreshTokens(refreshToken)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    // Save new tokens
                    sessionManager.saveAuthToken(body.tokens.accessToken)
                    sessionManager.saveRefreshToken(body.tokens.refreshToken)
                    goToMainAsHome()
                } else {
                    Log.w("SplashActivity", "Token refresh failed: ${'$'}{response.code()}")
                    sessionManager.clearAllTokens()
                    goToMainAsLogin()
                }
            } catch (e: Exception) {
                Log.e("SplashActivity", "Error refreshing token", e)
                sessionManager.clearAllTokens()
                goToMainAsLogin()
            }
        }
    }

    private fun goToMainAsHome() {
        goToMain(withAutoLogin = true)
    }

    private fun goToMainAsLogin() {
        goToMain(withAutoLogin = false)
    }

    private fun goToMain(withAutoLogin: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("AUTO_LOGIN_SUCCESS", withAutoLogin)
            startActivity(intent)
            finish()
        }, 1000)
    }
}

