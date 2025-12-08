package com.example.progr3ss

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
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

        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)
        graph.setStartDestination(if (autoLoginSuccess) {
            R.id.homeFragment
        } else {
            R.id.loginFragment
        })
        navController.graph = graph

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.profileFragment)
        )
        supportActionBar?.let {
            setupActionBarWithNavController(navController, appBarConfiguration)
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    val options = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, inclusive = false)
                        .setLaunchSingleTop(true)
                        .build()
                    navController.navigate(R.id.homeFragment, null, options)
                    true
                }
                R.id.profileFragment -> {
                    val options = androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, inclusive = false)
                        .setLaunchSingleTop(true)
                        .build()
                    navController.navigate(R.id.profileFragment, null, options)
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isAuthScreen = destination.id == R.id.loginFragment ||
                    destination.id == R.id.registerFragment ||
                    destination.id == R.id.resetPasswordFragment
            binding.bottomNav.isVisible = !isAuthScreen
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}