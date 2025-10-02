package com.example.dodaily.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.MoodActivity
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import com.example.dodaily.adapters.MoodEntriesAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for mood journaling
 * Allows users to log moods with emojis and view mood history
 */
class MoodJournalFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodEntriesAdapter
    private lateinit var addMoodFab: FloatingActionButton
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var moodChartButton: Button
    private lateinit var shareButton: Button
    
    private val moodEntries = mutableListOf<MoodEntry>()
    private val moodEmojis = listOf("😊", "🤩", "😐", "😢", "😠") // Happy, Excited, Neutral, Sad, Angry
    private val moodLevels = listOf(5, 4, 3, 2, 1) // Happy, Excited, Neutral, Sad, Angry
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_journal, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize DataManager
        dataManager = (activity as? com.example.dodaily.HomeActivity)?.getDataManager() ?: return
        
        // Initialize views
        moodRecyclerView = view.findViewById(R.id.mood_recycler_view)
        addMoodFab = view.findViewById(R.id.add_mood_fab)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        emptyStateText = view.findViewById(R.id.empty_state_text)
        moodChartButton = view.findViewById(R.id.mood_chart_button)
        shareButton = view.findViewById(R.id.share_button)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup FAB
        addMoodFab.setOnClickListener {
            val intent = Intent(requireContext(), MoodActivity::class.java)
            startActivity(intent)
        }
        
        // Setup chart button
        moodChartButton.setOnClickListener {
            showMoodChart()
        }
        
        // Setup share button
        shareButton.setOnClickListener {
            shareMoodSummary()
        }
        
        // Load mood entries
        loadMoodEntries()
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodEntriesAdapter(moodEntries) { moodEntry ->
            showEditMoodDialog(moodEntry)
        }
        
        moodRecyclerView.layoutManager = LinearLayoutManager(context)
        moodRecyclerView.adapter = moodAdapter
    }
    
    private fun loadMoodEntries() {
        moodEntries.clear()
        moodEntries.addAll(dataManager.loadMoodEntries().sortedByDescending { it.dateTime })
        moodAdapter.notifyDataSetChanged()
        updateUI()
    }
    
    private fun updateUI() {
        if (moodEntries.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            moodRecyclerView.visibility = View.GONE
            moodChartButton.visibility = View.GONE
            shareButton.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            moodRecyclerView.visibility = View.VISIBLE
            moodChartButton.visibility = View.VISIBLE
            shareButton.visibility = View.VISIBLE
        }
    }
    
    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.activity_mood, null)
        val emojiButtons = listOf(
            dialogView.findViewById<Button>(R.id.mood_happy),
            dialogView.findViewById<Button>(R.id.mood_excited),
            dialogView.findViewById<Button>(R.id.mood_neutral),
            dialogView.findViewById<Button>(R.id.mood_sad),
            dialogView.findViewById<Button>(R.id.mood_angry)
        )
        val noteEditText = dialogView.findViewById<EditText>(R.id.mood_note_edit)
        
        var selectedMoodLevel = 3 // Default to neutral
        
        // Setup emoji buttons
        emojiButtons.forEachIndexed { index, button ->
            button.text = moodEmojis[index]
            button.setOnClickListener {
                // Reset all buttons
                emojiButtons.forEach { it.isSelected = false }
                // Select current button
                button.isSelected = true
                selectedMoodLevel = moodLevels[index]
            }
        }
        
        // Select neutral mood by default (index 2 in the array)
        emojiButtons[2].isSelected = true
        
        AlertDialog.Builder(requireContext())
            .setTitle("How are you feeling?")
            .setView(dialogView)
            .setPositiveButton("Save") { _: android.content.DialogInterface, _: Int ->
                val note = noteEditText.text.toString().trim()
                val moodEntry = MoodEntry(
                    emoji = moodEmojis[selectedMoodLevel - 1],
                    note = note,
                    moodLevel = selectedMoodLevel
                )
                dataManager.addMoodEntry(moodEntry)
                loadMoodEntries()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditMoodDialog(moodEntry: MoodEntry) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_mood, null)
        val emojiButtons = listOf(
            dialogView.findViewById<Button>(R.id.mood_happy),
            dialogView.findViewById<Button>(R.id.mood_excited),
            dialogView.findViewById<Button>(R.id.mood_neutral),
            dialogView.findViewById<Button>(R.id.mood_sad),
            dialogView.findViewById<Button>(R.id.mood_angry)
        )
        val noteEditText = dialogView.findViewById<EditText>(R.id.mood_note_edit)
        
        // Pre-fill with existing values
        noteEditText.setText(moodEntry.note)
        val moodIndex = moodLevels.indexOf(moodEntry.moodLevel)
        if (moodIndex >= 0) {
            emojiButtons[moodIndex].isSelected = true
        }
        
        var selectedMoodLevel = moodEntry.moodLevel
        
        // Setup emoji buttons
        emojiButtons.forEachIndexed { index, button ->
            button.text = moodEmojis[index]
            button.setOnClickListener {
                // Reset all buttons
                emojiButtons.forEach { it.isSelected = false }
                // Select current button
                button.isSelected = true
                selectedMoodLevel = moodLevels[index]
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Save") { _: android.content.DialogInterface, _: Int ->
                val note = noteEditText.text.toString().trim()
                val updatedMoodEntry = moodEntry.copy(
                    emoji = moodEmojis[selectedMoodLevel - 1],
                    note = note,
                    moodLevel = selectedMoodLevel
                )
                // Note: We would need to implement updateMoodEntry in DataManager
                loadMoodEntries()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showMoodChart() {
        val chartFragment = MoodChartFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, chartFragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun shareMoodSummary() {
        if (moodEntries.isEmpty()) {
            Toast.makeText(context, "No mood entries to share", Toast.LENGTH_SHORT).show()
            return
        }
        
        val today = Date()
        val todayEntries = moodEntries.filter { 
            isSameDay(it.dateTime, today) 
        }
        
        val summary = if (todayEntries.isNotEmpty()) {
            val avgMood = todayEntries.map { it.moodLevel }.average()
            val moodDescription = when {
                avgMood <= 1.5 -> "very sad"
                avgMood <= 2.5 -> "sad"
                avgMood <= 3.5 -> "neutral"
                avgMood <= 4.5 -> "happy"
                else -> "very happy"
            }
            
            "Today I'm feeling $moodDescription! 🌟\n\n" +
            "Mood entries: ${todayEntries.size}\n" +
            "Average mood: ${String.format("%.1f", avgMood)}/5\n\n" +
            "Tracked with DoDaily app 📱"
        } else {
            val recentEntries = moodEntries.take(5)
            "My recent mood journey with DoDaily:\n\n" +
            recentEntries.joinToString("\n") { entry ->
                val date = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(entry.dateTime)
                "$date - ${entry.emoji} ${entry.note.takeIf { it.isNotEmpty() } ?: entry.getMoodDescription()}"
            } + "\n\nTracked with DoDaily app 📱"
        }
        
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, summary)
        }
        startActivity(Intent.createChooser(shareIntent, "Share mood summary"))
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
