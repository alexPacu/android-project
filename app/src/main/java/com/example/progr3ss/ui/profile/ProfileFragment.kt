package com.example.progr3ss.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.progr3ss.R
import com.example.progr3ss.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val habitAdapter = HabitProgressAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHabits.adapter = habitAdapter

        binding.btnAddHabit.setOnClickListener {
            findNavController().navigate(R.id.addScheduleFragment)
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        observeViewModel()
        viewModel.loadProfile()
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile == null) {
                binding.tvUsername.text = ""
                binding.tvEmail.text = ""
                binding.tvBio.visibility = View.GONE
            } else {
                binding.tvUsername.text = profile.username
                binding.tvEmail.text = profile.email

                val bio = profile.description
                if (!bio.isNullOrBlank()) {
                    binding.tvBio.text = bio
                    binding.tvBio.visibility = View.VISIBLE
                } else {
                    binding.tvBio.visibility = View.GONE
                }
            }
        }

        viewModel.profileImageBitmap.observe(viewLifecycleOwner) { bmp ->
            if (bmp != null) {
                binding.ivProfilePicture.setImageBitmap(bmp)
            } else {
                binding.ivProfilePicture.setImageResource(android.R.drawable.ic_menu_myplaces)
            }
        }

        viewModel.habitWeeklyProgress.observe(viewLifecycleOwner) { progressMap ->
            val habits = viewModel.habits.value.orEmpty()
            habitAdapter.submitList(habits, progressMap ?: emptyMap())
        }

        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            if (habits.isNullOrEmpty()) {
                binding.tvNoHabits.visibility = View.VISIBLE
                binding.rvHabits.visibility = View.GONE
            } else {
                binding.tvNoHabits.visibility = View.GONE
                binding.rvHabits.visibility = View.VISIBLE

                val progressMap = viewModel.habitWeeklyProgress.value ?: emptyMap()
                habitAdapter.submitList(habits, progressMap)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
        }

        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().navigate(R.id.loginFragment)
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
