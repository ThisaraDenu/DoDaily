package com.example.dodaily.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for displaying mood entries
 */
class MoodEntriesAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onMoodEntryClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntriesAdapter.MoodEntryViewHolder>() {
    
    class MoodEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(R.id.mood_emoji)
        val moodDescriptionText: TextView = itemView.findViewById(R.id.mood_description)
        val noteText: TextView = itemView.findViewById(R.id.mood_note)
        val timeText: TextView = itemView.findViewById(R.id.mood_time)
        val moodLevelText: TextView = itemView.findViewById(R.id.mood_level)
        val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.mood_card)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodEntryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        // Set mood data
        holder.emojiText.text = moodEntry.emoji
        holder.moodDescriptionText.text = moodEntry.getMoodDescription()
        holder.noteText.text = moodEntry.note
        holder.moodLevelText.text = "${moodEntry.moodLevel}/5"
        
        // Set time
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.timeText.text = timeFormat.format(moodEntry.dateTime)
        
        // Set card background color based on mood level
        val moodColor = when (moodEntry.moodLevel) {
            1 -> R.color.mood_very_sad
            2 -> R.color.mood_sad
            3 -> R.color.mood_neutral
            4 -> R.color.mood_happy
            5 -> R.color.mood_very_happy
            else -> R.color.mood_neutral
        }
        holder.cardView.setCardBackgroundColor(holder.itemView.context.getColor(moodColor))
        
        // Set text colors for better contrast
        val textColor = if (moodEntry.moodLevel <= 2) {
            holder.itemView.context.getColor(R.color.white)
        } else {
            holder.itemView.context.getColor(R.color.text_primary)
        }
        
        holder.moodDescriptionText.setTextColor(textColor)
        holder.noteText.setTextColor(textColor)
        holder.timeText.setTextColor(textColor)
        holder.moodLevelText.setTextColor(textColor)
        
        // Set click listener
        holder.itemView.setOnClickListener {
            onMoodEntryClick(moodEntry)
        }
    }
    
    override fun getItemCount(): Int = moodEntries.size
}
