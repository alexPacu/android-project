package com.example.progr3ss

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.progr3ss.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar as ActionBar for NavigationUI
        findViewById<MaterialToolbar>(R.id.topAppBar)?.let { toolbar ->
            setSupportActionBar(toolbar)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.profileFragment, R.id.registerFragment, R.id.loginFragment, R.id.homeFragment)
        )
        // Only configure ActionBar if present
        if (supportActionBar != null) {
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
        binding.bottomNav.setupWithNavController(navController)

        val autoLoginSuccess = intent.getBooleanExtra("AUTO_LOGIN_SUCCESS", false)
        if (autoLoginSuccess) {
            if (navController.currentDestination?.id == R.id.loginFragment) {
                navController.navigate(R.id.homeFragment)
            }
        }
    }
}