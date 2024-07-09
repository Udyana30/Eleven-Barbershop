package com.example.elevenbarbershop.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.activity.LoginAct
import com.example.elevenbarbershop.databinding.FragmentProfileFrgBinding
import com.example.elevenbarbershop.view_model.MainVM

class ProfileFrg : Fragment() {

    private lateinit var mainVM: MainVM
    private lateinit var profileImageView: ImageView
    private lateinit var binding: FragmentProfileFrgBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainVM = ViewModelProvider(this)[MainVM::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileFrgBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImageView = view.findViewById(R.id.profile)

        // Observe ViewModel data
        mainVM.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                // Load profile image using Glide
                Glide.with(requireContext())
                    .load(user.pp)
                    .placeholder(R.drawable.profile_2)
                    .into(profileImageView)

                // Set username and email
                binding.yourEmail.text = user.email
                binding.username.text = user.username
            }
        }
        // Set click listeners for each LinearLayout
        binding.accountCenter.setOnClickListener {
            val account = AccountCenterFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, account)
                .addToBackStack(null)
                .commit()
        }

        binding.editProfile.setOnClickListener {
            val editProfile = EditProfileFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, editProfile)
                .addToBackStack(null)
                .commit()
        }

        binding.settings.setOnClickListener {
            val settings = SettingFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, settings)
                .addToBackStack(null)
                .commit()
        }

        binding.logout.setOnClickListener {
            mainVM.logout()
            val intent = Intent(requireContext(), LoginAct::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}