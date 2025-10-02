package com.example.dodaily

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodLoggingActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private var selectedMood: Int = -1
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    
    private lateinit var moodCards: List<CardView>
    private lateinit var monthYearText: TextView
    private lateinit var timeDisplay: TextView
    private lateinit var hourDisplay: TextView
    private lateinit var minuteDisplay: TextView
    private lateinit var amButton: TextView
    private lateinit var pmButton: TextView
    private lateinit var calendarWidget: View
    private lateinit var calendarGrid: LinearLayout
    private var selectedDateIndex: Int = 0
    private var dateItems: MutableList<TextView> = mutableListOf()
    private var isAM: Boolean = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Initialize views
        initializeViews()
        setupMoodSelection()
        setupTimeSelection()
        setupSaveButton()
        setupNavigation()
        setupCalendar()
        
        // Set initial time display
        updateTimeDisplays()
        updateMonthYearDisplay()
    }
    
    private fun initializeViews() {
        moodCards = listOf(
            findViewById(R.id.mood_happy),
            findViewById(R.id.mood_excited),
            findViewById(R.id.mood_neutral),
            findViewById(R.id.mood_sad),
            findViewById(R.id.mood_angry)
        )
        
        monthYearText = findViewById(R.id.month_year_text)
        timeDisplay = findViewById(R.id.time_display)
        hourDisplay = findViewById(R.id.hour_display)
        minuteDisplay = findViewById(R.id.minute_display)
        amButton = findViewById(R.id.am_button)
        pmButton = findViewById(R.id.pm_button)
        calendarWidget = findViewById(R.id.calendar_widget)
        calendarGrid = calendarWidget.findViewById(R.id.calendar_grid)
    }
    
    private fun setupMoodSelection() {
        moodCards.forEachIndexed { index, card ->
            card.setOnClickListener {
                selectMood(index)
            }
        }
    }
    
    private fun selectMood(moodIndex: Int) {
        selectedMood = moodIndex
        moodCards.forEachIndexed { index, card ->
            card.isSelected = (index == moodIndex)
            
            // Update text colors based on selection
            val linearLayout = card.getChildAt(0) as? LinearLayout
            val textView = linearLayout?.getChildAt(1) as? TextView
            textView?.setTextColor(
                if (index == moodIndex) {
                    resources.getColor(android.R.color.white, null)
                } else {
                    resources.getColor(R.color.text_primary, null)
                }
            )
        }
    }
    
    private fun setupTimeSelection() {
        // Hour controls
        findViewById<View>(R.id.decrease_hour_button).setOnClickListener {
            val currentHour = selectedTime.get(Calendar.HOUR)
            val newHour = if (currentHour == 1) 12 else currentHour - 1
            selectedTime.set(Calendar.HOUR, newHour)
            updateTimeDisplays()
        }
        
        findViewById<View>(R.id.increase_hour_button).setOnClickListener {
            val currentHour = selectedTime.get(Calendar.HOUR)
            val newHour = if (currentHour == 12) 1 else currentHour + 1
            selectedTime.set(Calendar.HOUR, newHour)
            updateTimeDisplays()
        }
        
        // Minute controls
        findViewById<View>(R.id.decrease_minute_button).setOnClickListener {
            val currentMinute = selectedTime.get(Calendar.MINUTE)
            val newMinute = if (currentMinute == 0) 59 else currentMinute - 1
            selectedTime.set(Calendar.MINUTE, newMinute)
            updateTimeDisplays()
        }
        
        findViewById<View>(R.id.increase_minute_button).setOnClickListener {
            val currentMinute = selectedTime.get(Calendar.MINUTE)
            val newMinute = if (currentMinute == 59) 0 else currentMinute + 1
            selectedTime.set(Calendar.MINUTE, newMinute)
            updateTimeDisplays()
        }
        
        // AM/PM toggle
        amButton.setOnClickListener {
            isAM = true
            selectedTime.set(Calendar.AM_PM, Calendar.AM)
            updateAMPMButtons()
            updateTimeDisplays()
        }
        
        pmButton.setOnClickListener {
            isAM = false
            selectedTime.set(Calendar.AM_PM, Calendar.PM)
            updateAMPMButtons()
            updateTimeDisplays()
        }
        
        // Calendar navigation
        calendarWidget.findViewById<View>(R.id.prev_month_button).setOnClickListener {
            selectedDate.add(Calendar.MONTH, -1)
            updateMonthYearDisplay()
            setupCalendar()
        }
        
        calendarWidget.findViewById<View>(R.id.next_month_button).setOnClickListener {
            selectedDate.add(Calendar.MONTH, 1)
            updateMonthYearDisplay()
            setupCalendar()
        }
        
        // Initialize AM/PM state
        isAM = selectedTime.get(Calendar.AM_PM) == Calendar.AM
        updateAMPMButtons()
    }
    
    private fun setupSaveButton() {
        findViewById<View>(R.id.save_button).setOnClickListener {
            saveMoodEntry()
        }
    }
    
    private fun setupNavigation() {
        findViewById<View>(R.id.back_button).setOnClickListener {
            finish()
        }
    }
    
    private fun updateTimeDisplay() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        timeDisplay.text = timeFormat.format(selectedTime.time)
    }
    
    private fun updateTimeDisplays() {
        val hour = selectedTime.get(Calendar.HOUR)
        val minute = selectedTime.get(Calendar.MINUTE)
        
        hourDisplay.text = hour.toString()
        minuteDisplay.text = String.format("%02d", minute)
        updateTimeDisplay()
    }
    
    private fun updateAMPMButtons() {
        if (isAM) {
            amButton.setTextColor(resources.getColor(R.color.white, null))
            amButton.setBackgroundResource(R.drawable.am_pm_selected_background)
            pmButton.setTextColor(resources.getColor(R.color.text_primary, null))
            pmButton.setBackgroundResource(R.drawable.am_pm_unselected_background)
        } else {
            amButton.setTextColor(resources.getColor(R.color.text_primary, null))
            amButton.setBackgroundResource(R.drawable.am_pm_unselected_background)
            pmButton.setTextColor(resources.getColor(R.color.white, null))
            pmButton.setBackgroundResource(R.drawable.am_pm_selected_background)
        }
    }
    
    private fun updateMonthYearDisplay() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearText.text = monthFormat.format(selectedDate.time)
    }
    
    private fun setupCalendar() {
        // Clear existing calendar
        calendarGrid.removeAllViews()
        dateItems.clear()
        
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.time
        
        // Set to first day of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Get the first day of the month and number of days
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Calculate how many days from previous month to show
        val daysFromPrevMonth = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - Calendar.SUNDAY
        
        // Create calendar rows
        var currentRow: LinearLayout? = null
        var dayCount = 0
        
        // Add empty cells for days from previous month
        for (i in 0 until daysFromPrevMonth) {
            if (dayCount % 7 == 0) {
                currentRow = createCalendarRow()
                calendarGrid.addView(currentRow)
            }
            
            val emptyDate = createDateItem(null, false)
            currentRow?.addView(emptyDate)
            dayCount++
        }
        
        // Add days of current month
        for (day in 1..daysInMonth) {
            if (dayCount % 7 == 0) {
                currentRow = createCalendarRow()
                calendarGrid.addView(currentRow)
            }
            
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = selectedDate.time
            dateCalendar.set(Calendar.DAY_OF_MONTH, day)
            
            val isToday = isSameDay(dateCalendar, Calendar.getInstance())
            val dateItem = createDateItem(dateCalendar, true)
            dateItems.add(dateItem)
            currentRow?.addView(dateItem)
            
            if (isToday) {
                selectedDateIndex = dateItems.size - 1
                selectDate(selectedDateIndex)
            }
            
            dayCount++
        }
        
        // Fill remaining cells in last row if needed
        while (dayCount % 7 != 0) {
            val emptyDate = createDateItem(null, false)
            currentRow?.addView(emptyDate)
            dayCount++
        }
    }
    
    private fun createCalendarRow(): LinearLayout {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        return row
    }
    
    private fun createDateItem(calendar: Calendar?, isClickable: Boolean): TextView {
        val inflater = LayoutInflater.from(this)
        val dateItem = inflater.inflate(R.layout.item_calendar_date, calendarGrid, false) as TextView
        
        if (calendar != null && isClickable) {
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            dateItem.text = dayOfMonth.toString()
            dateItem.setTextColor(resources.getColor(R.color.text_primary, null))
            
            // Set click listener
            dateItem.setOnClickListener {
                val index = dateItems.indexOf(dateItem)
                selectDate(index)
            }
        } else {
            dateItem.text = ""
            dateItem.setTextColor(resources.getColor(android.R.color.transparent, null))
            dateItem.isClickable = false
        }
        
        return dateItem
    }
    
    private fun selectDate(index: Int) {
        if (index < 0 || index >= dateItems.size) return
        
        selectedDateIndex = index
        
        // Update visual selection
        dateItems.forEachIndexed { i, item ->
            if (i == index) {
                // Selected state
                item.isSelected = true
                item.setTextColor(resources.getColor(android.R.color.white, null))
            } else {
                // Unselected state
                item.isSelected = false
                item.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
        
        // Update selected date - we need to calculate the actual date from the index
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Get the first day of the month
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromPrevMonth = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - Calendar.SUNDAY
        
        // Calculate the actual day of month
        val actualDay = index - daysFromPrevMonth + 1
        if (actualDay > 0 && actualDay <= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            calendar.set(Calendar.DAY_OF_MONTH, actualDay)
            selectedDate.time = calendar.time
        }
    }
    
    private fun saveMoodEntry() {
        if (selectedMood == -1) {
            // Show error message
            return
        }
        
        // Check daily limit (10 mood entries per day)
        val todayMoodEntries = dataManager.getMoodEntriesForDate(selectedTime.time)
        if (todayMoodEntries.size >= 10) {
            // Show limit reached message
            showLimitReachedMessage()
            return
        }
        
        val moodLevels = listOf(5, 4, 3, 2, 1) // Happy, Excited, Neutral, Sad, Angry
        val moodEmojis = listOf("😊", "🤩", "😐", "😢", "😠")
        
        val moodEntry = MoodEntry(
            emoji = moodEmojis[selectedMood],
            moodLevel = moodLevels[selectedMood],
            note = "",
            dateTime = selectedTime.time
        )
        
        dataManager.addMoodEntry(moodEntry)
        
        // Show success message and finish
        finish()
    }
    
    private fun showLimitReachedMessage() {
        // You can implement a custom dialog or toast here
        // For now, we'll use a simple approach
        android.widget.Toast.makeText(
            this,
            "Daily limit reached! You can only add 10 mood entries per day.",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
    
    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}