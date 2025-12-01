package com.example.progr3ss.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progr3ss.R
import com.example.progr3ss.model.ScheduleResponseDto
import com.example.progr3ss.model.ScheduleStatus

class ScheduleAdapter : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    private var schedules: List<ScheduleResponseDto> = emptyList()

    fun submitList(newSchedules: List<ScheduleResponseDto>) {
        android.util.Log.d("ScheduleAdapter", "===========================================")
        android.util.Log.d("ScheduleAdapter", " ADAPTER - submitList called")
        android.util.Log.d("ScheduleAdapter", "Old list size: ${schedules.size}")
        android.util.Log.d("ScheduleAdapter", "New list size: ${newSchedules.size}")
        schedules = newSchedules
        notifyDataSetChanged()
        android.util.Log.d("ScheduleAdapter", "notifyDataSetChanged() called")
        android.util.Log.d("ScheduleAdapter", "===========================================")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        android.util.Log.d("ScheduleAdapter", "Creating new ViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        android.util.Log.d("ScheduleAdapter", "Binding ViewHolder at position $position")
        holder.bind(schedules[position])
    }

    override fun getItemCount(): Int {
        android.util.Log.d("ScheduleAdapter", "getItemCount() called, returning: ${schedules.size}")
        return schedules.size
    }

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)

        fun bind(schedule: ScheduleResponseDto) {
            android.util.Log.d("ScheduleAdapter", "-------------------------------------------")
            android.util.Log.d("ScheduleAdapter", "Binding schedule:")
            android.util.Log.d("ScheduleAdapter", "  Title: ${schedule.title}")
            android.util.Log.d("ScheduleAdapter", "  Start Time: ${schedule.startTime}")
            android.util.Log.d("ScheduleAdapter", "  Status: ${schedule.scheduleStatus}")

            tvTime.text = extractTime(schedule.startTime)
            tvTitle.text = schedule.title

            if (schedule.description.isNullOrEmpty()) {
                tvDescription.visibility = View.GONE
            } else {
                tvDescription.visibility = View.VISIBLE
                tvDescription.text = schedule.description
            }

            when (schedule.scheduleStatus) {
                ScheduleStatus.PLANNED -> {
                    tvStatus.text = "Planned"
                    tvStatus.setTextColor(Color.parseColor("#FFB84D"))
                    tvStatus.setBackgroundColor(Color.parseColor("#3D2F1F"))
                    ivStatus.setImageResource(android.R.drawable.ic_menu_recent_history)
                    ivStatus.setColorFilter(Color.parseColor("#FFB84D"))
                }
                ScheduleStatus.COMPLETED -> {
                    tvStatus.text = "Completed"
                    tvStatus.setTextColor(Color.parseColor("#4ECDC4"))
                    tvStatus.setBackgroundColor(Color.parseColor("#1F3D3B"))
                    ivStatus.setImageResource(android.R.drawable.checkbox_on_background)
                    ivStatus.setColorFilter(Color.parseColor("#4ECDC4"))
                }
                ScheduleStatus.SKIPPED -> {
                    tvStatus.text = "Skipped"
                    tvStatus.setTextColor(Color.parseColor("#FF6B6B"))
                    tvStatus.setBackgroundColor(Color.parseColor("#3D1F1F"))
                    ivStatus.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                    ivStatus.setColorFilter(Color.parseColor("#FF6B6B"))
                }
            }
        }

        private fun extractTime(timeString: String): String {
            if (timeString.matches(Regex("\\d{2}:\\d{2}"))) {
                return timeString
            }
            return try {
                timeString.substring(11, 16)
            } catch (e: Exception) {
                timeString
            }
        }
    }
}

