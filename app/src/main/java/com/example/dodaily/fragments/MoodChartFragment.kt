package com.example.dodaily.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dodaily.R
import com.example.dodaily.data.DataManager
import com.example.dodaily.model.MoodEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for displaying mood trend chart
 * Shows mood data over the last 7 days using MPAndroidChart
 */
class MoodChartFragment : Fragment() {
    
    private lateinit var dataManager: DataManager
    private lateinit var moodChart: LineChart
    private lateinit var noDataText: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood_chart, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize DataManager
        dataManager = (activity as? com.example.dodaily.HomeActivity)?.getDataManager() ?: return
        
        // Initialize views
        moodChart = view.findViewById(R.id.mood_chart)
        noDataText = view.findViewById(R.id.no_data_text)
        
        // Setup chart
        setupChart()
        
        // Load and display data
        loadMoodData()
    }
    
    private fun setupChart() {
        // Configure chart appearance
        moodChart.description.isEnabled = false
        moodChart.setTouchEnabled(true)
        moodChart.isDragEnabled = true
        moodChart.setScaleEnabled(true)
        moodChart.setPinchZoom(true)
        moodChart.setBackgroundColor(Color.WHITE)
        
        // Configure X-axis
        val xAxis = moodChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        
        // Configure Y-axis
        val leftAxis = moodChart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f
        leftAxis.granularity = 1f
        leftAxis.isGranularityEnabled = true
        
        val rightAxis = moodChart.axisRight
        rightAxis.isEnabled = false
        
        // Configure legend
        val legend = moodChart.legend
        legend.isEnabled = true
        legend.textSize = 12f
        legend.textColor = Color.BLACK
    }
    
    private fun loadMoodData() {
        val moodEntries = dataManager.getMoodEntriesForLastWeek()
        
        if (moodEntries.isEmpty()) {
            moodChart.visibility = View.GONE
            noDataText.visibility = View.VISIBLE
            return
        }
        
        moodChart.visibility = View.VISIBLE
        noDataText.visibility = View.GONE
        
        // Group mood entries by date and calculate average mood for each day
        val moodData = groupMoodEntriesByDate(moodEntries)
        
        // Create chart data
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        
        moodData.forEachIndexed { index, (date, avgMood) ->
            entries.add(Entry(index.toFloat(), avgMood.toFloat()))
            labels.add(formatDate(date))
        }
        
        // Create dataset
        val dataSet = LineDataSet(entries, "Mood Level").apply {
            color = Color.parseColor("#5F8E5C")
            setCircleColor(Color.parseColor("#5F8E5C"))
            lineWidth = 3f
            circleRadius = 6f
            setDrawCircleHole(false)
            setDrawValues(true)
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            setDrawFilled(true)
            fillColor = Color.parseColor("#5F8E5C")
            fillAlpha = 50
        }
        
        // Set X-axis labels
        val xAxis = moodChart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < labels.size) {
                    labels[index]
                } else ""
            }
        }
        
        // Create line data and set to chart
        val lineData = LineData(dataSet)
        moodChart.data = lineData
        moodChart.invalidate()
    }
    
    private fun groupMoodEntriesByDate(moodEntries: List<MoodEntry>): List<Pair<Date, Double>> {
        val groupedEntries = moodEntries.groupBy { entry ->
            val calendar = Calendar.getInstance()
            calendar.time = entry.dateTime
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.time
        }
        
        return groupedEntries.map { (date, entries) ->
            val avgMood = entries.map { it.moodLevel }.average()
            date to avgMood
        }.sortedBy { it.first }
    }
    
    private fun formatDate(date: Date): String {
        val today = Date()
        val yesterday = Date(today.time - 24 * 60 * 60 * 1000)
        
        return when {
            isSameDay(date, today) -> "Today"
            isSameDay(date, yesterday) -> "Yesterday"
            else -> {
                val format = SimpleDateFormat("MMM dd", Locale.getDefault())
                format.format(date)
            }
        }
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
