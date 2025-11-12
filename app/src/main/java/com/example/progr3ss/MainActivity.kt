package com.example.progr3ss

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController

import com.example.lab5.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        // Inflate binding instead of setContentView with layout
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
// Get NavController from NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
// Define top-level destinations for ActionBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.profileFragment, R.id.registerFragment, R.id.loginFragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
// Hook up BottomNavigationView via binding
        binding.bottomNav.setupWithNavController(navController)
    }
}