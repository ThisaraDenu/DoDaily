package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dodaily.data.DataManager

class DataPrivacyActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_settings)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Setup click listeners
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        // Clear data button
        findViewById<Button>(R.id.clear_data_button).setOnClickListener {
            showClearDataDialog()
        }
    }
    
    private fun showClearDataDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Clear All Data")
            .setMessage("This will permanently delete all your mood entries, habits, and settings. This action cannot be undone.\n\nAre you sure you want to continue?")
            .setPositiveButton("Clear All Data") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun clearAllData() {
        try {
            // Clear all mood entries
            dataManager.saveMoodEntries(emptyList())
            
            // Clear all habits
            dataManager.saveHabits(emptyList())
            
            // Clear hydration settings
            dataManager.saveDailyHydrationGoal(2500)
            dataManager.saveHydrationRemindersEnabled(false)
            dataManager.saveHydrationReminderSound("Default")
            dataManager.saveHydrationReminders(emptyList())
            dataManager.saveHydrationSchedules(emptyList())
            
            Toast.makeText(this, "All data has been cleared successfully", Toast.LENGTH_LONG).show()
            
            // Navigate back to home
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error clearing data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
