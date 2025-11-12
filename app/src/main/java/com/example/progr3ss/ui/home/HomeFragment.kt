package com.example.progr3ss.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val repository = ScheduleRepository(context)
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeScheduleAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the ViewModel using the custom factory
        val factory = HomeViewModelFactory(requireContext())
        viewModel = ViewModelProvider(this, factory)
        [HomeViewModel::class.java]
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupObservers()
    }
    private fun setupUi() {
        // Setup RecyclerView and adapter
        adapter = HomeScheduleAdapter()
        binding.rvSchedules.layoutManager =
            LinearLayoutManager(requireContext())
        binding.rvSchedules.adapter = adapter
        // Add a divider between list items

        binding.rvSchedules.addItemDecoration(
            DividerItemDecoration(
                requireContext
                    (), LinearLayoutManager.VERTICAL
            )
        )
        // Fetch today's schedules (format YYYY-MM-DD)
        val today = try {
            LocalDate.now().toString()
        } catch (_: Exception) {
            "2025-10-26"
        }
        viewModel.getScheduleByDay(today)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}