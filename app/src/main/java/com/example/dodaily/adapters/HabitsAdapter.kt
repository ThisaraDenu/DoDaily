package com.example.dodaily.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.model.Habit

/**
 * RecyclerView Adapter for displaying habits
 */
class HabitsAdapter(
    private val habits: List<Habit>,
    private val onHabitAction: (Habit, HabitAction) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {
    
    enum class HabitAction {
        INCREMENT, EDIT, DELETE
    }
    
    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emojiText: TextView = itemView.findViewById(R.id.habit_emoji)
        val nameText: TextView = itemView.findViewById(R.id.habit_name)
        val descriptionText: TextView = itemView.findViewById(R.id.habit_description)
        val progressText: TextView = itemView.findViewById(R.id.habit_progress)
        val progressBar: ProgressBar = itemView.findViewById(R.id.habit_progress_bar)
        val progressPercentage: TextView = itemView.findViewById(R.id.progress_percentage)
        val incrementButton: Button = itemView.findViewById(R.id.increment_button)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        val completedIcon: ImageView = itemView.findViewById(R.id.completed_icon)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        
        // Set habit data
        holder.emojiText.text = habit.emoji
        holder.nameText.text = habit.name
        holder.descriptionText.text = habit.description
        
        // Set progress
        val progressPercentage = habit.getCompletionPercentage()
        holder.progressText.text = "${habit.currentCount}/${habit.targetCount}"
        holder.progressBar.progress = progressPercentage.toInt()
        holder.progressPercentage.text = "${progressPercentage.toInt()}%"
        
        // Set completion status
        if (habit.isFullyCompleted()) {
            holder.completedIcon.visibility = View.VISIBLE
            holder.completedIcon.startAnimation(android.view.animation.AnimationUtils.loadAnimation(holder.itemView.context, R.anim.scale_up))
            holder.incrementButton.isEnabled = false
            holder.incrementButton.text = "🎉 Completed!"
            holder.incrementButton.backgroundTintList = holder.itemView.context.getColorStateList(R.color.light_green)
        } else {
            holder.completedIcon.visibility = View.GONE
            holder.incrementButton.isEnabled = true
            holder.incrementButton.text = "✨ Add +1"
            holder.incrementButton.backgroundTintList = holder.itemView.context.getColorStateList(R.color.primary_green)
        }
        
        // Set click listeners
        holder.incrementButton.setOnClickListener {
            onHabitAction(habit, HabitAction.INCREMENT)
        }
        
        holder.editButton.setOnClickListener {
            onHabitAction(habit, HabitAction.EDIT)
        }
        
        holder.deleteButton.setOnClickListener {
            onHabitAction(habit, HabitAction.DELETE)
        }
    }
    
    override fun getItemCount(): Int = habits.size
}
