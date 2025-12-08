package com.example.progr3ss.ui.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.progr3ss.R
import com.google.android.material.button.MaterialButton
import android.widget.ImageButton
import androidx.core.os.bundleOf

class EditNotesDialogFragment : DialogFragment() {

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val etNotes = view.findViewById<EditText>(R.id.etNotes)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)

        val initialNotes = arguments?.getString("notes") ?: ""
        val scheduleId = arguments?.getInt("scheduleId") ?: -1
        etNotes.setText(initialNotes)

        fun dismissDialog() { dismissAllowingStateLoss() }

        btnCancel.setOnClickListener { dismissDialog() }
        btnClose.setOnClickListener { dismissDialog() }

        btnSave.setOnClickListener {
            val updatedNotes = etNotes.text?.toString()
            parentFragmentManager.setFragmentResult("notes_updated", bundleOf("notes" to (updatedNotes ?: "")))

            if (scheduleId != -1) {
                viewModel.updateSchedule(
                    id = scheduleId,
                    notes = updatedNotes
                )
            }
            dismissDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
