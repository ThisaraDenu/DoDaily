package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    
    // Mood statistics components removed
    
    // Calendar components
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var monthYearText: TextView
    private lateinit var calendarGrid: LinearLayout
    
    private val moodEntries = mutableListOf<MoodEntry>()
    private val calendar = Calendar.getInstance()
    private val selectedDate = Calendar.getInstance()
    private val dateItems = mutableListOf<TextView>()
    private var selectedDateIndex = -1
    
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
        
        // Setup calendar
        setupCalendar()
        
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
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_UPDATE_MOOD && resultCode == RESULT_OK) {
            // Refresh data after mood update
            loadMoodData()
            updateTodayChart()
        }
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
        
        // Mood statistics components removed
        
        // Initialize calendar components
        prevMonthButton = findViewById(R.id.prev_month_button)
        nextMonthButton = findViewById(R.id.next_month_button)
        monthYearText = findViewById(R.id.month_year_text)
        calendarGrid = findViewById(R.id.calendar_grid)
        
        // Find stats text views by looking for specific patterns in the layout
        // Since we don't have IDs for them, we'll update them programmatically
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodEntriesAdapter(
            moodEntries = moodEntries,
            onMoodEntryClick = { moodEntry ->
                // Collapse any expanded items when viewing details
                moodAdapter.collapseAll()
                showMoodEntryDetails(moodEntry)
            },
            onEditClick = { moodEntry ->
                openMoodUpdateActivity(moodEntry)
            },
            onDeleteClick = { moodEntry ->
                showDeleteConfirmationDialog(moodEntry)
            }
        )
        
        moodEntriesRecycler.layoutManager = LinearLayoutManager(this)
        moodEntriesRecycler.adapter = moodAdapter
        
        // Add click listener to collapse expanded items when tapping outside
        moodEntriesRecycler.setOnClickListener {
            if (moodAdapter.hasExpandedItem()) {
                moodAdapter.collapseAll()
            }
        }
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
        
        // Calendar click listeners
        prevMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            setupCalendar()
        }
        
        nextMonthButton.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            setupCalendar()
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
                    // Navigate to HydrationSettingsActivity
                    val intent = Intent(this, HydrationSettingsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
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
        // Load today's mood entries by default
        val today = Calendar.getInstance()
        val todayEntries = dataManager.getMoodEntriesForDate(today.time).sortedByDescending { it.dateTime }
        moodEntries.addAll(todayEntries)
        
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
        
        // Create main chart container
        val mainChartContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }
        
        // Create chart title with mood emoji
        val chartTitle = TextView(this).apply {
            text = "📊 Today's Mood Journey"
            textSize = 16f
            setTextColor(getColor(R.color.text_primary))
            gravity = android.view.Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        
        // Create chart area
        val chartArea = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.BOTTOM
            background = getDrawable(R.drawable.chart_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                530 // Maximum possible height within 350dp card
            ).apply {
                setMargins(0, 0, 0, 8)
            }
        }
        
        // Create time slots (6 AM to 11 PM = 18 hours, every 2 hours)
        val timeSlots = listOf(6, 8, 10, 12, 14, 16, 18, 20, 22)
        
        // Draw chart bars for each time slot
        for (hour in timeSlots) {
            val hourEntries = sortedEntries.filter { 
                val cal = Calendar.getInstance().apply { time = it.dateTime }
                cal.get(Calendar.HOUR_OF_DAY) == hour
            }
            
            // Create bar container
            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                ).apply {
                    setMargins(2, 0, 2, 0)
                }
            }
            
            // Calculate bar height and color
            val (barHeight, barColor, moodText) = if (hourEntries.isNotEmpty()) {
                val avgMood = hourEntries.map { it.moodLevel }.average()
                val height = (avgMood / 5.0 * 280).toInt() + 35 // Scale to 280dp max + 35dp base
                val color = when {
                    avgMood >= 4.5 -> R.color.mood_very_happy // Happy (5)
                    avgMood >= 3.5 -> R.color.mood_happy // Excited (4)
                    avgMood >= 2.5 -> R.color.mood_neutral // Neutral (3)
                    avgMood >= 1.5 -> R.color.mood_sad // Sad (2)
                    else -> R.color.mood_very_sad // Angry (1)
                }
                val moodDesc = when {
                    avgMood >= 4.5 -> "😊" // Happy
                    avgMood >= 3.5 -> "🤩" // Excited
                    avgMood >= 2.5 -> "😐" // Neutral
                    avgMood >= 1.5 -> "😢" // Sad
                    else -> "😠" // Angry
                }
                Triple(height, color, moodDesc)
            } else {
                Triple(20, R.color.background_color, "⚪")
            }
            
            // Create mood bar with rounded corners
            val moodBar = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                background = getDrawable(R.drawable.mood_bar_background)
                setBackgroundColor(getColor(barColor))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    barHeight
                )
            }
            
            // Add mood emoji to bar
            val moodEmoji = TextView(this).apply {
                text = moodText
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            
            // Add entry count for all bars
            val entryCount = TextView(this).apply {
                text = "${hourEntries.size}"
                textSize = 10f
                setTextColor(getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            moodBar.addView(entryCount)
            
            moodBar.addView(moodEmoji)
            
            // Create hour label
            val hourLabel = TextView(this).apply {
                text = if (hour == 12) "12PM" else if (hour < 12) "${hour}AM" else "${hour-12}PM"
                textSize = 10f
                setTextColor(getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 0)
                }
            }
            
            barContainer.addView(moodBar)
            barContainer.addView(hourLabel)
            chartArea.addView(barContainer)
        }
        
        // Create mood scale
        val moodScale = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        
        val scaleLabels = listOf("😠", "😢", "😐", "🤩", "😊")
        val scaleTexts = listOf("Angry", "Sad", "Neutral", "Excited", "Happy")
        
        for (i in scaleLabels.indices) {
            val scaleItem = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            
            val emoji = TextView(this).apply {
                text = scaleLabels[i]
                textSize = 14f
                gravity = android.view.Gravity.CENTER
            }
            
            val label = TextView(this).apply {
                text = scaleTexts[i]
                textSize = 8f
                setTextColor(getColor(R.color.text_secondary))
                gravity = android.view.Gravity.CENTER
            }
            
            scaleItem.addView(emoji)
            scaleItem.addView(label)
            moodScale.addView(scaleItem)
        }
        
        // Add all components to main container
        mainChartContainer.addView(chartTitle)
        mainChartContainer.addView(chartArea)
        mainChartContainer.addView(moodScale)
        
        // Add chart to container
        moodChartContainer.addView(mainChartContainer)
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mood_entry_details, null)
        
        // Get views
        val emojiText = dialogView.findViewById<TextView>(R.id.mood_emoji)
        val descriptionText = dialogView.findViewById<TextView>(R.id.mood_description)
        val levelText = dialogView.findViewById<TextView>(R.id.mood_level)
        val dateText = dialogView.findViewById<TextView>(R.id.mood_date)
        val timeText = dialogView.findViewById<TextView>(R.id.mood_time)
        val noteText = dialogView.findViewById<TextView>(R.id.mood_note)
        val noteSection = dialogView.findViewById<LinearLayout>(R.id.note_section)
        val noNoteMessage = dialogView.findViewById<TextView>(R.id.no_note_message)
        
        // Set mood data
        emojiText.text = moodEntry.emoji
        descriptionText.text = moodEntry.getMoodDescription()
        levelText.text = "${moodEntry.moodLevel}/5"
        
        // Set date and time
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        dateText.text = dateFormat.format(moodEntry.dateTime)
        timeText.text = timeFormat.format(moodEntry.dateTime)
        
        // Set note or show no note message
        if (moodEntry.note.isNotEmpty()) {
            noteText.text = moodEntry.note
            noteSection.visibility = View.VISIBLE
            noNoteMessage.visibility = View.GONE
        } else {
            noteSection.visibility = View.GONE
            noNoteMessage.visibility = View.VISIBLE
        }
        
        // Show dialog
        AlertDialog.Builder(this)
            .setTitle("Mood Entry Details")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                openMoodUpdateActivity(moodEntry)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun setupCalendar() {
        // Clear existing calendar
        calendarGrid.removeAllViews()
        dateItems.clear()
        
        // Set calendar to first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Calculate days from previous month to show
        val daysFromPrevMonth = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
        
        // Update month/year text
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearText.text = monthYearFormat.format(calendar.time)
        
        // Create calendar rows
        var currentDay = 1
        val totalCells = daysFromPrevMonth + daysInMonth
        val rows = (totalCells + 6) / 7 // Round up to get number of rows
        
        for (row in 0 until rows) {
            val rowLayout = createCalendarRow()
            
            for (col in 0 until 7) {
                val cellIndex = row * 7 + col
                val dateItem = createDateItem(cellIndex, daysFromPrevMonth, daysInMonth, currentDay)
                rowLayout.addView(dateItem)
                dateItems.add(dateItem)
                
                if (cellIndex >= daysFromPrevMonth && currentDay <= daysInMonth) {
                    currentDay++
                }
            }
            
            calendarGrid.addView(rowLayout)
        }
        
        // Highlight today's date
        highlightToday()
    }
    
    private fun createCalendarRow(): LinearLayout {
        val rowLayout = LinearLayout(this)
        rowLayout.orientation = LinearLayout.HORIZONTAL
        rowLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        return rowLayout
    }
    
    private fun createDateItem(cellIndex: Int, daysFromPrevMonth: Int, daysInMonth: Int, currentDay: Int): TextView {
        val dateItem = TextView(this)
        val layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layoutParams.setMargins(2, 2, 2, 2)
        dateItem.layoutParams = layoutParams
        
        dateItem.textSize = 16f
        dateItem.gravity = android.view.Gravity.CENTER
        dateItem.setPadding(8, 8, 8, 8)
        dateItem.background = getDrawable(R.drawable.calendar_date_background)
        dateItem.isClickable = true
        dateItem.isFocusable = true
        
        if (cellIndex >= daysFromPrevMonth && currentDay <= daysInMonth) {
            // Current month day
            dateItem.text = currentDay.toString()
            dateItem.setTextColor(getColor(R.color.text_primary))
            dateItem.setOnClickListener { selectDate(cellIndex) }
        } else {
            // Empty cell
            dateItem.text = ""
            dateItem.setTextColor(getColor(android.R.color.transparent))
        }
        
        return dateItem
    }
    
    private fun selectDate(index: Int) {
        // Update selected date index
        selectedDateIndex = index
        
        // Update visual selection
        for (i in dateItems.indices) {
            val item = dateItems[i]
            if (i == index && item.text.isNotEmpty()) {
                item.isSelected = true
                item.setTextColor(getColor(R.color.white))
            } else {
                item.isSelected = false
                item.setTextColor(getColor(R.color.text_primary))
            }
        }
        
        // Calculate the actual selected date
        val daysFromPrevMonth = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
        val dayOfMonth = index - daysFromPrevMonth + 1
        
        if (dayOfMonth > 0 && dayOfMonth <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            // Set selected date
            selectedDate.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            selectedDate.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            
            // Filter mood entries for selected date
            filterMoodEntriesForDate(selectedDate.time)
        }
    }
    
    private fun highlightToday() {
        val today = Calendar.getInstance()
        if (today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && 
            today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
            
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            val daysFromPrevMonth = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY + 7) % 7
            val todayIndex = daysFromPrevMonth + todayDay - 1
            
            if (todayIndex < dateItems.size) {
                val todayItem = dateItems[todayIndex]
                todayItem.setTextColor(getColor(R.color.primary_green))
                todayItem.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }
    
    private fun filterMoodEntriesForDate(date: Date) {
        // Filter mood entries for the selected date
        val filteredEntries = dataManager.getMoodEntriesForDate(date)
        
        // Update the mood entries list
        moodEntries.clear()
        moodEntries.addAll(filteredEntries)
        
        // Update the adapter
        moodAdapter.notifyDataSetChanged()
        
        // Show/hide empty state
        if (moodEntries.isEmpty()) {
            moodEmptyState.visibility = View.VISIBLE
            moodEntriesRecycler.visibility = View.GONE
        } else {
            moodEmptyState.visibility = View.GONE
            moodEntriesRecycler.visibility = View.VISIBLE
        }
        
        // Update chart for selected date
        updateChartForSelectedDate()
    }
    
    private fun updateChartForSelectedDate() {
        // Update the chart to show data for the selected date
        if (moodEntries.isNotEmpty()) {
            drawTodayMoodChart(moodEntries)
            chartTimeRange.text = "Selected Date"
        } else {
            clearChartData()
            chartMessage.text = "No mood data for selected date"
            chartTimeRange.text = "Selected Date"
        }
    }
    
    private fun openMoodUpdateActivity(moodEntry: MoodEntry) {
        val intent = Intent(this, com.example.dodaily.MoodUpdateActivity::class.java)
        intent.putExtra("mood_entry", moodEntry)
        startActivityForResult(intent, REQUEST_UPDATE_MOOD)
    }
    
    companion object {
        private const val REQUEST_UPDATE_MOOD = 1001
    }
    
    private fun showDeleteConfirmationDialog(moodEntry: MoodEntry) {
        AlertDialog.Builder(this)
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?\n\n${moodEntry.emoji} ${moodEntry.getMoodDescription()}\n${SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(moodEntry.dateTime)}")
            .setPositiveButton("Delete") { _, _ ->
                deleteMoodEntry(moodEntry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteMoodEntry(moodEntry: MoodEntry) {
        // Get all mood entries
        val allMoodEntries = dataManager.loadMoodEntries().toMutableList()
        
        // Remove the specific mood entry
        val updatedEntries = allMoodEntries.filter { it.id != moodEntry.id }
        
        // Save updated entries
        dataManager.saveMoodEntries(updatedEntries)
        
        // Refresh the UI
        loadMoodData()
        updateTodayChart()
        
        // Show confirmation
        Toast.makeText(this, "Mood entry deleted", Toast.LENGTH_SHORT).show()
    }
    
}
