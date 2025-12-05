package com.example.progr3ss.ui.schedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.databinding.FragmentEditScheduleBinding
import java.util.Locale

class EditScheduleFragment : Fragment() {

    private var _binding: FragmentEditScheduleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ScheduleViewModel by viewModels()

    private var selectedStatus: String = "Planned"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatusButtons()
        setupTimePickers()

        binding.btnSave.setOnClickListener { saveChanges() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }

        observeViewModel()
        val scheduleId = arguments?.getInt("scheduleId") ?: -1
        if (scheduleId != -1) {
            viewModel.loadScheduleById(scheduleId)
        }
    }

    private fun setupStatusButtons() {
        fun select(status: String) {
            selectedStatus = status
            val primary = 0xFF6C63FF.toInt()
            val dark = 0xFF25253A.toInt()

            fun style(btn: com.google.android.material.button.MaterialButton, isSelected: Boolean) {
                btn.setBackgroundColor(if (isSelected) primary else dark)
            }

            style(binding.btnStatusPlanned, status == "Planned")
            style(binding.btnStatusSkipped, status == "Skipped")
            style(binding.btnStatusCompleted, status == "Completed")
        }

        binding.btnStatusPlanned.setOnClickListener { select("Planned") }
        binding.btnStatusSkipped.setOnClickListener { select("Skipped") }
        binding.btnStatusCompleted.setOnClickListener { select("Completed") }

        select(selectedStatus)
    }

    private fun setupTimePickers() {
        binding.etStartTime.setOnClickListener { showTimePicker(binding.etStartTime) }
        binding.etEndTime.setOnClickListener { showTimePicker(binding.etEndTime) }
    }

    private fun showTimePicker(target: android.widget.EditText) {
        val current = target.text.toString()
        val parts = if (current.contains(":")) current.split(":") else listOf("8", "0")
        val hour = parts[0].toIntOrNull() ?: 8
        val minute = parts[1].toIntOrNull() ?: 0
        TimePickerDialog(requireContext(), { _, h, m ->
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m))
        }, hour, minute, true).show()
    }

    private fun observeViewModel() {
        viewModel.selectedSchedule.observe(viewLifecycleOwner) { schedule ->
            if (schedule != null) {
                // If you add a title TextView for the habit in the layout, bind it here
                // binding.tvHabitName.text = schedule.habit.name
                binding.etStartTime.setText(schedule.startTime.substringAfter("T").substring(0, 5))
                schedule.endTime?.let {
                    binding.etEndTime.setText(it.substringAfter("T").substring(0, 5))
                }
                binding.etDuration.setText(schedule.durationMinutes?.toString() ?: "")
                binding.etNotes.setText(schedule.notes ?: "")

                selectedStatus = when (schedule.status) {
                    "Planned" -> "Planned"
                    "Completed" -> "Completed"
                    "Skipped" -> "Skipped"
                    else -> "Planned"
                }

                setupStatusButtons()
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveChanges() {
        val schedule = viewModel.selectedSchedule.value ?: return
        val startTimeInput = binding.etStartTime.text.toString()
        val endTimeInput = binding.etEndTime.text.toString()
        val durationInput = binding.etDuration.text.toString().toIntOrNull()
        val notesInput = binding.etNotes.text.toString().ifBlank { null }
        val status = selectedStatus

        val date = schedule.date.substringBefore("T")
        val startIso = if (startTimeInput.isNotBlank()) "${date}T$startTimeInput:00.000Z" else null
        val endIso = if (endTimeInput.isNotBlank()) "${date}T$endTimeInput:00.000Z" else null

        viewModel.updateSchedule(
            id = schedule.id,
            startTime = startIso,
            endTime = endIso,
            durationMinutes = durationInput,
            status = status,
            date = schedule.date,
            isCustom = schedule.isCustom,
            participantIds = null,
            notes = notesInput
        )
        Toast.makeText(requireContext(), "Schedule updated", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
