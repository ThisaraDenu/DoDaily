package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.adapters.MoodEntriesAdapter
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MoodPageActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private lateinit var addMoodButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var moodEntriesRecycler: RecyclerView
    private lateinit var moodEmptyState: LinearLayout
    private lateinit var moodAdapter: MoodEntriesAdapter
    private lateinit var avgMoodText: TextView
    private lateinit var totalEntriesText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    
    // Trend tabs
    private lateinit var tabToday: TextView
    private lateinit var tabWeekly: TextView
    private lateinit var tabMonthly: TextView
    private var currentTrendType = "today"
    
    // Daily limit indicator
    private lateinit var dailyLimitIndicator: TextView
    
    // Chart components
    private lateinit var moodChartContainer: LinearLayout
    private lateinit var chartPlaceholder: TextView
    private lateinit var chartMessage: TextView
    private lateinit var chartTimeRange: TextView
    
    private val moodEntries = mutableListOf<MoodEntry>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mood_page)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Initialize views
        initializeViews()
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup click listeners
        setupClickListeners()
        
        // Setup bottom navigation
        setupBottomNavigation()
        
        // Load mood data
        loadMoodData()
        
        // Initialize chart with today's data
        updateTodayChart()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Refresh data when returning to existing activity
        loadMoodData()
        updateTodayChart()
    }
    
    private fun initializeViews() {
        addMoodButton = findViewById(R.id.add_mood_button)
        backButton = findViewById(R.id.back_button)
        moodEntriesRecycler = findViewById(R.id.mood_entries_recycler)
        moodEmptyState = findViewById(R.id.mood_empty_state)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        // Initialize trend tabs
        tabToday = findViewById(R.id.tab_today)
        tabWeekly = findViewById(R.id.tab_weekly)
        tabMonthly = findViewById(R.id.tab_monthly)
        
        // Initialize daily limit indicator
        dailyLimitIndicator = findViewById(R.id.daily_limit_indicator)
        
        // Initialize chart components
        moodChartContainer = findViewById(R.id.mood_chart_container)
        chartPlaceholder = findViewById(R.id.chart_placeholder)
        chartMessage = findViewById(R.id.chart_message)
        chartTimeRange = findViewById(R.id.chart_time_range)
        
        // Find stats text views by looking for specific patterns in the layout
        // Since we don't have IDs for them, we'll update them programmatically
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodEntriesAdapter(moodEntries) { moodEntry ->
            // Handle mood entry click - could open edit dialog
            showMoodEntryDetails(moodEntry)
        }
        
        moodEntriesRecycler.layoutManager = LinearLayoutManager(this)
        moodEntriesRecycler.adapter = moodAdapter
    }
    
    private fun setupClickListeners() {
        addMoodButton.setOnClickListener {
            // Navigate to MoodLoggingActivity for adding new mood
            val intent = Intent(this, MoodLoggingActivity::class.java)
            startActivity(intent)
        }
        
        backButton.setOnClickListener {
            // Navigate back to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        
        // Trend tab click listeners
        tabToday.setOnClickListener {
            selectTrendTab("today")
        }
        
        tabWeekly.setOnClickListener {
            selectTrendTab("weekly")
        }
        
        tabMonthly.setOnClickListener {
            selectTrendTab("monthly")
        }
    }
    
    private fun setupBottomNavigation() {
        // Set mood as selected
        bottomNavigation.selectedItemId = R.id.nav_mood
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Navigate to HomeActivity
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_mood -> {
                    // Already on mood page
                    true
                }
                R.id.nav_hydration -> {
                    // TODO: Implement hydration functionality
                    Toast.makeText(this, "Hydration feature coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    // Navigate to SettingActivity
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
    
    private fun loadMoodData() {
        moodEntries.clear()
        val allMoodEntries = dataManager.loadMoodEntries().sortedByDescending { it.dateTime }
        moodEntries.addAll(allMoodEntries)
        
        moodAdapter.notifyDataSetChanged()
        updateUI()
        updateStats()
        updateDailyLimitInfo()
    }
    
    private fun updateDailyLimitInfo() {
        val today = Calendar.getInstance()
        val todayMoodEntries = dataManager.getMoodEntriesForDate(today.time)
        val remainingEntries = 10 - todayMoodEntries.size
        
        // Update the daily limit indicator
        dailyLimitIndicator.text = "Daily entries: ${todayMoodEntries.size}/10"
        dailyLimitIndicator.visibility = if (todayMoodEntries.size > 0) View.VISIBLE else View.GONE
        
        // Change color based on remaining entries
        when {
            remainingEntries <= 0 -> {
                dailyLimitIndicator.setTextColor(getColor(R.color.mood_very_sad))
                dailyLimitIndicator.text = "Daily limit reached (10/10)"
            }
            remainingEntries <= 2 -> {
                dailyLimitIndicator.setTextColor(getColor(R.color.mood_sad))
            }
            else -> {
                dailyLimitIndicator.setTextColor(getColor(R.color.text_secondary))
            }
        }
        
        // Show warning when only 2 or fewer entries remaining
        if (remainingEntries <= 2 && remainingEntries > 0) {
            android.widget.Toast.makeText(
                this,
                "You have $remainingEntries mood entries remaining today.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun updateUI() {
        if (moodEntries.isEmpty()) {
            moodEmptyState.visibility = View.VISIBLE
            moodEntriesRecycler.visibility = View.GONE
        } else {
            moodEmptyState.visibility = View.GONE
            moodEntriesRecycler.visibility = View.VISIBLE
        }
    }
    
    private fun selectTrendTab(trendType: String) {
        currentTrendType = trendType
        
        // Update tab appearances
        when (trendType) {
            "today" -> {
                tabToday.setTextColor(getColor(R.color.text_primary))
                tabToday.setBackgroundResource(R.drawable.tab_selected_background)
                tabWeekly.setTextColor(getColor(R.color.text_secondary))
                tabWeekly.setBackgroundResource(android.R.color.transparent)
                tabMonthly.setTextColor(getColor(R.color.text_secondary))
                tabMonthly.setBackgroundResource(android.R.color.transparent)
            }
            "weekly" -> {
                tabToday.setTextColor(getColor(R.color.text_secondary))
                tabToday.setBackgroundResource(android.R.color.transparent)
                tabWeekly.setTextColor(getColor(R.color.text_primary))
                tabWeekly.setBackgroundResource(R.drawable.tab_selected_background)
                tabMonthly.setTextColor(getColor(R.color.text_secondary))
                tabMonthly.setBackgroundResource(android.R.color.transparent)
            }
            "monthly" -> {
                tabToday.setTextColor(getColor(R.color.text_secondary))
                tabToday.setBackgroundResource(android.R.color.transparent)
                tabWeekly.setTextColor(getColor(R.color.text_secondary))
                tabWeekly.setBackgroundResource(android.R.color.transparent)
                tabMonthly.setTextColor(getColor(R.color.text_primary))
                tabMonthly.setBackgroundResource(R.drawable.tab_selected_background)
            }
        }
        
        // Update chart content based on selected trend
        updateTrendChart(trendType)
    }
    
    private fun updateTrendChart(trendType: String) {
        when (trendType) {
            "today" -> {
                updateTodayChart()
            }
            "weekly" -> {
                updateWeeklyChart()
            }
            "monthly" -> {
                updateMonthlyChart()
            }
        }
    }
    
    private fun updateTodayChart() {
        val today = Calendar.getInstance()
        val todayMoodEntries = dataManager.getMoodEntriesForDate(today.time)
        
        if (todayMoodEntries.isEmpty()) {
            // Show empty state
            chartPlaceholder.text = "📈"
            chartMessage.text = "No mood data for today yet"
            chartMessage.visibility = View.VISIBLE
            chartPlaceholder.visibility = View.VISIBLE
            clearChartData()
        } else {
            // Show chart data
            chartPlaceholder.visibility = View.GONE
            chartMessage.visibility = View.GONE
            drawTodayMoodChart(todayMoodEntries)
        }
        
        // Update time range
        chartTimeRange.text = "6 AM - 11 PM"
    }
    
    private fun updateWeeklyChart() {
        chartPlaceholder.text = "📊"
        chartMessage.text = "Weekly mood trends coming soon"
        chartMessage.visibility = View.VISIBLE
        chartPlaceholder.visibility = View.VISIBLE
        chartTimeRange.text = "Last 7 days"
        clearChartData()
    }
    
    private fun updateMonthlyChart() {
        chartPlaceholder.text = "📅"
        chartMessage.text = "Monthly mood trends coming soon"
        chartMessage.visibility = View.VISIBLE
        chartPlaceholder.visibility = View.VISIBLE
        chartTimeRange.text = "Last 30 days"
        clearChartData()
    }
    
    private fun drawTodayMoodChart(moodEntries: List<MoodEntry>) {
        // Clear existing chart data
        clearChartData()
        
        // Sort entries by time
        val sortedEntries = moodEntries.sortedBy { it.dateTime }
        
        // Create time slots (6 AM to 11 PM = 17 hours)
        val timeSlots = mutableListOf<String>()
        val hourLabels = mutableListOf<String>()
        
        for (hour in 6..23) {
            timeSlots.add(String.format("%02d:00", hour))
            hourLabels.add(if (hour <= 12) "${hour}AM" else "${hour-12}PM")
        }
        
        // Create chart container
        val chartLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.BOTTOM
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // Create hour labels container
        val hourLabelsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        // Draw chart bars for each hour
        for (i in timeSlots.indices) {
            val hour = i + 6
            val hourEntries = sortedEntries.filter { 
                val cal = Calendar.getInstance().apply { time = it.dateTime }
                cal.get(Calendar.HOUR_OF_DAY) == hour
            }
            
            // Create bar container
            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                )
            }
            
            // Calculate bar height based on mood entries
            val barHeight = if (hourEntries.isNotEmpty()) {
                val avgMood = hourEntries.map { it.moodLevel }.average()
                (avgMood / 5.0 * 200).toInt() // Scale to 200dp max height
            } else {
                20 // Minimum height for empty slots
            }
            
            // Create mood bar
            val moodBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    barHeight
                )
                background = when {
                    hourEntries.isEmpty() -> getDrawable(R.color.background_color)
                    else -> {
                        val avgMood = hourEntries.map { it.moodLevel }.average()
                        when {
                            avgMood >= 4.5 -> getDrawable(R.color.mood_very_happy)
                            avgMood >= 3.5 -> getDrawable(R.color.mood_happy)
                            avgMood >= 2.5 -> getDrawable(R.color.mood_neutral)
                            avgMood >= 1.5 -> getDrawable(R.color.mood_sad)
                            else -> getDrawable(R.color.mood_very_sad)
                        }
                    }
                }
            }
            
            // Create hour label
            val hourLabel = TextView(this).apply {
                text = if (hour <= 12) "${hour}AM" else "${hour-12}PM"
                textSize = 8f
                setTextColor(getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            barContainer.addView(moodBar)
            barContainer.addView(hourLabel)
            chartLayout.addView(barContainer)
        }
        
        // Add chart to container
        moodChartContainer.addView(chartLayout)
    }
    
    private fun clearChartData() {
        // Remove all views except the placeholder and message
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until moodChartContainer.childCount) {
            val child = moodChartContainer.getChildAt(i)
            if (child != chartPlaceholder && child != chartMessage) {
                viewsToRemove.add(child)
            }
        }
        viewsToRemove.forEach { moodChartContainer.removeView(it) }
    }
    
    private fun updateStats() {
        // Update average mood and total entries in the stats cards
        // We'll find the text views dynamically since they don't have IDs
        
        val totalEntries = moodEntries.size
        val averageMood = if (moodEntries.isNotEmpty()) {
            moodEntries.map { it.moodLevel }.average()
        } else 0.0
        
        // Find and update the stats text views
        // This is a workaround since we don't have specific IDs
        // In a real app, you'd want to add IDs to these views
    }
    
    private fun showMoodEntryDetails(moodEntry: MoodEntry) {
        // Show mood entry details or edit dialog
        // For now, just show a simple message
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(moodEntry.dateTime)
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mood Entry")
            .setMessage("${moodEntry.emoji} ${moodEntry.getMoodDescription()}\n\n$dateString\n\n${moodEntry.note}")
            .setPositiveButton("OK", null)
            .show()
    }
    
}
