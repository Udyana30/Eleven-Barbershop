package com.example.elevenbarbershop.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.elevenbarbershop.R

class SettingFrg : Fragment() {
    private lateinit var backProfile: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting_frg, container, false)

        backProfile = view.findViewById(R.id.logo)

        backProfile.setOnClickListener{
            val back = ProfileFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, back)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

}
