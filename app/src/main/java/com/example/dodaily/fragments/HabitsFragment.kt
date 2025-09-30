package com.example.dodaily.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.Habit
import com.example.dodaily.adapters.HabitsAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

/**
 * Fragment for managing daily habits
 * Allows users to add, edit, delete, and track habit completion
 */
class HabitsFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var addHabitFab: FloatingActionButton
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var progressText: TextView
    
    private val habits = mutableListOf<Habit>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize DataManager
        dataManager = (activity as? com.example.dodaily.HomeActivity)?.getDataManager() ?: return
        
        // Initialize views
        habitsRecyclerView = view.findViewById(R.id.habits_recycler_view)
        addHabitFab = view.findViewById(R.id.add_habit_fab)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        emptyStateText = view.findViewById(R.id.empty_state_text)
        progressText = view.findViewById(R.id.progress_text)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup FAB
        addHabitFab.setOnClickListener {
            showAddHabitDialog()
        }
        
        // Load habits
        loadHabits()
    }
    
    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(habits) { habit, action ->
            when (action) {
                HabitsAdapter.HabitAction.INCREMENT -> incrementHabit(habit)
                HabitsAdapter.HabitAction.EDIT -> showEditHabitDialog(habit)
                HabitsAdapter.HabitAction.DELETE -> showDeleteHabitDialog(habit)
            }
        }
        
        habitsRecyclerView.layoutManager = LinearLayoutManager(context)
        habitsRecyclerView.adapter = habitsAdapter
    }
    
    private fun loadHabits() {
        habits.clear()
        habits.addAll(dataManager.loadHabits())
        habitsAdapter.notifyDataSetChanged()
        updateUI()
    }
    
    private fun updateUI() {
        if (habits.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            habitsRecyclerView.visibility = View.GONE
            progressText.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            habitsRecyclerView.visibility = View.VISIBLE
            progressText.visibility = View.VISIBLE
            
            // Calculate overall progress
            val totalHabits = habits.size
            val completedHabits = habits.count { it.isFullyCompleted() }
            val progressPercentage = if (totalHabits > 0) {
                (completedHabits * 100) / totalHabits
            } else 0
            
            progressText.text = "Today's Progress: $completedHabits/$totalHabits habits completed ($progressPercentage%)"
        }
    }
    
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.habit_name_edit)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.habit_description_edit)
        val targetCountEditText = dialogView.findViewById<EditText>(R.id.habit_target_count_edit)
        val emojiEditText = dialogView.findViewById<EditText>(R.id.habit_emoji_edit)
        
        // Set default values
        targetCountEditText.setText("1")
        emojiEditText.setText("📝")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
                val emoji = emojiEditText.text.toString().trim().takeIf { it.isNotEmpty() } ?: "📝"
                
                if (name.isNotEmpty()) {
                    val habit = Habit(
                        name = name,
                        description = description,
                        targetCount = targetCount,
                        emoji = emoji
                    )
                    dataManager.addHabit(habit)
                    loadHabits()
                } else {
                    Toast.makeText(context, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_habit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.habit_name_edit)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.habit_description_edit)
        val targetCountEditText = dialogView.findViewById<EditText>(R.id.habit_target_count_edit)
        val emojiEditText = dialogView.findViewById<EditText>(R.id.habit_emoji_edit)
        
        // Pre-fill with existing values
        nameEditText.setText(habit.name)
        descriptionEditText.setText(habit.description)
        targetCountEditText.setText(habit.targetCount.toString())
        emojiEditText.setText(habit.emoji)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetCount = targetCountEditText.text.toString().toIntOrNull() ?: 1
                val emoji = emojiEditText.text.toString().trim().takeIf { it.isNotEmpty() } ?: "📝"
                
                if (name.isNotEmpty()) {
                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        targetCount = targetCount,
                        emoji = emoji
                    )
                    dataManager.updateHabit(updatedHabit)
                    loadHabits()
                } else {
                    Toast.makeText(context, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDeleteHabitDialog(habit: Habit) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                dataManager.deleteHabit(habit.id)
                loadHabits()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun incrementHabit(habit: Habit) {
        val updatedHabit = habit.copy(
            currentCount = habit.currentCount + 1,
            isCompleted = (habit.currentCount + 1) >= habit.targetCount
        )
        dataManager.updateHabit(updatedHabit)
        loadHabits()
    }
}
