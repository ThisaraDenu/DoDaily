package com.example.dodaily.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.model.MoodEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * RecyclerView Adapter for displaying mood entries with drag-to-show edit/delete functionality
 */
class MoodEntriesAdapter(
    private val moodEntries: List<MoodEntry>,
    private val onMoodEntryClick: (MoodEntry) -> Unit,
    private val onEditClick: (MoodEntry) -> Unit,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntriesAdapter.MoodEntryViewHolder>() {
    
    // Track which item is currently expanded
    private var expandedPosition = -1
    
    class MoodEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(R.id.mood_emoji)
        val moodDescriptionText: TextView = itemView.findViewById(R.id.mood_description)
        val noteText: TextView = itemView.findViewById(R.id.mood_note)
        val timeText: TextView = itemView.findViewById(R.id.mood_time)
        val viewButton: ImageButton = itemView.findViewById(R.id.view_button)
        val cardView: androidx.cardview.widget.CardView = itemView.findViewById(R.id.mood_card)
        val mainContent: LinearLayout = itemView.findViewById(R.id.main_content)
        val actionButtons: LinearLayout = itemView.findViewById(R.id.action_buttons)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        
        // Drag functionality variables
        private var startX = 0f
        private var isDragging = false
        private val maxDragDistance = 200f // Maximum drag distance in pixels
        
        fun setupDragListener(onDragComplete: (Boolean) -> Unit) {
            mainContent.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.x
                        isDragging = false
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = startX - event.x
                        if (Math.abs(deltaX) > 20) { // Start dragging after 20px movement
                            isDragging = true
                            val dragDistance = Math.max(0f, Math.min(deltaX, maxDragDistance))
                            mainContent.translationX = -dragDistance
                            
                            // Show action buttons when dragging left
                            if (dragDistance > 50) {
                                actionButtons.visibility = View.VISIBLE
                                actionButtons.alpha = (dragDistance / maxDragDistance).coerceAtMost(1f)
                            } else {
                                actionButtons.visibility = View.GONE
                            }
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        if (isDragging) {
                            val deltaX = startX - event.x
                            val shouldShow = deltaX > 100 // Show if dragged more than 100px
                            
                            if (shouldShow) {
                                // Snap to show action buttons
                                mainContent.animate()
                                    .translationX(-maxDragDistance)
                                    .setDuration(200)
                                    .start()
                                actionButtons.visibility = View.VISIBLE
                                actionButtons.alpha = 1f
                                onDragComplete(true)
                            } else {
                                // Snap back to original position
                                mainContent.animate()
                                    .translationX(0f)
                                    .setDuration(200)
                                    .start()
                                actionButtons.visibility = View.GONE
                                onDragComplete(false)
                            }
                        }
                        false
                    }
                    else -> false
                }
            }
        }
        
        fun resetPosition() {
            mainContent.translationX = 0f
            actionButtons.visibility = View.GONE
            actionButtons.alpha = 0f
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_entry, parent, false)
        return MoodEntryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        val moodEntry = moodEntries[position]
        
        // Reset position if this item is not expanded
        if (expandedPosition != position) {
            holder.resetPosition()
        }
        
        // Set mood data
        holder.emojiText.text = moodEntry.emoji
        holder.moodDescriptionText.text = moodEntry.getMoodDescription()
        holder.noteText.text = moodEntry.note
        
        // Show note if it exists
        holder.noteText.visibility = if (moodEntry.note.isNotEmpty()) View.VISIBLE else View.GONE
        
        // Set time
        val timeFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        holder.timeText.text = timeFormat.format(moodEntry.dateTime)
        
        // Set card background color based on mood level
        val moodColor = when (moodEntry.moodLevel) {
            1 -> R.color.mood_very_sad // Angry
            2 -> R.color.mood_sad // Sad
            3 -> R.color.mood_neutral // Neutral
            4 -> R.color.mood_happy // Excited
            5 -> R.color.mood_very_happy // Happy
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
        
        // Setup drag functionality
        holder.setupDragListener { isExpanded ->
            if (isExpanded) {
                // Collapse previously expanded item
                if (expandedPosition != -1 && expandedPosition != position) {
                    notifyItemChanged(expandedPosition)
                }
                expandedPosition = position
            } else {
                if (expandedPosition == position) {
                    expandedPosition = -1
                }
            }
        }
        
        // Setup view button listener
        holder.viewButton.setOnClickListener {
            onMoodEntryClick(moodEntry)
        }
        
        // Setup action button listeners
        holder.editButton.setOnClickListener {
            onEditClick(moodEntry)
            // Collapse after action
            expandedPosition = -1
            holder.resetPosition()
        }
        
        holder.deleteButton.setOnClickListener {
            onDeleteClick(moodEntry)
            // Collapse after action
            expandedPosition = -1
            holder.resetPosition()
        }
    }
    
    override fun getItemCount(): Int = moodEntries.size
    
    /**
     * Collapse all expanded items
     */
    fun collapseAll() {
        if (expandedPosition != -1) {
            val previousExpanded = expandedPosition
            expandedPosition = -1
            notifyItemChanged(previousExpanded)
        }
    }
    
    /**
     * Check if any item is currently expanded
     */
    fun hasExpandedItem(): Boolean = expandedPosition != -1
}
