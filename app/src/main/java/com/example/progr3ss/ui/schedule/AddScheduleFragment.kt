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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.R
import com.example.progr3ss.databinding.FragmentAddScheduleBinding
import com.example.progr3ss.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleFragment : Fragment() {

    private lateinit var binding: FragmentAddScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels()

    private var selectedHabitId: Int? = null
    private var selectedTime: String = "08:00"
    private var selectedRepeatPattern: String = "once"
    private val habitList = mutableListOf<HabitResponseDto>()
    private val categoryList = mutableListOf<CategoryDto>()
    private val selectedCustomDays = mutableListOf<Int>()
    private var habitSpinnerInitialized = false
    private var suppressHabitSelectionEvent = false

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

        binding.cardTime.setOnClickListener {
            showTimePicker()
        }

        setupRepeatButtons()
        setupCustomDayCheckboxes()

        binding.btnCreate.setOnClickListener {
            createSchedule()
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupHabitSpinner() {
        val habitOptions = mutableListOf("", "+ Create New Habit")
        habitOptions.addAll(habitList.map { it.name })

        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_dark, habitOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark)

        binding.spinnerHabit.adapter = adapter
        binding.spinnerHabit.setSelection(0, false)

        habitSpinnerInitialized = false
        suppressHabitSelectionEvent = false

        binding.spinnerHabit.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!habitSpinnerInitialized) {
                    habitSpinnerInitialized = true
                    return
                }

                if (suppressHabitSelectionEvent) {
                    suppressHabitSelectionEvent = false
                    return
                }

                if (position == 1) {
                    suppressHabitSelectionEvent = true
                    binding.spinnerHabit.setSelection(0, false)
                    showCreateHabitDialog()
                    return
                }

                if (position > 1) {
                    val habitIndex = position - 2
                    if (habitIndex >= 0 && habitIndex < habitList.size) {
                        selectedHabitId = habitList[habitIndex].id
                    }
                } else {
                    selectedHabitId = null
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedHabitId = null
            }
        }
    }

    private fun showCreateHabitDialog() {
        if (categoryList.isEmpty()) {
            Toast.makeText(requireContext(), "Loading categories, please wait...", Toast.LENGTH_SHORT).show()
            viewModel.loadCategories()
            lifecycleScope.launch {
                delay(1000)
                if (categoryList.isNotEmpty()) {
                    showCreateHabitDialog()
                } else {
                    Toast.makeText(requireContext(), "Failed to load categories. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_create_habit, null)
        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

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

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnCreate.setOnClickListener {
            val name = etHabitName.text.toString().trim()
            val description = etHabitDescription.text.toString().trim().ifEmpty { null }
            val categoryPosition = spinnerCategory.selectedItemPosition
            val goalText = etHabitGoal.text.toString().trim()
            val goal = goalText.ifEmpty { "Complete $name consistently" }

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter habit name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (categoryPosition < 0 || categoryPosition >= categoryList.size) {
                Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val categoryId = categoryList[categoryPosition].id

            lifecycleScope.launch {
                try {
                    viewModel.createHabit(name, description, categoryId, goal)
                    delay(1000)
                    viewModel.loadHabits()
                    delay(500)

                    val newHabit = habitList.find { it.name.equals(name, ignoreCase = true) }
                    if (newHabit != null) {
                        selectedHabitId = newHabit.id
                        val habitIndex = habitList.indexOf(newHabit)
                        suppressHabitSelectionEvent = true
                        binding.spinnerHabit.setSelection(habitIndex + 2, false)
                        Toast.makeText(requireContext(), "Habit created successfully!", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Failed to create habit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            selectedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
            binding.tvTimeValue.text = formatTime(selectedHour, selectedMinute)
        }, hour, minute, true).show()
    }

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

    private fun createScheduleWithHabitId() {
        if (selectedHabitId == null) {
            Toast.makeText(requireContext(), "Error: Habit not found", Toast.LENGTH_SHORT).show()
            return
        }

        val durationText = binding.etGoalAmount.text.toString()
        val durationMinutes = if (durationText.isNotEmpty()) durationText.toIntOrNull() else null
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val startTimeIso = "${currentDate}T${selectedTime}:00.000Z"


        when (selectedRepeatPattern) {
            "daily", "weekdays", "weekends" -> {
                val request = CreateRecurringScheduleRequest(
                    habitId = selectedHabitId!!,
                    startTime = startTimeIso,
                    repeatPattern = selectedRepeatPattern,
                    endTime = null,
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
                    endTime = null,
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
                    endTime = null,
                    durationMinutes = durationMinutes,
                    isCustom = true,
                    participantIds = null,
                    notes = null
                )
                viewModel.createCustomSchedule(request)
            }
        }
    }
}
