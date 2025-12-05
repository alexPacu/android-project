package com.example.progr3ss.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progr3ss.R
import com.example.progr3ss.model.HabitResponseDto

class HabitProgressAdapter : RecyclerView.Adapter<HabitProgressAdapter.HabitViewHolder>() {

    private val items = mutableListOf<HabitResponseDto>()

    fun submitList(newItems: List<HabitResponseDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit_progress, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvHabitName)
        private val tvPercent: TextView = itemView.findViewById(R.id.tvHabitPercent)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressHabit)

        fun bind(item: HabitResponseDto) {
            tvName.text = item.name
            val progress = 0
            tvPercent.text = "${progress}%"
            progressBar.progress = progress
        }
    }
}
