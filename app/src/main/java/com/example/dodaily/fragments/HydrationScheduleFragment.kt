package com.example.dodaily.fragments

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.adapters.HydrationScheduleAdapter
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.HydrationSchedule
import com.example.dodaily.services.HydrationAlarmManager
import com.example.dodaily.services.HydrationNotificationService
import java.text.SimpleDateFormat
import java.util.*

class HydrationScheduleFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var scheduleRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyStateText: TextView
    private lateinit var addScheduleFab: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var dateTypeText: TextView
    private lateinit var progressText: TextView
    
    private lateinit var scheduleAdapter: HydrationScheduleAdapter
    private lateinit var alarmManager: HydrationAlarmManager
    private val scheduleList = mutableListOf<HydrationSchedule>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration_schedule, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize DataManager
        dataManager = DataManager(requireContext())
        
        // Initialize alarm manager and notification service
        alarmManager = HydrationAlarmManager(requireContext())
        HydrationNotificationService.createNotificationChannel(requireContext())
        
        // Initialize views
        initializeViews(view)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup click listeners
        setupClickListeners()
        
        // Load schedule data
        loadScheduleData()
        
        // Reschedule all notifications
        rescheduleAllNotifications()
        
        // Update UI
        updateUI()
    }
    
    private fun initializeViews(view: View) {
        scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        emptyStateText = view.findViewById(R.id.empty_state_text)
        addScheduleFab = view.findViewById(R.id.add_schedule_fab)
        dateTypeText = view.findViewById(R.id.date_type_text)
        progressText = view.findViewById(R.id.progress_text)
    }
    
    private fun setupRecyclerView() {
        scheduleAdapter = HydrationScheduleAdapter(
            scheduleList,
            onDeleteClick = { schedule -> deleteSchedule(schedule) }
        )
        
        scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        scheduleRecyclerView.adapter = scheduleAdapter
    }
    
    private fun setupClickListeners() {
        addScheduleFab.setOnClickListener {
            // Test notification to verify system works
            testNotification()
            showTimePickerDialog()
        }
    }
    
    private fun testNotification() {
        // Test notification to verify the system works
        android.util.Log.d("HydrationSchedule", "Testing notification...")
        HydrationNotificationService.showHydrationNotification(requireContext(), "Test Time")
        
        // Also test alarm for 1 minute from now
        testAlarm()
    }
    
    private fun testAlarm() {
        // Schedule a test alarm for 1 minute from now
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, 1)
        
        android.util.Log.d("HydrationSchedule", "Scheduling test alarm for: ${calendar.time}")
        
        val intent = android.content.Intent(requireContext(), com.example.dodaily.receivers.HydrationAlarmReceiver::class.java).apply {
            putExtra("time", "😠")
        }
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            requireContext(),
            9999, // Unique request code for test
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        val alarmManager = requireContext().getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.setExact(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        
        android.widget.Toast.makeText(requireContext(), "Test alarm scheduled for 1 minute", android.widget.Toast.LENGTH_SHORT).show()
    }
    
    private fun loadScheduleData() {
        scheduleList.clear()
        
        // Load saved schedules from DataManager
        val savedSchedules = dataManager.getHydrationSchedules()
        scheduleList.addAll(savedSchedules)
        
        // If no saved schedules, add default ones
        if (scheduleList.isEmpty()) {
            val defaultSchedules = listOf(
                HydrationSchedule("1", "08:00 AM", "Morning Hydration", false),
                HydrationSchedule("2", "12:00 PM", "Lunch Hydration", false),
                HydrationSchedule("3", "04:00 PM", "Afternoon Hydration", false)
            )
            scheduleList.addAll(defaultSchedules)
            saveSchedules()
        }
        
        scheduleAdapter.notifyDataSetChanged()
    }
    
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val isAM = hourOfDay < 12
                val hour12 = if (hourOfDay == 0) 12 else if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
                val period = if (isAM) "AM" else "PM"
                val timeString = String.format("%02d:%02d %s", hour12, minute, period)
                
                val newSchedule = HydrationSchedule(
                    id = UUID.randomUUID().toString(),
                    time = timeString,
                    description = "Hydration Reminder",
                    isCompleted = false
                )
                
                scheduleList.add(newSchedule)
                scheduleAdapter.notifyItemInserted(scheduleList.size - 1)
                saveSchedules()
                updateUI()
            },
            currentHour,
            currentMinute,
            false // 24-hour format
        ).show()
    }
    
    private fun deleteSchedule(schedule: HydrationSchedule) {
        val index = scheduleList.indexOf(schedule)
        if (index != -1) {
            // Cancel notification for this schedule
            alarmManager.cancelHydrationReminder(schedule.time)
            
            scheduleList.removeAt(index)
            scheduleAdapter.notifyItemRemoved(index)
            saveSchedules()
            updateUI()
        }
    }
    
    private fun saveSchedules() {
        dataManager.saveHydrationSchedules(scheduleList)
    }
    
    private fun updateUI() {
        // Update date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val today = Calendar.getInstance()
        dateTypeText.text = "Today - ${dateFormat.format(today.time)}"
        
        // Update progress
        val totalCount = scheduleList.size
        progressText.text = "Total Reminders: $totalCount scheduled"
        
        // Show/hide empty state
        if (scheduleList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            scheduleRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            scheduleRecyclerView.visibility = View.VISIBLE
        }
    }
    
    fun addNewSchedule(time: String) {
        val newSchedule = HydrationSchedule(
            id = UUID.randomUUID().toString(),
            time = time,
            description = "Hydration Reminder",
            isCompleted = false
        )
        
        scheduleList.add(newSchedule)
        scheduleAdapter.notifyItemInserted(scheduleList.size - 1)
        saveSchedules()
        updateUI()
        
        // Schedule notification for this time
        scheduleNotification(time)
    }
    
    private fun scheduleNotification(time: String) {
        try {
            // Parse time string (format: "08:00 AM" or "2:30 PM")
            val timeParts = time.split(" ")
            if (timeParts.size == 2) {
                val timeOnly = timeParts[0]
                val period = timeParts[1]
                
                val timeComponents = timeOnly.split(":")
                if (timeComponents.size == 2) {
                    var hour = timeComponents[0].toInt()
                    val minute = timeComponents[1].toInt()
                    
                    // Convert to 24-hour format
                    if (period == "PM" && hour != 12) {
                        hour += 12
                    } else if (period == "AM" && hour == 12) {
                        hour = 0
                    }
                    
                    // Schedule the alarm
                    alarmManager.scheduleHydrationReminder(time, hour, minute)
                }
            }
        } catch (e: Exception) {
            // Handle parsing errors gracefully
            android.util.Log.e("HydrationSchedule", "Error parsing time: $time", e)
        }
    }
    
    private fun rescheduleAllNotifications() {
        // Reschedule all existing notifications (useful when app restarts)
        for (schedule in scheduleList) {
            scheduleNotification(schedule.time)
        }
    }
    
}
