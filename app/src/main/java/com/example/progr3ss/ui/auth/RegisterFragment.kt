package com.example.progr3ss.ui.auth

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class RegisterFragment : Fragment() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnRegisterGoogle: Button
    private lateinit var tvGoToLogin: android.widget.TextView

    private val viewModel: RegisterViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
    }

    private var googleSignInClient: GoogleSignInClient? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrEmpty()) {
                viewModel.googleRegister(idToken)
            } else {
                Toast.makeText(requireContext(), "Google sign-in failed: missing token", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(requireContext(), "Google sign-in error: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        btnRegisterGoogle = view.findViewById(R.id.btnRegisterGoogle)
        tvGoToLogin = view.findViewById(R.id.tvGoToLogin)
        setupGoogleSignIn()
        return view
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id)) // Needs to be defined from google-services.json
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnRegister.setOnClickListener {
            clearPasswordErrorStyles()

            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                // Highlight both password fields with red border and update hints
                highlightPasswordMismatch()
                return@setOnClickListener
            }

            viewModel.register(username, email, password)
        }

        btnRegisterGoogle.setOnClickListener {
            googleSignInClient?.signOut() // ensure fresh sign-in
            val signInIntent = googleSignInClient?.signInIntent
            if (signInIntent != null) {
                googleSignInLauncher.launch(signInIntent)
            } else {
                Toast.makeText(requireContext(), "Google client not initialized", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_LONG).show()
                // Navigate to Home
                findNavController().navigate(R.id.homeFragment)
            }.onFailure { error ->
                Toast.makeText(requireContext(), "Registration failed: ${'$'}{error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun highlightPasswordMismatch() {
        val errorColor = Color.RED
        etPassword.background.setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP)
        etConfirmPassword.background.setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP)
        etPassword.error = "Passwords do not match"
        etConfirmPassword.error = "Passwords do not match"
    }

    private fun clearPasswordErrorStyles() {
        etPassword.background.clearColorFilter()
        etConfirmPassword.background.clearColorFilter()
        etPassword.error = null
        etConfirmPassword.error = null
    }
}