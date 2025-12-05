package com.example.progr3ss.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
            goToMain(withAutoLogin = false)
            return
        }

        val repository = AuthRepository(applicationContext)
        lifecycleScope.launch {
            try {
                val response = repository.refreshTokens(refreshToken)
                if (response.isSuccessful && response.body() != null) {
                    val body: com.example.progr3ss.model.RefreshTokenResponse = response.body()!!
                    sessionManager.saveAuthToken(body.accessToken)
                    sessionManager.saveRefreshToken(body.refreshToken)
                    goToMain(withAutoLogin = true)
                } else {
                    sessionManager.clearAllTokens()
                    goToMain(withAutoLogin = false)
                }
            } catch (e: Exception) {
                sessionManager.clearAllTokens()
                goToMain(withAutoLogin = false)
            }
        }
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
