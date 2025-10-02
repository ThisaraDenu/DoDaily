package com.example.dodaily

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
        moodAdapter = MoodEntriesAdapter(moodEntries) { moodEntry ->
            // Handle mood entry click - could open edit dialog
            showMoodEntryDetails(moodEntry)
        }
        
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
    
    override fun onResume() {
        super.onResume()
        // Refresh mood data when returning from MoodLoggingActivity
        loadMoodData()
    }
}
