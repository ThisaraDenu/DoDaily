package com.example.dodaily.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R

class TimePickerAdapter(
    private val timeList: List<String>,
    private val onTimeSelected: (String) -> Unit
) : RecyclerView.Adapter<TimePickerAdapter.TimeViewHolder>() {

    private var selectedPosition = 0

    class TimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.time_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_picker, parent, false)
        return TimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val time = timeList[position]
        holder.timeText.text = time
        holder.timeText.isSelected = position == selectedPosition

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            onTimeSelected(time)
        }
    }

    override fun getItemCount(): Int = timeList.size

    fun setSelectedTime(time: String) {
        val position = timeList.indexOf(time)
        if (position != -1) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }
}
