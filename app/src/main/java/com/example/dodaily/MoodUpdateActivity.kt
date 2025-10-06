package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.adapters.TimePickerAdapter
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodUpdateActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private lateinit var backButton: ImageButton
    private lateinit var updateButton: Button
    private lateinit var noteEditText: EditText
    
    // Mood selection
    private lateinit var moodButtons: List<Button>
    private var selectedMoodLevel = 3 // Default to neutral
    
    // Time selection
    private lateinit var timeDisplay: TextView
    private lateinit var hourPicker: RecyclerView
    private lateinit var ampmPicker: RecyclerView
    private lateinit var hourAdapter: TimePickerAdapter
    private lateinit var ampmAdapter: TimePickerAdapter
    
    // Available times for mood tracking (6AM to 12AM in 2-hour intervals)
    private val availableHours = listOf("6", "8", "10", "12", "2", "4", "6", "8", "10", "12")
    private val ampmOptions = listOf("AM", "PM")
    
    private var selectedTime: Calendar = Calendar.getInstance()
    
    // Mood data
    private lateinit var moodEntry: MoodEntry
    private val moodEmojis = listOf("😊", "🤩", "😐", "😢", "😠") // Happy, Excited, Neutral, Sad, Angry
    private val moodLevels = listOf(5, 4, 3, 2, 1) // Happy, Excited, Neutral, Sad, Angry
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_update)
        
        // Enable edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize DataManager
        dataManager = DataManager(this)
        
        // Get mood entry from intent
        moodEntry = intent.getSerializableExtra("mood_entry") as? MoodEntry ?: return
        
        // Initialize views
        initializeViews()
        
        // Setup click listeners
        setupClickListeners()
        
        // Pre-fill with existing data
        prefillData()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        updateButton = findViewById(R.id.update_button)
        noteEditText = findViewById(R.id.mood_note_edit)
        
        // Mood buttons
        moodButtons = listOf(
            findViewById(R.id.mood_happy),
            findViewById(R.id.mood_excited),
            findViewById(R.id.mood_neutral),
            findViewById(R.id.mood_sad),
            findViewById(R.id.mood_angry)
        )
        
        // Time controls
        timeDisplay = findViewById(R.id.time_display)
        hourPicker = findViewById(R.id.hour_picker)
        ampmPicker = findViewById(R.id.ampm_picker)
        
        setupTimePickers()
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
        
        // Set initial selection based on mood entry time
        val hour = selectedTime.get(Calendar.HOUR_OF_DAY)
        val ampm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        
        val hourString = displayHour.toString()
        val availableHourIndex = availableHours.indexOf(hourString)
        if (availableHourIndex != -1) {
            hourAdapter.setSelectedTime(hourString)
        }
        ampmAdapter.setSelectedTime(ampm)
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
        
        updateTimeDisplay()
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        updateButton.setOnClickListener {
            updateMoodEntry()
        }
        
        // Mood button selection
        moodButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectMoodButton(index)
            }
        }
        
    }
    
    private fun prefillData() {
        // Pre-fill note
        noteEditText.setText(moodEntry.note)
        
        // Pre-select mood
        val moodIndex = moodLevels.indexOf(moodEntry.moodLevel)
        if (moodIndex >= 0) {
            selectMoodButton(moodIndex)
        }
        
        // Pre-fill time
        selectedTime.time = moodEntry.dateTime
        updateTimeDisplay()
    }
    
    private fun selectMoodButton(index: Int) {
        // Reset all buttons
        moodButtons.forEach { it.isSelected = false }
        
        // Select current button
        moodButtons[index].isSelected = true
        selectedMoodLevel = moodLevels[index]
    }
    
    
    private fun updateTimeDisplay() {
        val hour = selectedTime.get(Calendar.HOUR_OF_DAY)
        val minute = selectedTime.get(Calendar.MINUTE)
        
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val ampm = if (hour < 12) "AM" else "PM"
        
        timeDisplay.text = "$displayHour $ampm"
    }
    
    private fun updateMoodEntry() {
        val note = noteEditText.text.toString().trim()
        
        val updatedMoodEntry = moodEntry.copy(
            emoji = moodEmojis[moodLevels.indexOf(selectedMoodLevel)],
            note = note,
            moodLevel = selectedMoodLevel,
            dateTime = selectedTime.time
        )
        
        dataManager.updateMoodEntry(updatedMoodEntry)
        
        Toast.makeText(this, "Mood entry updated", Toast.LENGTH_SHORT).show()
        
        // Return to previous activity
        setResult(RESULT_OK)
        finish()
    }
}