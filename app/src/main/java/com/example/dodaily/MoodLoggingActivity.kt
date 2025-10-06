package com.example.dodaily

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.adapters.TimePickerAdapter
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import java.util.*

class MoodLoggingActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private var selectedMood: Int = -1
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    
    private lateinit var moodButtons: List<Button>
    private lateinit var timeDisplay: TextView
    private lateinit var hourPicker: RecyclerView
    private lateinit var ampmPicker: RecyclerView
    private lateinit var hourAdapter: TimePickerAdapter
    private lateinit var ampmAdapter: TimePickerAdapter
    
    // Available times for mood tracking (6AM to 12AM in 2-hour intervals)
    private val availableHours = listOf("6", "8", "10", "12", "2", "4", "6", "8", "10", "12")
    private val ampmOptions = listOf("AM", "PM")
    
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
        
        // Get selected date from intent
        val selectedDateMillis = intent.getLongExtra("selected_date", -1L)
        if (selectedDateMillis != -1L) {
            selectedDate.timeInMillis = selectedDateMillis
        }
        
        // Initialize views
        initializeViews()
        setupMoodSelection()
        setupTimeSelection()
        setupSaveButton()
        setupNavigation()
        
        // Set initial time display
        updateTimeDisplays()
    }
    
    private fun initializeViews() {
        moodButtons = listOf(
            findViewById(R.id.mood_happy),
            findViewById(R.id.mood_excited),
            findViewById(R.id.mood_neutral),
            findViewById(R.id.mood_sad),
            findViewById(R.id.mood_angry)
        )
        
        timeDisplay = findViewById(R.id.time_display)
        
        // Initialize time pickers
        hourPicker = findViewById(R.id.hour_picker)
        ampmPicker = findViewById(R.id.ampm_picker)
        
        setupTimePickers()
    }
    
    private fun setupMoodSelection() {
        moodButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectMood(index)
            }
        }
    }
    
    private fun selectMood(moodIndex: Int) {
        selectedMood = moodIndex
        moodButtons.forEachIndexed { index, button ->
            button.isSelected = (index == moodIndex)
        }
    }
    
    private fun setupTimePickers() {
        // Setup hour picker
        hourAdapter = TimePickerAdapter(availableHours) { selectedHour ->
            updateSelectedTime(selectedHour, null)
        }
        hourPicker.layoutManager = LinearLayoutManager(this)
        hourPicker.adapter = hourAdapter
        
        // Setup AM/PM picker
        ampmAdapter = TimePickerAdapter(ampmOptions) { selectedAmPm ->
            updateSelectedTime(null, selectedAmPm)
        }
        ampmPicker.layoutManager = LinearLayoutManager(this)
        ampmPicker.adapter = ampmAdapter
        
        // Set initial selection
        val currentHour = selectedTime.get(Calendar.HOUR_OF_DAY)
        val currentAmPm = if (currentHour < 12) "AM" else "PM"
        val displayHour = if (currentHour == 0) 12 else if (currentHour > 12) currentHour - 12 else currentHour
        
        val hourString = displayHour.toString()
        val availableHourIndex = availableHours.indexOf(hourString)
        if (availableHourIndex != -1) {
            hourAdapter.setSelectedTime(hourString)
        }
        ampmAdapter.setSelectedTime(currentAmPm)
    }
    
    private fun updateSelectedTime(selectedHour: String?, selectedAmPm: String?) {
        val currentHour = selectedTime.get(Calendar.HOUR_OF_DAY)
        val currentAmPm = if (currentHour < 12) "AM" else "PM"
        val displayHour = if (currentHour == 0) 12 else if (currentHour > 12) currentHour - 12 else currentHour
        
        val hour = selectedHour ?: displayHour.toString()
        val ampm = selectedAmPm ?: currentAmPm
        
        val hour24 = when {
            hour == "12" && ampm == "AM" -> 0
            hour == "12" && ampm == "PM" -> 12
            ampm == "AM" -> hour.toInt()
            else -> hour.toInt() + 12
        }
        
        selectedTime.set(Calendar.HOUR_OF_DAY, hour24)
        selectedTime.set(Calendar.MINUTE, 0) // Always set minutes to 0
        
        updateTimeDisplays()
    }
    
    private fun setupTimeSelection() {
        // Time selection is handled by the time pickers
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
        val hour = selectedTime.get(Calendar.HOUR_OF_DAY)
        val minute = selectedTime.get(Calendar.MINUTE)
        
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val ampm = if (hour < 12) "AM" else "PM"
        
        timeDisplay.text = "$displayHour $ampm"
    }
    
    private fun updateTimeDisplays() {
        updateTimeDisplay()
    }
    
    private fun saveMoodEntry() {
        if (selectedMood == -1) {
            // Show error message
            return
        }
        
        val note = findViewById<TextView>(R.id.mood_note_edit).text.toString()
        
        // Combine selected date with selected time
        val finalDateTime = Calendar.getInstance().apply {
            // Set the date from selectedDate
            set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            
            // Set the time from selectedTime
            set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Create mood entry with selected date and time
        val moodEntry = MoodEntry(
            id = UUID.randomUUID().toString(),
            moodLevel = getMoodLevel(selectedMood), // Convert button index to correct mood level
            emoji = getMoodEmoji(selectedMood),
            note = note,
            dateTime = finalDateTime.time
        )
        
        // Save to DataManager
        val existingEntries = dataManager.loadMoodEntries().toMutableList()
        existingEntries.add(moodEntry)
        dataManager.saveMoodEntries(existingEntries)
        
        // Set result and finish activity
        setResult(RESULT_OK)
        finish()
    }
    
    private fun getMoodLevel(buttonIndex: Int): Int {
        // Map button indices to correct mood levels
        // Button order: Happy(0), Excited(1), Neutral(2), Sad(3), Angry(4)
        // Mood levels: Happy(5), Excited(4), Neutral(3), Sad(2), Angry(1)
        return when (buttonIndex) {
            0 -> 5 // Happy
            1 -> 4 // Excited
            2 -> 3 // Neutral
            3 -> 2 // Sad
            4 -> 1 // Angry
            else -> 3 // Default to Neutral
        }
    }
    
    private fun getMoodEmoji(moodIndex: Int): String {
        val emojis = listOf("😊", "🤩", "😐", "😢", "😠")
        return if (moodIndex in emojis.indices) emojis[moodIndex] else "😐"
    }
}