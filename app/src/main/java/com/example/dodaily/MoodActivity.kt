package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.adapters.MoodEntriesAdapter
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class MoodActivity : AppCompatActivity() {
    
    private lateinit var dataManager: DataManager
    private lateinit var backButton: ImageButton
    private lateinit var moodEntriesRecycler: RecyclerView
    private lateinit var moodEmptyState: LinearLayout
    private lateinit var moodAdapter: MoodEntriesAdapter
    private lateinit var avgMoodText: TextView
    private lateinit var totalEntriesText: TextView
    
    private val moodEntries = mutableListOf<MoodEntry>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        
        // Load mood data
        loadMoodData()
    }
    
    private fun initializeViews() {
        backButton = findViewById(R.id.back_button)
        moodEntriesRecycler = findViewById(R.id.mood_entries_recycler)
        moodEmptyState = findViewById(R.id.mood_empty_state)
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodEntriesAdapter(
            moodEntries = moodEntries,
            onMoodEntryClick = { moodEntry ->
                // Handle mood entry click - show details
                showMoodEntryDetails(moodEntry)
            },
            onEditClick = { moodEntry ->
                openMoodUpdateActivity(moodEntry)
            },
            onDeleteClick = { moodEntry ->
                // Handle delete click - show confirmation dialog
                showDeleteConfirmationDialog(moodEntry)
            }
        )
        
        moodEntriesRecycler.layoutManager = LinearLayoutManager(this)
        moodEntriesRecycler.adapter = moodAdapter
    }
    
    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadMoodData() {
        moodEntries.clear()
        val allMoodEntries = dataManager.loadMoodEntries().sortedByDescending { it.dateTime }
        moodEntries.addAll(allMoodEntries)
        
        moodAdapter.notifyDataSetChanged()
        updateUI()
        updateStats()
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
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Mood Entry Details")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                openMoodUpdateActivity(moodEntry)
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh mood data when returning from MoodLoggingActivity
        loadMoodData()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_UPDATE_MOOD && resultCode == RESULT_OK) {
            // Refresh data after mood update
            loadMoodData()
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
        androidx.appcompat.app.AlertDialog.Builder(this)
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
        
        // Show confirmation
        android.widget.Toast.makeText(this, "Mood entry deleted", android.widget.Toast.LENGTH_SHORT).show()
    }
}
