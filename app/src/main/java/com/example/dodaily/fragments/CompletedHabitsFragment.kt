package com.example.dodaily.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.adapters.HabitsAdapter
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.Habit
import com.example.dodaily.model.HabitWithCompletion
import com.example.dodaily.model.HabitCompletion
import java.util.*

class CompletedHabitsFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var completedHabitsRecycler: RecyclerView
    private lateinit var completedEmptyState: LinearLayout
    private lateinit var completedCountText: TextView
    private lateinit var habitsAdapter: HabitsAdapter
    
    private val completedHabits = mutableListOf<HabitWithCompletion>()
    private var selectedDate: Date = Date()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_complete_habit, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize DataManager
        dataManager = DataManager(requireContext())
        
        // Initialize views
        initializeViews(view)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Load completed habits
        loadCompletedHabits()
    }
    
    private fun initializeViews(view: View) {
        completedHabitsRecycler = view.findViewById(R.id.completed_habits_recycler)
        completedEmptyState = view.findViewById(R.id.completed_empty_state)
        completedCountText = view.findViewById(R.id.completed_count_text)
    }
    
    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(completedHabits.map { it.habit }) { habit, action ->
            // Handle habit action - for completed habits, we only handle toggle completion
            when (action) {
                HabitsAdapter.HabitAction.INCREMENT -> {
                    val habitWithCompletion = completedHabits.find { it.habit.id == habit.id }
                    habitWithCompletion?.let { toggleHabitCompletion(it) }
                }
                else -> { /* Handle other actions if needed */ }
            }
        }
        
        completedHabitsRecycler.layoutManager = LinearLayoutManager(requireContext())
        completedHabitsRecycler.adapter = habitsAdapter
    }
    
    private fun loadCompletedHabits() {
        completedHabits.clear()
        
        // Get all habits for the selected date
        val allHabits = dataManager.loadHabits()
        val habitCompletions = dataManager.getHabitCompletionsForDate(selectedDate)
        
        // Filter only completed habits
        val completed = allHabits.mapNotNull { habit ->
            val completion = habitCompletions.find { it.habitId == habit.id }
            if (completion?.isCompleted == true) {
                HabitWithCompletion(
                    habit = habit,
                    completedCount = completion.completedCount,
                    isCompleted = completion.isCompleted,
                    date = selectedDate
                )
            } else null
        }
        
        completedHabits.addAll(completed)
        
        // Update adapter with new habit list
        habitsAdapter = HabitsAdapter(completedHabits.map { it.habit }) { habit, action ->
            when (action) {
                HabitsAdapter.HabitAction.INCREMENT -> {
                    val habitWithCompletion = completedHabits.find { it.habit.id == habit.id }
                    habitWithCompletion?.let { toggleHabitCompletion(it) }
                }
                else -> { /* Handle other actions if needed */ }
            }
        }
        completedHabitsRecycler.adapter = habitsAdapter
        
        updateUI()
        updateCount()
    }
    
    private fun updateUI() {
        if (completedHabits.isEmpty()) {
            completedEmptyState.visibility = View.VISIBLE
            completedHabitsRecycler.visibility = View.GONE
        } else {
            completedEmptyState.visibility = View.GONE
            completedHabitsRecycler.visibility = View.VISIBLE
        }
    }
    
    private fun updateCount() {
        val totalHabits = dataManager.loadHabits().size
        val completedCount = completedHabits.size
        completedCountText.text = "$completedCount/$totalHabits"
    }
    
    private fun toggleHabitCompletion(habitWithCompletion: HabitWithCompletion) {
        // Toggle completion status
        val newCompletionStatus = !habitWithCompletion.isCompleted
        
        if (newCompletionStatus) {
            // Mark as completed
            dataManager.completeHabitForDate(
                habitWithCompletion.habit.id,
                selectedDate,
                habitWithCompletion.habit.targetCount
            )
        } else {
            // Mark as not completed
            dataManager.updateHabitCompletion(
                habitWithCompletion.habit.id,
                selectedDate,
                false
            )
        }
        
        // Remove from completed list if unchecked
        if (!newCompletionStatus) {
            val index = completedHabits.indexOf(habitWithCompletion)
            if (index != -1) {
                completedHabits.removeAt(index)
                habitsAdapter.notifyItemRemoved(index)
            }
        }
        
        updateUI()
        updateCount()
    }
    
    fun updateSelectedDate(date: Date) {
        selectedDate = date
        loadCompletedHabits()
    }
}
