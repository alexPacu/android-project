package com.example.progr3ss.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progr3ss.R
import com.example.progr3ss.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var scheduleAdapter: ScheduleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        setupFab()

        binding.tvDate.text = viewModel.getCurrentDateFormatted()

        return binding.root
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter()
        binding.recyclerViewSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
    }

    private fun setupObservers() {
        viewModel.schedules.observe(viewLifecycleOwner) { schedules ->
            android.util.Log.d("HomeFragment", "===========================================")
            android.util.Log.d("HomeFragment", "📱 UI UPDATE - Schedules LiveData changed")
            android.util.Log.d("HomeFragment", "Received ${schedules.size} schedules in Fragment")

            scheduleAdapter.submitList(schedules)
            android.util.Log.d("HomeFragment", "Submitted ${schedules.size} schedules to adapter")

            if (schedules.isEmpty()) {
                android.util.Log.d("HomeFragment", "Showing empty state")
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerViewSchedules.visibility = View.GONE
            } else {
                android.util.Log.d("HomeFragment", "Showing RecyclerView with schedules")
                binding.emptyState.visibility = View.GONE
                binding.recyclerViewSchedules.visibility = View.VISIBLE
            }
            android.util.Log.d("HomeFragment", "===========================================")
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            android.util.Log.d("HomeFragment", "Loading state: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                android.util.Log.e("HomeFragment", "Error message: $error")
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFab() {
        binding.fabAddSchedule.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addScheduleFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("HomeFragment", "onResume - Loading today's schedules")
        viewModel.loadTodaySchedules()
    }
}