package com.example.dodaily

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class SettingActivity : AppCompatActivity() {
    
    private lateinit var logoutButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_auth", MODE_PRIVATE)
        
        // Initialize logout button
        logoutButton = findViewById(R.id.logout_button)
        
        setupBottomNavigation()
        setupClickListeners()
    }
    
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_settings
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    val intent = Intent(this, MoodPageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_hydration -> {
                    // Navigate to HydrationSettingsActivity
                    val intent = Intent(this, HydrationSettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    // Already on settings page
                    true
                }
                else -> false
            }
        }
    }
    
    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle configuration changes (like rotation) without recreating the activity
        // The layout will automatically switch to landscape/portrait based on the layout-land folder
    }
    
    private fun setupClickListeners() {
        // Back button
        findViewById<View>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
        
        // Profile section - navigate to profile page
        findViewById<View>(R.id.profile_section).setOnClickListener {
            openProfilePage()
        }
        
        // For now, show toast messages for each section
        // You can replace these with actual navigation to specific settings pages
        
        // Theme section
        // findViewById<View>(R.id.theme_section).setOnClickListener {
        //     Toast.makeText(this, "Theme settings coming soon!", Toast.LENGTH_SHORT).show()
        // }
        
        // Notifications section
        // findViewById<View>(R.id.notifications_section).setOnClickListener {
        //     Toast.makeText(this, "Notification settings coming soon!", Toast.LENGTH_SHORT).show()
        // }
        
        // Language section
        // findViewById<View>(R.id.language_section).setOnClickListener {
        //     Toast.makeText(this, "Language settings coming soon!", Toast.LENGTH_SHORT).show()
        // }
        
        // Data & Privacy section
        findViewById<View>(R.id.data_privacy_section).setOnClickListener {
            openDataPrivacySettings()
        }
        
        // Help & Support section
        // findViewById<View>(R.id.help_support_section).setOnClickListener {
        //     Toast.makeText(this, "Help & Support coming soon!", Toast.LENGTH_SHORT).show()
        // }
        
        // Sign Out section
        findViewById<View>(R.id.sign_out_section).setOnClickListener {
            showSignOutConfirmation()
        }
        
        // Logout button
        logoutButton.setOnClickListener {
            logout()
        }
    }
    
    private fun openDataPrivacySettings() {
        // Create a new activity to show the data privacy settings
        val intent = Intent(this, DataPrivacyActivity::class.java)
        startActivity(intent)
    }
    
    private fun openProfilePage() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }
    
    private fun showSignOutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun signOut() {
        // Just set logged in to false, keep user data for easy re-login
        val sharedPreferences = getSharedPreferences("user_auth", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("user_logged_in", false)
            apply()
        }
        
        Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_SHORT).show()
        
        // Navigate to login screen
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    
    private fun logout() {
        // Just set logged in to false, keep user data for easy re-login
        sharedPreferences.edit().apply {
            putBoolean("user_logged_in", false)
            apply()
        }
        
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        
        // Navigate to login screen
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}