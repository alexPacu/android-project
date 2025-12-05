package com.example.progr3ss.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progr3ss.R
import com.example.progr3ss.model.ProgressResponseDto

class ScheduleProgressAdapter : RecyclerView.Adapter<ScheduleProgressAdapter.ProgressViewHolder>() {

    private val items = mutableListOf<ProgressResponseDto>()

    fun submitList(newItems: List<ProgressResponseDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgressViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_progress, parent, false)
        return ProgressViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProgressViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ProgressViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvProgressDate)
        private val tvLoggedTime: TextView = itemView.findViewById(R.id.tvProgressLoggedTime)
        private val tvNotes: TextView = itemView.findViewById(R.id.tvProgressNotes)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvProgressStatus)

        fun bind(item: ProgressResponseDto) {
            tvDate.text = item.date
            tvLoggedTime.text = if (item.loggedTime != null) "${item.loggedTime} min" else "-"
            tvNotes.text = item.notes ?: "No notes"
            tvStatus.text = if (item.isCompleted) "Completed" else "Planned"
        }
    }
}
