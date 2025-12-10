package com.example.progr3ss.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.R
import com.example.progr3ss.databinding.FragmentEditProfileBinding
import com.example.progr3ss.model.ProfileResponseDto
import java.io.File
import java.io.FileOutputStream

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val file = copyUriToCache(uri)
            if (file != null) {
                viewModel.uploadProfileImage(file)
            } else {
                Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.findViewById<View>(R.id.btnBack).setOnClickListener { findNavController().popBackStack() }
        binding.toolbar.findViewById<View>(R.id.btnSave).setOnClickListener { saveProfile() }
        binding.btnChangePhoto.setOnClickListener { openImagePicker() }

        observeViewModel()
        viewModel.loadProfile()
    }

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) fillUi(profile)
        }
        viewModel.profileImageBitmap.observe(viewLifecycleOwner) { bmp ->
            if (bmp != null) {
                binding.ivProfilePicture.setImageBitmap(bmp)
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { err ->
            if (!err.isNullOrEmpty()) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
        }
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSave.isEnabled = !isLoading
            binding.btnChangePhoto.isEnabled = !isLoading
        }
    }

    private fun saveProfile() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val bio = binding.etBio.text?.toString()?.trim().orEmpty()
        viewModel.updateProfile(username.ifBlank { null }, bio.ifBlank { null })
        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val pickIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        val chooser = Intent.createChooser(intent, "Select Profile Image").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        }
        pickImageLauncher.launch(chooser)
    }

    private fun copyUriToCache(uri: Uri): File? {
        return try {
            val name = (queryDisplayName(uri) ?: ("upload_${System.currentTimeMillis()}" + guessExtension(uri)))
            val dest = File(requireContext().cacheDir, name)
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
            val mime = resolveMimeType(uri)
            viewModel.uploadProfileImageWithMime(dest, mime)
            dest
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveMimeType(uri: Uri): String? {
        val cr = requireContext().contentResolver
        return cr.getType(uri) ?: run {
            val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            if (ext.isNullOrBlank()) null else MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.lowercase())
        }
    }

    private fun guessExtension(uri: Uri): String {
        val type = resolveMimeType(uri) ?: return ".jpg"
        return when (type.lowercase()) {
            "image/jpeg" -> ".jpg"
            "image/jpg" -> ".jpg"
            "image/png" -> ".png"
            else -> ".jpg"
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        val cr = requireContext().contentResolver
        val cursor = cr.query(uri, null, null, null, null) ?: return null
        cursor.use { c ->
            val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIdx >= 0 && c.moveToFirst()) return c.getString(nameIdx)
        }
        return null
    }

    private fun fillUi(p: ProfileResponseDto) {
        binding.etUsername.setText(p.username)
        binding.etEmail.setText(p.email)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
