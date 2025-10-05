package com.example.dodaily.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.dodaily.R
import com.example.dodaily.model.HydrationSchedule

class HydrationScheduleAdapter(
    private val scheduleList: List<HydrationSchedule>,
    private val onDeleteClick: (HydrationSchedule) -> Unit
) : RecyclerView.Adapter<HydrationScheduleAdapter.ScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_time, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]
        holder.bind(schedule)
    }

    override fun getItemCount(): Int = scheduleList.size

    inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val scheduleTime: TextView = itemView.findViewById(R.id.schedule_time)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(schedule: HydrationSchedule) {
            scheduleTime.text = schedule.time

            // Set click listeners
            deleteButton.setOnClickListener {
                onDeleteClick(schedule)
            }
        }
    }
}
