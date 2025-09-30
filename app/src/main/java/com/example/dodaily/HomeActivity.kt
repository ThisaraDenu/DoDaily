package com.example.dodaily

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.dodaily.fragments.HabitsFragment
import com.example.dodaily.fragments.MoodJournalFragment
import com.example.dodaily.fragments.SettingsFragment
import com.example.dodaily.data.DataManager

class HomeActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dataManager: DataManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Reset habits for new day if needed
        dataManager.resetHabitsForNewDay()
        
        // Initialize bottom navigation
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_habits -> {
                    replaceFragment(HabitsFragment())
                    true
                }
                R.id.nav_mood -> {
                    replaceFragment(MoodJournalFragment())
                    true
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
        
        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(HabitsFragment())
        }
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    fun getDataManager(): DataManager = dataManager
}