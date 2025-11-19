package com.example.progr3ss.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.R

class ResetPasswordFragment : Fragment() {
    private lateinit var etEmailReset: EditText
    private lateinit var btnSendReset: Button

    private val viewModel: AuthViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reset_password, container, false)
        etEmailReset = view.findViewById(R.id.etEmailReset)
        btnSendReset = view.findViewById(R.id.btnSendReset)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnSendReset.setOnClickListener {
            val email = etEmailReset.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.resetPassword(email)
        }
        viewModel.resetResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { msg ->
                Toast.makeText(requireContext(), msg.message, Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }.onFailure { err ->
                Toast.makeText(requireContext(), "Reset failed: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

