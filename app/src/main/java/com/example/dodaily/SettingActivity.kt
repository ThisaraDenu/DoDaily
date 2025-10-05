package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_setting)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
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
    
    private fun setupClickListeners() {
        // Back button
        findViewById<View>(R.id.back_button).setOnClickListener {
            onBackPressed()
        }
        
        // Profile section - you can add specific navigation here
        // findViewById<View>(R.id.profile_section).setOnClickListener {
        //     // Navigate to profile settings
        // }
        
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
        // findViewById<View>(R.id.data_privacy_section).setOnClickListener {
        //     Toast.makeText(this, "Data & Privacy settings coming soon!", Toast.LENGTH_SHORT).show()
        // }
        
        // Help & Support section
        // findViewById<View>(R.id.help_support_section).setOnClickListener {
        //     Toast.makeText(this, "Help & Support coming soon!", Toast.LENGTH_SHORT).show()
        // }
    }
}