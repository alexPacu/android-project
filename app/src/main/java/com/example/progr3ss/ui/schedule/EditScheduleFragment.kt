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

        binding.etDuration.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { recomputeEndTimeFromDuration() }
        })

        val units = listOf("minutes", "hours")
        val unitAdapter = android.widget.ArrayAdapter(requireContext(), com.example.progr3ss.R.layout.spinner_item_dark, units)
        unitAdapter.setDropDownViewResource(com.example.progr3ss.R.layout.spinner_dropdown_item_dark)
        binding.spinnerDurationUnit.adapter = unitAdapter
        binding.spinnerDurationUnit.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                recomputeEndTimeFromDuration()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

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
        // binding.etEndTime.setOnClickListener { showTimePicker(binding.etEndTime) }
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
                binding.etStartTime.setText(schedule.startTime.substringAfter("T").substring(0, 5))
                schedule.endTime?.let {
                    binding.etEndTime.setText(it.substringAfter("T").substring(0, 5))
                }
                binding.etDuration.setText(schedule.durationMinutes?.toString() ?: "")
                binding.etNotes.setText(schedule.notes ?: "")

                val participantsPrefill = schedule.participants?.mapNotNull { p ->
                    when (p) {
                        is Map<*, *> -> (p["id"] as? Number)?.toInt()
                        is Number -> p.toInt()
                        else -> null
                    }
                }?.joinToString(",") ?: ""
                binding.etParticipants.setText(participantsPrefill)

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

    private fun parseParticipants(text: String): List<Int>? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        return trimmed.split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .takeIf { it.isNotEmpty() }
    }

    private fun recomputeEndTimeFromDuration() {
        val start = binding.etStartTime.text.toString()
        val parts = start.split(":")
        if (parts.size < 2) return
        val sh = parts[0].toIntOrNull() ?: return
        val sm = parts[1].toIntOrNull() ?: return
        val amount = binding.etDuration.text.toString().toIntOrNull() ?: return
        val unit = binding.spinnerDurationUnit.selectedItem?.toString()?.lowercase(Locale.getDefault())
        val addMinutes = if (unit == "hours") amount * 60 else amount
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, sh)
        cal.set(java.util.Calendar.MINUTE, sm)
        cal.add(java.util.Calendar.MINUTE, addMinutes)
        val eh = cal.get(java.util.Calendar.HOUR_OF_DAY)
        val em = cal.get(java.util.Calendar.MINUTE)
        binding.etEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", eh, em))
    }

    private fun saveChanges() {
        val schedule = viewModel.selectedSchedule.value ?: return
        val startTimeInput = binding.etStartTime.text.toString()
        val endTimeInput = binding.etEndTime.text.toString()
        val amount = binding.etDuration.text.toString().toIntOrNull()
        val unit = binding.spinnerDurationUnit.selectedItem?.toString()?.lowercase(Locale.getDefault())
        val durationMinutes = amount?.let { if (unit == "hours") it * 60 else it }
        val notesInput = binding.etNotes.text.toString().ifBlank { null }
        val participantsInput = parseParticipants(binding.etParticipants.text.toString())
        val status = selectedStatus

        val date = schedule.date.substringBefore("T")
        val startIso = if (startTimeInput.isNotBlank()) "${date}T$startTimeInput:00.000Z" else null
        val endIso = if (endTimeInput.isNotBlank()) "${date}T$endTimeInput:00.000Z" else null

        viewModel.updateSchedule(
            id = schedule.id,
            startTime = startIso,
            endTime = endIso,
            durationMinutes = durationMinutes,
            status = status,
            date = schedule.date,
            isCustom = schedule.isCustom,
            participantIds = participantsInput,
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
