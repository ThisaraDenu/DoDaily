package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.dodaily.fragments.HabitsFragment
import com.example.dodaily.fragments.MoodJournalFragment
import com.example.dodaily.fragments.CompletedHabitsFragment
import com.example.dodaily.data.DataManager
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dataManager: DataManager
    private lateinit var horizontalCalendarContainer: LinearLayout
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedDateIndex: Int = 0
    private var dateItems: MutableList<View> = mutableListOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Initialize views
        initializeViews()
        setupHorizontalCalendar()
        
        // Reset habits for new day if needed
        dataManager.resetHabitsForNewDay()
        
        // Initialize bottom navigation
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Reload HabitsFragment when returning to home
                    val habitsFragment = HabitsFragment()
                    habitsFragment.onHabitsUpdated = {
                        refreshCompletedHabitsFragment()
                    }
                    replaceFragment(habitsFragment)
                    true
                }
                R.id.nav_mood -> {
                    // Navigate to MoodPageActivity
                    val intent = Intent(this, MoodPageActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_hydration -> {
                    // Navigate to HydrationSettingsActivity
                    val intent = Intent(this, HydrationSettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    // Navigate to SettingActivity
                    val intent = Intent(this, SettingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        // Debug: Add some test habits
        addTestHabits()
        
        // Set default fragments - load both HabitsFragment and CompletedHabitsFragment
        if (savedInstanceState == null) {
            loadHomeFragments()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh habits when returning to the activity
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val completedFragment = supportFragmentManager.findFragmentById(R.id.completed_fragment_container)
        
        if (currentFragment is HabitsFragment) {
            currentFragment.updateSelectedDate(selectedDate.time)
        }
        if (completedFragment is CompletedHabitsFragment) {
            completedFragment.updateSelectedDate(selectedDate.time)
        }
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    private fun loadHomeFragments() {
        // Load daily habits fragment
        val habitsFragment = HabitsFragment()
        habitsFragment.onHabitsUpdated = {
            // Refresh completed habits fragment when habits are updated
            refreshCompletedHabitsFragment()
        }
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, habitsFragment)
            .commit()
            
        // Load completed habits fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.completed_fragment_container, CompletedHabitsFragment())
            .commit()
    }
    
    private fun refreshCompletedHabitsFragment() {
        val completedFragment = supportFragmentManager.findFragmentById(R.id.completed_fragment_container)
        if (completedFragment is CompletedHabitsFragment) {
            completedFragment.updateSelectedDate(selectedDate.time)
        }
    }
    
    fun getDataManager(): DataManager = dataManager
    
    private fun initializeViews() {
        horizontalCalendarContainer = findViewById(R.id.horizontal_calendar_container)
    }
    
    private fun setupHorizontalCalendar() {
        // Get the static date items from XML
        dateItems = mutableListOf()
        
        // Try to find each date item with null safety
        val dateItemIds = listOf(
            R.id.date_item_1,
            R.id.date_item_2,
            R.id.date_item_3,
            R.id.date_item_4,
            R.id.date_item_5,
            R.id.date_item_6,
            R.id.date_item_7
        )
        
        dateItemIds.forEach { id ->
            val dateItem = findViewById<View>(id)
            if (dateItem != null) {
                dateItems.add(dateItem)
            }
        }
        
        // If no items found, create them programmatically
        if (dateItems.isEmpty()) {
            createDateItemsProgrammatically()
            return
        }
        
        // Generate dates for the current week (7 days)
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.time
        
        // Find the start of the week (Sunday)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - Calendar.SUNDAY
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
        
        // Update each date item with real dates
        dateItems.forEachIndexed { index, dateItem ->
            updateDateItem(dateItem, calendar)
            
            // Set click listener
            dateItem.setOnClickListener {
                selectDate(index)
            }
            
            // Move to next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Set today as selected by default (find today's index in the week)
        val today = Calendar.getInstance()
        val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val todayIndex = if (todayDayOfWeek == Calendar.SUNDAY) 0 else todayDayOfWeek - Calendar.SUNDAY
        selectDate(todayIndex)
    }
    
    private fun updateDateItem(dateItem: View, calendar: Calendar) {
        val dateNumber = dateItem.findViewById<TextView>(R.id.date_number)
        val dayAbbreviation = dateItem.findViewById<TextView>(R.id.day_abbreviation)
        
        // Set date number
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        dateNumber.text = dayOfMonth.toString()
        
        // Set day abbreviation
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        dayAbbreviation.text = dayFormat.format(calendar.time).uppercase()
    }
    
    private fun createDateItemsProgrammatically() {
        // Clear existing items
        horizontalCalendarContainer.removeAllViews()
        dateItems.clear()
        
        // Get current device date
        val today = Calendar.getInstance()
        
        // Find the start of the current week (Sunday)
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)
        val daysFromSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - Calendar.SUNDAY
        today.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
        
        // Create 7 date items for the week
        for (i in 0..6) {
            val dateItem = createDateItem(today)
            dateItems.add(dateItem)
            horizontalCalendarContainer.addView(dateItem)
            
            // Move to next day
            today.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        // Set today as selected by default
        val currentDay = Calendar.getInstance()
        val currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK)
        val todayIndex = if (currentDayOfWeek == Calendar.SUNDAY) 0 else currentDayOfWeek - Calendar.SUNDAY
        selectDate(todayIndex)
    }
    
    private fun createDateItem(calendar: Calendar): View {
        val inflater = LayoutInflater.from(this)
        val dateItem = inflater.inflate(R.layout.item_date_picker, horizontalCalendarContainer, false)
        
        val dateNumber = dateItem.findViewById<TextView>(R.id.date_number)
        val dayAbbreviation = dateItem.findViewById<TextView>(R.id.day_abbreviation)
        
        // Set date number
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        dateNumber.text = dayOfMonth.toString()
        
        // Set day abbreviation
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        dayAbbreviation.text = dayFormat.format(calendar.time).uppercase()
        
        // Set click listener
        dateItem.setOnClickListener {
            val index = dateItems.indexOf(dateItem)
            selectDate(index)
        }
        
        return dateItem
    }
    
    private fun selectDate(index: Int) {
        selectedDateIndex = index
        
        // Update visual selection
        dateItems.forEachIndexed { i, item ->
            val dateNumber = item.findViewById<TextView>(R.id.date_number)
            val dayAbbreviation = item.findViewById<TextView>(R.id.day_abbreviation)
            
            if (i == index) {
                // Selected state
                item.setBackgroundResource(R.drawable.date_selected_background)
                dateNumber.setTextColor(resources.getColor(android.R.color.white, null))
                dayAbbreviation.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                // Unselected state
                item.setBackgroundResource(R.drawable.date_unselected_background)
                dateNumber.setTextColor(resources.getColor(R.color.text_primary, null))
                dayAbbreviation.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }
        
        // Update selected date
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.time
        
        // Find the start of the current week (Sunday)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromSunday = if (dayOfWeek == Calendar.SUNDAY) 0 else dayOfWeek - Calendar.SUNDAY
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromSunday)
        
        // Add the selected index to get the actual selected date
        calendar.add(Calendar.DAY_OF_MONTH, index)
        selectedDate = calendar
        
        // Update habits display for selected date
        updateHabitsFragment()
    }
    
    private fun updateHabitsFragment() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val completedFragment = supportFragmentManager.findFragmentById(R.id.completed_fragment_container)
        
        if (currentFragment is HabitsFragment) {
            currentFragment.updateSelectedDate(selectedDate.time)
        }
        if (completedFragment is CompletedHabitsFragment) {
            completedFragment.updateSelectedDate(selectedDate.time)
        }
    }
    
    private fun addTestHabits() {
        // Add some test habits if none exist
        val existingHabits = dataManager.loadHabits()
        if (existingHabits.isEmpty()) {
            val testHabits = listOf(
                com.example.dodaily.model.Habit(
                    name = "Drink Water",
                    description = "Stay hydrated throughout the day",
                    targetCount = 8,
                    emoji = "💧"
                ),
                com.example.dodaily.model.Habit(
                    name = "Exercise",
                    description = "30 minutes of physical activity",
                    targetCount = 1,
                    emoji = "🏃‍♂️"
                ),
                com.example.dodaily.model.Habit(
                    name = "Read",
                    description = "Read for 20 minutes",
                    targetCount = 1,
                    emoji = "📚"
                )
            )
            
            testHabits.forEach { habit ->
                dataManager.addHabit(habit)
            }
        }
    }
}