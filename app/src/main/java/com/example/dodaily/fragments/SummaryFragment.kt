package com.example.dodaily.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

class SummaryFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var dateRangeText: TextView
    private lateinit var totalEntriesText: TextView
    private lateinit var averageMoodText: TextView
    private lateinit var mostCommonMoodEmoji: TextView
    private lateinit var mostCommonMoodText: TextView
    private lateinit var moodRangeText: TextView
    private lateinit var moodDistributionContainer: LinearLayout
    
    private var selectedDate: Date = Date()
    private val moodEntries = mutableListOf<MoodEntry>()
    
    companion object {
        fun newInstance(selectedDate: Date): SummaryFragment {
            val fragment = SummaryFragment()
            val args = Bundle()
            args.putLong("selected_date", selectedDate.time)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataManager = DataManager(requireContext())
        
        arguments?.let {
            selectedDate = Date(it.getLong("selected_date", Date().time))
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_summary, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        loadMoodData()
        updateSummary()
    }
    
    private fun initializeViews(view: View) {
        dateRangeText = view.findViewById(R.id.date_range_text)
        totalEntriesText = view.findViewById(R.id.total_entries_text)
        averageMoodText = view.findViewById(R.id.average_mood_text)
        mostCommonMoodEmoji = view.findViewById(R.id.most_common_mood_emoji)
        mostCommonMoodText = view.findViewById(R.id.most_common_mood_text)
        moodRangeText = view.findViewById(R.id.mood_range_text)
        moodDistributionContainer = view.findViewById(R.id.mood_distribution_container)
    }
    
    private fun loadMoodData() {
        moodEntries.clear()
        // Load mood entries for the selected date
        val entriesForDate = dataManager.getMoodEntriesForDate(selectedDate)
        moodEntries.addAll(entriesForDate)
    }
    
    private fun updateSummary() {
        updateDateRange()
        updateBasicStats()
        updateMoodDistribution()
    }
    
    private fun updateDateRange() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val today = Calendar.getInstance()
        val selected = Calendar.getInstance()
        selected.time = selectedDate
        
        val isToday = selected.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                     selected.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        
        dateRangeText.text = if (isToday) {
            "Today's Mood Summary"
        } else {
            "Mood Summary for ${dateFormat.format(selectedDate)}"
        }
    }
    
    private fun updateBasicStats() {
        val totalEntries = moodEntries.size
        totalEntriesText.text = totalEntries.toString()
        
        if (totalEntries == 0) {
            averageMoodText.text = "0.0"
            mostCommonMoodEmoji.text = "😐"
            mostCommonMoodText.text = "No Data"
            moodRangeText.text = "0 - 0"
            return
        }
        
        // Calculate average mood
        val averageMood = moodEntries.map { it.moodLevel }.average()
        averageMoodText.text = String.format("%.1f", averageMood)
        
        // Find most common mood
        val moodCounts = moodEntries.groupBy { it.moodLevel }.mapValues { it.value.size }
        val mostCommonMoodLevel = moodCounts.maxByOrNull { it.value }?.key ?: 3
        
        val moodEmojis = listOf("", "😠", "😢", "😐", "🤩", "😊") // Index 0 is empty
        val moodNames = listOf("", "Angry", "Sad", "Neutral", "Excited", "Happy")
        
        mostCommonMoodEmoji.text = moodEmojis[mostCommonMoodLevel]
        mostCommonMoodText.text = moodNames[mostCommonMoodLevel]
        
        // Calculate mood range
        val minMood = moodEntries.minOfOrNull { it.moodLevel } ?: 0
        val maxMood = moodEntries.maxOfOrNull { it.moodLevel } ?: 0
        moodRangeText.text = "$minMood - $maxMood"
    }
    
    private fun updateMoodDistribution() {
        moodDistributionContainer.removeAllViews()
        
        if (moodEntries.isEmpty()) {
            val emptyText = TextView(requireContext()).apply {
                text = "No mood entries for this date"
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_secondary, null))
            }
            moodDistributionContainer.addView(emptyText)
            return
        }
        
        val moodCounts = moodEntries.groupBy { it.moodLevel }.mapValues { it.value.size }
        val moodEmojis = listOf("", "😠", "😢", "😐", "🤩", "😊")
        val moodNames = listOf("", "Angry", "Sad", "Neutral", "Excited", "Happy")
        val moodColors = listOf("", "#FF6B6B", "#FFB347", "#FFD93D", "#6BCF7F", "#4ECDC4")
        
        // Sort by mood level (1-5)
        val sortedMoods = moodCounts.toList().sortedBy { it.first }
        
        sortedMoods.forEach { (moodLevel, count) ->
            val moodItem = createMoodDistributionItem(
                moodEmojis[moodLevel],
                moodNames[moodLevel],
                count,
                moodEntries.size,
                moodColors[moodLevel]
            )
            moodDistributionContainer.addView(moodItem)
        }
    }
    
    private fun createMoodDistributionItem(
        emoji: String,
        name: String,
        count: Int,
        total: Int,
        color: String
    ): View {
        val inflater = LayoutInflater.from(requireContext())
        val itemView = inflater.inflate(R.layout.item_mood_distribution, moodDistributionContainer, false)
        
        val emojiText = itemView.findViewById<TextView>(R.id.mood_emoji)
        val nameText = itemView.findViewById<TextView>(R.id.mood_name)
        val countText = itemView.findViewById<TextView>(R.id.mood_count)
        val percentageText = itemView.findViewById<TextView>(R.id.mood_percentage)
        val progressBar = itemView.findViewById<View>(R.id.mood_progress_bar)
        
        emojiText.text = emoji
        nameText.text = name
        countText.text = count.toString()
        
        val percentage = if (total > 0) (count * 100.0 / total).toInt() else 0
        percentageText.text = "$percentage%"
        
        // Set progress bar width
        val layoutParams = progressBar.layoutParams
        layoutParams.width = (percentage * 2).toInt() // Scale for visual representation
        progressBar.layoutParams = layoutParams
        
        // Set color
        try {
            val colorInt = android.graphics.Color.parseColor(color)
            progressBar.setBackgroundColor(colorInt)
        } catch (e: Exception) {
            progressBar.setBackgroundColor(resources.getColor(R.color.text_secondary, null))
        }
        
        return itemView
    }
    
    fun updateSelectedDate(newDate: Date) {
        selectedDate = newDate
        loadMoodData()
        updateSummary()
    }
}
