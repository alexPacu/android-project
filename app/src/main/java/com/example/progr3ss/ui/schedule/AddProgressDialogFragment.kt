package com.example.progr3ss.ui.schedule

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.progr3ss.R
import com.google.android.material.button.MaterialButton

class AddProgressDialogFragment : DialogFragment() {

    private val viewModel: ScheduleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etNotes = view.findViewById<EditText>(R.id.etProgressNotes)
        val cbCompleted = view.findViewById<CheckBox>(R.id.cbCompleted)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)

        val scheduleId = arguments?.getInt("scheduleId") ?: -1
        val date = arguments?.getString("date") ?: ""
        val isUncompleting = arguments?.getBoolean("isUncompleting", false) ?: false

        if (isUncompleting) {
            cbCompleted.isChecked = false
            cbCompleted.visibility = View.GONE
        } else {
            cbCompleted.isChecked = true
        }

        fun dismissDialog() { dismissAllowingStateLoss() }

        btnCancel.setOnClickListener { dismissDialog() }

        btnSave.setOnClickListener {
            if (scheduleId != -1 && date.isNotBlank()) {
                val notes = etNotes.text?.toString()?.takeIf { it.isNotBlank() }
                val isCompleted = if (isUncompleting) false else cbCompleted.isChecked

                viewModel.createProgress(
                    scheduleId = scheduleId,
                    date = date,
                    notes = notes,
                    isCompleted = isCompleted
                )

                parentFragmentManager.setFragmentResult(
                    "progress_added",
                    bundleOf(
                        "scheduleId" to scheduleId,
                        "wasCompleted" to isCompleted
                    )
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
