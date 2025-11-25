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
        val tokenPreview = refreshToken?.take(20) ?: "null"
        Log.d("SplashActivity", "refreshToken present=${!refreshToken.isNullOrEmpty()}, preview=$tokenPreview")

        if (refreshToken.isNullOrEmpty()) {
            Log.d("SplashActivity", "BRANCH: No refresh token, going to login")
            goToMain(withAutoLogin = false)
            return
        }

        Log.d("SplashActivity", "BRANCH: Have refresh token, attempting refresh call")
        val repository = AuthRepository(applicationContext)
        lifecycleScope.launch {
            try {
                Log.d("SplashActivity", "Making refresh API call...")
                val response = repository.refreshTokens(refreshToken)
                Log.d("SplashActivity", "Refresh response code=${response.code()}, isSuccessful=${response.isSuccessful}, body present=${response.body() != null}")
                if (response.isSuccessful && response.body() != null) {
                    val body: com.example.progr3ss.model.RefreshTokenResponse = response.body()!!
                    Log.d("SplashActivity", "BRANCH: Refresh SUCCESS - saving new tokens")
                    sessionManager.saveAuthToken(body.accessToken)
                    sessionManager.saveRefreshToken(body.refreshToken)
                    Log.d("SplashActivity", "BRANCH: Calling goToMain(withAutoLogin=true)")
                    goToMain(withAutoLogin = true)
                } else {
                    Log.w("SplashActivity", "BRANCH: Refresh FAILED with code ${response.code()}")
                    sessionManager.clearAllTokens()
                    goToMain(withAutoLogin = false)
                }
            } catch (e: Exception) {
                Log.e("SplashActivity", "BRANCH: Exception during refresh", e)
                sessionManager.clearAllTokens()
                goToMain(withAutoLogin = false)
            }
        }
    }

    private fun goToMain(withAutoLogin: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("AUTO_LOGIN_SUCCESS", withAutoLogin)
            Log.d("SplashActivity", "Starting MainActivity with AUTO_LOGIN_SUCCESS=$withAutoLogin")
            startActivity(intent)
            finish()
        }, 1000)
    }
}
