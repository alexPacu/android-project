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

        findViewById<MaterialToolbar>(R.id.topAppBar)?.let { toolbar ->
            setSupportActionBar(toolbar)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val autoLoginSuccess = intent.getBooleanExtra("AUTO_LOGIN_SUCCESS", false)
        android.util.Log.d("MainActivity", "AUTO_LOGIN_SUCCESS=$autoLoginSuccess")

        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(if (autoLoginSuccess) {
            R.id.homeFragment
        } else {
            R.id.loginFragment
        })
        navController.graph = graph

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.homeFragment)
        )
        supportActionBar?.let {
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}