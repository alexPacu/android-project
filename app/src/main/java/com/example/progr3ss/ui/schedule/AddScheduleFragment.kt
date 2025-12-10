package com.example.progr3ss.ui.schedule

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.R
import com.example.progr3ss.databinding.FragmentAddScheduleBinding
import com.example.progr3ss.model.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import android.widget.AdapterView

class AddScheduleFragment : Fragment() {

    private lateinit var binding: FragmentAddScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels()

    private var selectedHabitId: Int? = null
    private var selectedTime: String = "08:00"
    private var selectedRepeatPattern: String = "once"
    private val habitList = mutableListOf<HabitResponseDto>()
    private val categoryList = mutableListOf<CategoryDto>()
    private val selectedCustomDays = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_schedule,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupViews()

        viewModel.loadHabits()
        viewModel.loadCategories()
    }

    private fun setupObservers() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            habitList.clear()
            habitList.addAll(habits)
            setupHabitSpinner()
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryList.clear()
            categoryList.addAll(categories)
        }


        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.scheduleCreated.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Schedule created successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun setupViews() {
        val goalUnits = resources.getStringArray(R.array.goal_units)
        val unitAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, goalUnits)
        unitAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)
        binding.spinnerGoalUnit.adapter = unitAdapter

        binding.cardTime.setOnClickListener { showTimePickerForStart() }
        // binding.cardEndTime.setOnClickListener { showTimePickerForEnd() }

        setupRepeatButtons()
        setupCustomDayCheckboxes()

        binding.etGoalAmount.setOnFocusChangeListener { _, _ -> syncEndTimeWithDuration() }
        binding.etGoalAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) { syncEndTimeWithDuration() }
        })

        binding.spinnerGoalUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                syncEndTimeWithDuration()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnCreate.setOnClickListener { createSchedule() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    private fun showTimePickerForStart() {
        val parts = selectedTime.split(":")
        val sh = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val sm = parts.getOrNull(1)?.toIntOrNull() ?: 0
        TimePickerDialog(requireContext(), { _, h, m ->
            selectedTime = String.format(Locale.US, "%02d:%02d", h, m)
            binding.tvTimeValue.text = formatTime(h, m)
            syncEndTimeWithDuration()
        }, sh, sm, true).show()
    }

    // private fun showTimePickerForEnd() {
    // }

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    private fun setupRepeatButtons() {
        val buttons = listOf(
            binding.btnOnce to "once",
            binding.btnEveryDay to "daily",
            binding.btnWeekdays to "weekdays",
            binding.btnWeekends to "weekends",
            binding.btnCustom to "custom"
        )

        buttons.forEach { (button, pattern) ->
            button.setOnClickListener {
                selectedRepeatPattern = pattern
                updateRepeatButtonsUI(button)
                binding.cardCustomDays.visibility = if (pattern == "custom") View.VISIBLE else View.GONE
            }
        }

        updateRepeatButtonsUI(binding.btnOnce)
        binding.cardCustomDays.visibility = View.GONE
    }

    private fun setupCustomDayCheckboxes() {
        val checkboxes = listOf(
            binding.cbMonday to 1,
            binding.cbTuesday to 2,
            binding.cbWednesday to 3,
            binding.cbThursday to 4,
            binding.cbFriday to 5,
            binding.cbSaturday to 6,
            binding.cbSunday to 7
        )

        checkboxes.forEach { (checkbox, dayNumber) ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!selectedCustomDays.contains(dayNumber)) {
                        selectedCustomDays.add(dayNumber)
                    }
                } else {
                    selectedCustomDays.remove(dayNumber)
                }
            }
        }
    }

    private fun updateRepeatButtonsUI(selectedButton: com.google.android.material.button.MaterialButton) {
        val buttons = listOf(
            binding.btnOnce,
            binding.btnEveryDay,
            binding.btnWeekdays,
            binding.btnWeekends,
            binding.btnCustom
        )

        buttons.forEach { button ->
            if (button == selectedButton) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            } else {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.dark_card))
            }
        }
    }

    private fun createSchedule() {
        if (selectedHabitId == null) {
            Toast.makeText(requireContext(), "Please select a habit", Toast.LENGTH_SHORT).show()
            return
        }
        createScheduleWithHabitId()
    }

    private fun setupHabitSpinner() {
        val habitOptions = mutableListOf("", "+ Create New Habit")
        habitOptions.addAll(habitList.map { it.name })

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, habitOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)

        binding.spinnerHabit.adapter = adapter
        binding.spinnerHabit.setSelection(0, false)

        binding.spinnerHabit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 1) {
                    binding.spinnerHabit.setSelection(0, false)
                    showCreateHabitDialog()
                    return
                }
                if (position > 1) {
                    val habitIndex = position - 2
                    if (habitIndex < habitList.size) {
                        selectedHabitId = habitList[habitIndex].id
                    }
                } else {
                    selectedHabitId = null
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedHabitId = null
            }
        }
    }

    private fun syncEndTimeWithDuration() {
        val amount = binding.etGoalAmount.text.toString().toIntOrNull() ?: return
        val unit = binding.spinnerGoalUnit.selectedItem?.toString()?.lowercase(Locale.getDefault())
        val addMinutes = if (unit == "hours") amount * 60 else amount
        val parts = selectedTime.split(":")
        val sh = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val sm = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, sh)
        cal.set(Calendar.MINUTE, sm)
        cal.add(Calendar.MINUTE, addMinutes)
        val eh = cal.get(Calendar.HOUR_OF_DAY)
        val em = cal.get(Calendar.MINUTE)
        binding.tvEndTimeValue.text = String.format(Locale.US, "%02d:%02d", eh, em)
    }

    private fun syncDurationWithEndTime() {
        val startParts = selectedTime.split(":")
        val sh = startParts.getOrNull(0)?.toIntOrNull() ?: 8
        val sm = startParts.getOrNull(1)?.toIntOrNull() ?: 0
        val endParts = binding.tvEndTimeValue.text.toString().split(":")
        val eh = endParts.getOrNull(0)?.toIntOrNull() ?: sh
        val em = endParts.getOrNull(1)?.toIntOrNull() ?: sm
        val start = sh * 60 + sm
        val end = eh * 60 + em
        var diff = end - start
        if (diff < 0) diff += 24 * 60
        val unit = binding.spinnerGoalUnit.selectedItem?.toString()?.lowercase(Locale.getDefault())
        if (unit == "hours") {
            val hours = diff / 60
            binding.etGoalAmount.setText(hours.toString())
        } else {
            binding.etGoalAmount.setText(diff.toString())
        }
    }

    private fun createScheduleWithHabitId() {
        if (selectedHabitId == null) {
            Toast.makeText(requireContext(), "Error: Habit not found", Toast.LENGTH_SHORT).show()
            return
        }

        val durationText = binding.etGoalAmount.text.toString()
        val unitSelected = binding.spinnerGoalUnit.selectedItem?.toString()?.lowercase(Locale.getDefault())
        val durationMinutes: Int? = durationText.toIntOrNull()?.let { amount ->
            when (unitSelected) {
                "hours" -> amount * 60
                "minutes" -> amount
                else -> amount
            }
        }

        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormatter.timeZone = TimeZone.getTimeZone("UTC")
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormatter.timeZone = TimeZone.getTimeZone("UTC")

        val today = Date()
        val currentDate = dateFormatter.format(today)

        val parts = selectedTime.split(":")
        val sh = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val sm = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.time = today
        cal.set(Calendar.HOUR_OF_DAY, sh)
        cal.set(Calendar.MINUTE, sm)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startTimeIso = isoFormatter.format(cal.time)

        val endDateTimeIso: String? = durationMinutes?.let { mins ->
            val endCal = cal.clone() as Calendar
            endCal.add(Calendar.MINUTE, mins)
            isoFormatter.format(endCal.time)
        }

        when (selectedRepeatPattern) {
            "daily", "weekdays", "weekends" -> {
                val request = CreateRecurringScheduleRequest(
                    habitId = selectedHabitId!!,
                    startTime = startTimeIso,
                    repeatPattern = selectedRepeatPattern,
                    endTime = endDateTimeIso,
                    durationMinutes = durationMinutes,
                    repeatDays = 30,
                    isCustom = true,
                    participantIds = null,
                    notes = null
                )
                viewModel.createRecurringSchedule(request)
            }
            "custom" -> {
                if (selectedCustomDays.isEmpty()) {
                    Toast.makeText(requireContext(), "Please select at least one day", Toast.LENGTH_SHORT).show()
                    return
                }
                val request = CreateWeekdayRecurringScheduleRequest(
                    habitId = selectedHabitId!!,
                    startTime = startTimeIso,
                    daysOfWeek = selectedCustomDays.sorted(),
                    numberOfWeeks = 4,
                    durationMinutes = durationMinutes,
                    endTime = endDateTimeIso,
                    participantIds = null,
                    notes = null
                )
                viewModel.createWeekdayRecurringSchedule(request)
            }
            else -> {
                val request = CreateCustomScheduleRequest(
                    habitId = selectedHabitId!!,
                    date = currentDate,
                    startTime = startTimeIso,
                    endTime = endDateTimeIso,
                    durationMinutes = durationMinutes,
                    isCustom = true,
                    participantIds = null,
                    notes = null
                )
                viewModel.createCustomSchedule(request)
            }
        }
    }

    private fun showCreateHabitDialog() {
        if (categoryList.isEmpty()) {
            viewModel.loadCategories()
            Toast.makeText(requireContext(), "Loading categories...", Toast.LENGTH_SHORT).show()
            return
        }
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_habit, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        val etHabitName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHabitName)
        val etHabitDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHabitDescription)
        val spinnerCategory = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)
        val etHabitGoal = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etHabitGoal)
        val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancelHabit)
        val btnCreate = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCreateHabit)

        val categoryNames = categoryList.map { it.name }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnCreate.setOnClickListener {
            val name = etHabitName.text?.toString()?.trim().orEmpty()
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Enter habit name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val description = etHabitDescription.text?.toString()?.trim().orEmpty()
            val goalInput = etHabitGoal.text?.toString()
            val goal = if (goalInput.isNullOrBlank()) "Complete $name" else goalInput.trim()
            val pos = spinnerCategory.selectedItemPosition
            if (pos < 0 || pos >= categoryList.size) {
                Toast.makeText(requireContext(), "Select category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val categoryId = categoryList[pos].id
            viewModel.createHabit(name, if (description.isEmpty()) null else description, categoryId, goal)
            dialog.dismiss()
            viewModel.loadHabits()
        }
        dialog.show()
    }
}
