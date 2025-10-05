package com.example.dodaily

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dodaily.data.DataManager
import com.example.dodaily.fragments.HydrationScheduleFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class HydrationSettingsActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private lateinit var backButton: ImageButton
    private lateinit var dailyGoalEdit: EditText
    private lateinit var enableRemindersSwitch: Switch
    private lateinit var selectedSoundText: TextView
    private lateinit var soundSettingsLayout: LinearLayout
    private lateinit var addTimeButton: Button
    private lateinit var remindersListLayout: LinearLayout
    private lateinit var scheduledRemindersSection: LinearLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var scheduleFragmentContainer: FrameLayout
    private lateinit var scrollView: ScrollView
    
    private val remindersList = mutableListOf<ReminderTime>()
    private val reminderViews = mutableListOf<View>()
    
    data class ReminderTime(
        val id: String,
        val hour: Int,
        val minute: Int,
        val isAM: Boolean
    ) {
        fun getFormattedTime(): String {
            val period = if (isAM) "AM" else "PM"
            return String.format("%02d:%02d %s", hour, minute, period)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration_settings)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
        
        // Setup bottom navigation
        setupBottomNavigation()
        
        // Load saved settings
        loadSettings()
        
        // Setup initial reminders
        setupInitialReminders()
        
        // Load hydration schedule fragment
        loadHydrationScheduleFragment()
        
        // Request notification permission
        requestNotificationPermission()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        dailyGoalEdit = findViewById(R.id.daily_goal_edit)
        enableRemindersSwitch = findViewById(R.id.enable_reminders_switch)
        selectedSoundText = findViewById(R.id.selected_sound_text)
        soundSettingsLayout = findViewById(R.id.sound_settings_layout)
        addTimeButton = findViewById(R.id.add_time_button)
        remindersListLayout = findViewById(R.id.reminders_list_layout)
        scheduledRemindersSection = findViewById(R.id.scheduled_reminders_section)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        fragmentContainer = findViewById(R.id.fragment_container)
        scheduleFragmentContainer = findViewById(R.id.schedule_fragment_container)
        scrollView = findViewById(R.id.scroll_view)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        dailyGoalEdit.setOnEditorActionListener { _, _, _ ->
            saveDailyGoal()
            true
        }
        
        enableRemindersSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveRemindersEnabled(isChecked)
            updateRemindersVisibility(isChecked)
        }
        
        soundSettingsLayout.setOnClickListener {
            showSoundSelectionDialog()
        }
        
        addTimeButton.setOnClickListener {
            showTimePickerDialog()
        }
        
        // Setup delete buttons for existing reminders

    }
    
    private fun setupBottomNavigation() {
        // Set hydration as selected
        bottomNavigation.selectedItemId = R.id.nav_hydration
        
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
                    // Already on hydration page
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    

    
    private fun loadSettings() {
        try {
            // Load daily goal
            val dailyGoal = dataManager.getDailyHydrationGoal()
            dailyGoalEdit.setText(dailyGoal.toString())
            
            // Load reminders enabled
            val remindersEnabled = dataManager.areHydrationRemindersEnabled()
            enableRemindersSwitch.isChecked = remindersEnabled
            
            // Load selected sound
            val selectedSound = dataManager.getHydrationReminderSound()
            selectedSoundText.text = selectedSound
            
            // Update reminders visibility
            updateRemindersVisibility(remindersEnabled)
        } catch (e: Exception) {
            // Handle any potential errors during settings load
            Toast.makeText(this, "Error loading settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupInitialReminders() {
        try {
            // For now, just use the static views in the layout
            // The default reminders are already shown in the XML layout
            // We'll handle dynamic addition later if needed
        } catch (e: Exception) {
            Toast.makeText(this, "Error setting up reminders: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveDailyGoal() {
        try {
            val goal = dailyGoalEdit.text.toString().toInt()
            dataManager.saveDailyHydrationGoal(goal)
            Toast.makeText(this, "Daily goal updated", Toast.LENGTH_SHORT).show()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveRemindersEnabled(enabled: Boolean) {
        dataManager.saveHydrationRemindersEnabled(enabled)
    }
    
    private fun updateRemindersVisibility(enabled: Boolean) {
        soundSettingsLayout.visibility = if (enabled) View.VISIBLE else View.GONE
        scheduledRemindersSection.visibility = if (enabled) View.VISIBLE else View.GONE
        addTimeButton.visibility = if (enabled) View.VISIBLE else View.GONE
        remindersListLayout.visibility = if (enabled) View.VISIBLE else View.GONE
        scheduleFragmentContainer.visibility = if (enabled) View.VISIBLE else View.GONE
    }
    
    private fun showSoundSelectionDialog() {
        val sounds = arrayOf("Default", "Soft Chime", "Gentle Bell", "Water Drop", "None")
        val currentSound = selectedSoundText.text.toString()
        val currentIndex = sounds.indexOf(currentSound).coerceAtLeast(0)
        
        AlertDialog.Builder(this)
            .setTitle("Select Sound")
            .setSingleChoiceItems(sounds, currentIndex) { dialog, which ->
                val selectedSound = sounds[which]
                selectedSoundText.text = selectedSound
                dataManager.saveHydrationReminderSound(selectedSound)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val isAM = hourOfDay < 12
                val hour12 = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                val period = if (isAM) "AM" else "PM"
                val timeString = String.format("%02d:%02d %s", hour12, minute, period)
                
                // Add to the hydration schedule fragment
                val fragment = supportFragmentManager.findFragmentById(R.id.schedule_fragment_container) as? HydrationScheduleFragment
                fragment?.addNewSchedule(timeString)
            },
            currentHour,
            currentMinute,
            false // 24-hour format
        ).show()
    }
    
    private fun deleteReminder(index: Int) {
        // For now, just show a toast message
        Toast.makeText(this, "Reminder deleted", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateRemindersList() {
        // For now, this is handled by the static layout
        // Dynamic view creation will be implemented later
    }
    
    private fun saveReminders() {
        // For now, just save a simple reminder list
        val defaultReminders = listOf("8:0:AM", "12:0:PM", "4:0:PM")
        dataManager.saveHydrationReminders(defaultReminders)
    }
    
    private fun loadHydrationScheduleFragment() {
        // Load the hydration schedule fragment into the schedule container
        val fragment = HydrationScheduleFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.schedule_fragment_container, fragment)
            .commit()
    }
    
    private fun showHydrationScheduleFragment() {
        // Hide the main content and show the fragment
        scrollView.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        
        // Load the hydration schedule fragment
        val fragment = HydrationScheduleFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    private fun hideHydrationScheduleFragment() {
        // Hide the fragment and show the main content
        fragmentContainer.visibility = View.GONE
        scrollView.visibility = View.VISIBLE
    }
    
    private fun requestNotificationPermission() {
        val permissionsToRequest = mutableListOf<String>()
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                1001
            )
        }
    }
}
