package com.example.progr3ss.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progr3ss.databinding.FragmentScheduleDetailsBinding

class ScheduleDetailsFragment : Fragment() {

    private var _binding: FragmentScheduleDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels()

    private lateinit var activityAdapter: ScheduleProgressAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityAdapter = ScheduleProgressAdapter()
        binding.recyclerActivities.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerActivities.adapter = activityAdapter

        val scheduleId = arguments?.getInt("scheduleId") ?: -1

        binding.btnEditSchedule.setOnClickListener {
            val bundle = Bundle().apply { putInt("scheduleId", scheduleId) }
            parentFragmentManager.setFragmentResult("edit_schedule_request", bundle)
        }

        observeViewModel()
        if (scheduleId != -1) {
            viewModel.loadScheduleById(scheduleId)
        }
    }

    private fun observeViewModel() {
        viewModel.selectedSchedule.observe(viewLifecycleOwner) { schedule ->
            if (schedule != null) {
                binding.tvHabitName.text = schedule.habit.name
                binding.tvScheduleStatus.text = schedule.status
                binding.tvNotes.text = schedule.notes ?: "No notes"

                binding.tvTimeRange.text = buildTimeRange(schedule.startTime, schedule.endTime)
                binding.tvDuration.text = schedule.durationMinutes?.let { "${it}m" } ?: "-"

                val partnersText = schedule.participants
                    ?.joinToString(", ") { participant ->
                        when (participant) {
                            is Map<*, *> ->
                                (participant["name"] as? String)
                                    ?: (participant["email"] as? String)
                                    ?: "-"
                            else -> participant.toString()
                        }
                    } ?: "None"
                binding.tvPartners.text = partnersText

                binding.tvRepeat.text = when (schedule.type) {
                    "recurring" -> "recurring"
                    else -> "custom"
                }

                val progressItems = schedule.progress
                    ?.mapNotNull { it as? com.example.progr3ss.model.ProgressResponseDto }
                    ?: emptyList()

                activityAdapter.submitList(progressItems.sortedByDescending { it.date })
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.tvError.visibility = if (error.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.tvError.text = error
        }
    }

    private fun buildTimeRange(start: String, end: String?): String {
        fun formatTime(raw: String): String {
            return try {
                val timePart = raw.substringAfter("T").substring(0, 5) // HH:mm
                val hour = timePart.substring(0, 2).toIntOrNull() ?: return timePart
                val minute = timePart.substring(3, 5)
                val amPm = if (hour < 12) "AM" else "PM"
                val hour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "$hour12:$minute $amPm"
            } catch (e: Exception) {
                raw
            }
        }

        return try {
            val startFormatted = formatTime(start)
            val endFormatted = end?.let { formatTime(it) }
            if (endFormatted != null) "$startFormatted - $endFormatted" else startFormatted
        } catch (e: Exception) {
            if (end != null) "$start - $end" else start
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
