package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private lateinit var moodCards: List<CardView>
    private var selectedMoodLevel = 3 // Default to neutral
    
    // Time selection
    private lateinit var timeDisplay: TextView
    private lateinit var hourDisplay: TextView
    private lateinit var minuteDisplay: TextView
    private lateinit var decreaseHourButton: ImageButton
    private lateinit var increaseHourButton: ImageButton
    private lateinit var decreaseMinuteButton: ImageButton
    private lateinit var increaseMinuteButton: ImageButton
    
    private var currentHour = 10
    private var currentMinute = 30
    
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
        
        // Mood cards
        moodCards = listOf(
            findViewById(R.id.mood_happy),
            findViewById(R.id.mood_excited),
            findViewById(R.id.mood_neutral),
            findViewById(R.id.mood_sad),
            findViewById(R.id.mood_angry)
        )
        
        // Time controls
        timeDisplay = findViewById(R.id.time_display)
        hourDisplay = findViewById(R.id.hour_display)
        minuteDisplay = findViewById(R.id.minute_display)
        decreaseHourButton = findViewById(R.id.decrease_hour_button)
        increaseHourButton = findViewById(R.id.increase_hour_button)
        decreaseMinuteButton = findViewById(R.id.decrease_minute_button)
        increaseMinuteButton = findViewById(R.id.increase_minute_button)
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
        
        updateButton.setOnClickListener {
            updateMoodEntry()
        }
        
        // Mood card selection
        moodCards.forEachIndexed { index, cardView ->
            cardView.setOnClickListener {
                selectMoodCard(index)
            }
        }
        
        // Time controls
        decreaseHourButton.setOnClickListener { changeHour(-1) }
        increaseHourButton.setOnClickListener { changeHour(1) }
        decreaseMinuteButton.setOnClickListener { changeMinute(-1) }
        increaseMinuteButton.setOnClickListener { changeMinute(1) }
    }
    
    private fun prefillData() {
        // Pre-fill note
        noteEditText.setText(moodEntry.note)
        
        // Pre-select mood
        val moodIndex = moodLevels.indexOf(moodEntry.moodLevel)
        if (moodIndex >= 0) {
            selectMoodCard(moodIndex)
        }
        
        // Pre-fill time
        val calendar = Calendar.getInstance()
        calendar.time = moodEntry.dateTime
        currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        currentMinute = calendar.get(Calendar.MINUTE)
        
        updateTimeDisplay()
    }
    
    private fun selectMoodCard(index: Int) {
        // Reset all cards
        moodCards.forEach { it.isSelected = false }
        
        // Select current card
        moodCards[index].isSelected = true
        selectedMoodLevel = moodLevels[index]
    }
    
    private fun changeHour(delta: Int) {
        currentHour += delta
        if (currentHour < 0) currentHour = 23
        else if (currentHour > 23) currentHour = 0
        updateTimeDisplay()
    }
    
    private fun changeMinute(delta: Int) {
        currentMinute += delta
        if (currentMinute < 0) currentMinute = 59
        else if (currentMinute > 59) currentMinute = 0
        updateTimeDisplay()
    }
    
    private fun updateTimeDisplay() {
        val hourText = if (currentHour < 10) "0$currentHour" else currentHour.toString()
        val minuteText = if (currentMinute < 10) "0$currentMinute" else currentMinute.toString()
        
        timeDisplay.text = "$hourText:$minuteText"
        hourDisplay.text = hourText
        minuteDisplay.text = minuteText
    }
    
    private fun updateMoodEntry() {
        val note = noteEditText.text.toString().trim()
        
        // Create new date with updated time (already in 24-hour format)
        val calendar = Calendar.getInstance()
        calendar.time = moodEntry.dateTime
        calendar.set(Calendar.HOUR_OF_DAY, currentHour)
        calendar.set(Calendar.MINUTE, currentMinute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val updatedMoodEntry = moodEntry.copy(
            emoji = moodEmojis[selectedMoodLevel - 1],
            note = note,
            moodLevel = selectedMoodLevel,
            dateTime = calendar.time
        )
        
        dataManager.updateMoodEntry(updatedMoodEntry)
        
        Toast.makeText(this, "Mood entry updated", Toast.LENGTH_SHORT).show()
        
        // Return to previous activity
        setResult(RESULT_OK)
        finish()
    }
}