package com.example.elevenbarbershop.etc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.example.elevenbarbershop.R

class FilterDialogFragment(
    private val applyFilters: (String, String) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_filter, container, false)

        val typeSpinner: Spinner = view.findViewById(R.id.typeSpinner)
        val faceSpinner: Spinner = view.findViewById(R.id.faceShapeSpinner)
        val applyButton: Button = view.findViewById(R.id.apply)

        applyButton.setOnClickListener {
            val selectedType = typeSpinner.selectedItem.toString()
            val selectedFace = faceSpinner.selectedItem.toString()
            applyFilters(selectedType, selectedFace)
            dismiss()
        }

        return view
    }
}
