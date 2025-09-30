package com.example.dodaily.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import com.example.dodaily.services.HydrationReminderService

/**
 * Fragment for app settings and preferences
 * Includes hydration reminder settings and user preferences
 */
class SettingsFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var userNameText: TextView
    private lateinit var hydrationSwitch: Switch
    private lateinit var hydrationIntervalSpinner: Spinner
    private lateinit var clearDataButton: Button
    private lateinit var aboutButton: Button
    
    private val intervalOptions = listOf(30, 60, 90, 120, 180, 240) // minutes
    private val intervalLabels = listOf("30 min", "1 hour", "1.5 hours", "2 hours", "3 hours", "4 hours")
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize DataManager
        dataManager = (activity as? com.example.dodaily.HomeActivity)?.getDataManager() ?: return
        
        // Initialize views
        userNameText = view.findViewById(R.id.user_name_text)
        hydrationSwitch = view.findViewById(R.id.hydration_switch)
        hydrationIntervalSpinner = view.findViewById(R.id.hydration_interval_spinner)
        clearDataButton = view.findViewById(R.id.clear_data_button)
        aboutButton = view.findViewById(R.id.about_button)
        
        // Setup UI
        setupUI()
        loadSettings()
        
        // Setup listeners
        setupListeners()
    }
    
    private fun setupUI() {
        // Setup spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervalLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hydrationIntervalSpinner.adapter = adapter
        
        // Set user name
        userNameText.text = "Welcome, ${dataManager.getUserName()}!"
    }
    
    private fun loadSettings() {
        // Load hydration reminder settings
        hydrationSwitch.isChecked = dataManager.isHydrationReminderEnabled()
        
        val currentInterval = dataManager.getHydrationInterval()
        val intervalIndex = intervalOptions.indexOf(currentInterval)
        if (intervalIndex != -1) {
            hydrationIntervalSpinner.setSelection(intervalIndex)
        }
    }
    
    private fun setupListeners() {
        // Hydration switch
        hydrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            dataManager.setHydrationReminderEnabled(isChecked)
            
            if (isChecked) {
                // Start hydration reminder service
                val intent = Intent(requireContext(), HydrationReminderService::class.java)
                requireContext().startService(intent)
                Toast.makeText(context, "Hydration reminders enabled", Toast.LENGTH_SHORT).show()
            } else {
                // Stop hydration reminder service
                val intent = Intent(requireContext(), HydrationReminderService::class.java)
                requireContext().stopService(intent)
                Toast.makeText(context, "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Hydration interval spinner
        hydrationIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedInterval = intervalOptions[position]
                dataManager.setHydrationInterval(selectedInterval)
                
                // Restart service with new interval if enabled
                if (hydrationSwitch.isChecked) {
                    val intent = Intent(requireContext(), HydrationReminderService::class.java)
                    requireContext().startService(intent)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Clear data button
        clearDataButton.setOnClickListener {
            showClearDataDialog()
        }
        
        // About button
        aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }
    
    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("This will delete all your habits, mood entries, and settings. This action cannot be undone. Are you sure?")
            .setPositiveButton("Clear All") { _, _ ->
                // Clear all data
                dataManager.saveHabits(emptyList())
                dataManager.saveMoodEntries(emptyList())
                dataManager.setHydrationReminderEnabled(false)
                dataManager.setHydrationInterval(60)
                
                // Stop hydration service
                val intent = Intent(requireContext(), HydrationReminderService::class.java)
                requireContext().stopService(intent)
                
                Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                loadSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAboutDialog() {
        val aboutText = """
            DoDaily v1.0
            
            A personal wellness app to help you manage your daily health routines.
            
            Features:
            • Daily habit tracking
            • Mood journaling with emojis
            • Hydration reminders
            • Progress visualization
            
            Built with ❤️ using Android Studio and Kotlin
        """.trimIndent()
        
        AlertDialog.Builder(requireContext())
            .setTitle("About DoDaily")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show()
    }
}
