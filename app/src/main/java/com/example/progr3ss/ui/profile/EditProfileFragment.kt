package com.example.progr3ss.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progr3ss.R
import com.example.progr3ss.databinding.FragmentEditProfileBinding
import com.example.progr3ss.model.ProfileResponseDto
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data ?: return@registerForActivityResult
            val originalBmp = decodeBitmapFromUri(uri)
            if (originalBmp == null) {
                Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            showCropDialog(originalBmp) { croppedBitmap ->
                binding.ivProfilePicture.setImageBitmap(croppedBitmap)
                val file = saveBitmapToCache(croppedBitmap)
                if (file != null) {
                    viewModel.uploadProfileImageWithMime(file, "image/jpeg")
                } else {
                    Toast.makeText(requireContext(), "Failed to prepare image for upload", Toast.LENGTH_SHORT).show()
                }
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

        viewModel.profileUpdateSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                viewModel.clearProfileUpdateFlag()
            }
        }
    }

    private fun saveProfile() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val bio = binding.etBio.text?.toString()?.trim().orEmpty()
        viewModel.updateProfile(username.ifBlank { null }, bio.ifBlank { null })
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

    private fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun showCropDialog(source: Bitmap, onCropped: (Bitmap) -> Unit) {
        val size = min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val square = try {
            Bitmap.createBitmap(source, x, y, size, size)
        } catch (_: Exception) {
            source
        }

        val imageView = ImageView(requireContext()).apply {
            setImageBitmap(square)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Crop profile image")
            .setView(imageView)
            .setPositiveButton("Use") { _, _ ->
                onCropped(square)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveBitmapToCache(bitmap: Bitmap): File? {
        return try {
            val file = File(requireContext().cacheDir, "profile_crop_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file
        } catch (_: Exception) {
            null
        }
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
