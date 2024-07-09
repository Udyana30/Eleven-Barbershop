package com.example.elevenbarbershop.fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.UserModel
import com.example.elevenbarbershop.view_model.MainVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfileFrg : Fragment() {

    private lateinit var mainVM: MainVM
    private lateinit var profileImageView: CircleImageView
    private lateinit var editName: EditText
    private lateinit var editDate: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var editAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var backProfile: ImageView
    private lateinit var editPPImageView: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUser: UserModel
    private val calendar = Calendar.getInstance()

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                updateProfilePicture(it)
                uploadProfilePicture(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        profileImageView = view.findViewById(R.id.profile)
        editName = view.findViewById(R.id.my_name)
        editDate = view.findViewById(R.id.editTextDate)
        genderSpinner = view.findViewById(R.id.gender_list)
        editAddress = view.findViewById(R.id.your_address)
        btnSave = view.findViewById(R.id.btn_save)
        backProfile = view.findViewById(R.id.logo)
        editPPImageView = view.findViewById(R.id.edit_pp)

        mainVM = ViewModelProvider(this)[MainVM::class.java]
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")
        storage = FirebaseStorage.getInstance()

        mainVM.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = it
                editName.setText(it.name)
                editDate.setText(it.birth)
                editAddress.setText(it.address)
                // Set gender spinner based on user.gender
                val genderArray = resources.getStringArray(R.array.gender_array)
                val genderIndex = genderArray.indexOf(it.gender)
                if (genderIndex >= 0) {
                    genderSpinner.setSelection(genderIndex)
                }
                // Load profile image using Glide
                Glide.with(this)
                    .load(it.pp)
                    .placeholder(R.drawable.profile_2)
                    .into(profileImageView)
            }
        }

        backProfile.setOnClickListener{
            val back = ProfileFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, back)
                .addToBackStack(null)
                .commit()
        }

        editPPImageView.setOnClickListener {
            pickImageFromGallery()
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                updateUserData()
                val back = ProfileFrg()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, back)
                    .addToBackStack(null)
                    .commit()
            }
        }
        setupDatePicker()
        return view
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        editDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateLabel() {
        val format = "dd/MM/yyyy" // Change the format as needed
        val sdf = SimpleDateFormat(format, Locale.US)
        editDate.setText(sdf.format(calendar.time))
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (editName.text.isBlank()) {
            editName.error = getString(R.string.empty_messages)
            isValid = false
        }
        if (editDate.text.isBlank()) {
            editDate.error = getString(R.string.empty_messages)
            isValid = false
        }
        if (editAddress.text.isBlank()) {
            editAddress.error = getString(R.string.email_username)
            isValid = false
        }
        return isValid
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    private fun updateProfilePicture(imageUri: Uri) {
        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.profile_2)
            .into(profileImageView)
    }

    private fun uploadProfilePicture(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageRef = storage.reference.child("userprofile").child("$userId.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveProfileImageUrl(imageUrl)
                        updateProfileImageView(imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileImageUrl(imageUrl: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            database.child(userId).child("pp").setValue(imageUrl)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Profile image URL saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save profile image URL", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateProfileImageView(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.profile_2)
            .into(profileImageView)
    }

    private fun updateUserData() {
        val name = editName.text.toString()
        val birth = editDate.text.toString()
        val address = editAddress.text.toString()
        val gender = genderSpinner.selectedItem.toString()
        val currentUser = mainVM.userData.value

        val updatedUser = currentUser?.copy(name = name, birth = birth, address = address, gender = gender)

        updatedUser?.let {
            mainVM.updateUserData(it)
            Toast.makeText(context, getString(R.string.update_messages), Toast.LENGTH_SHORT).show()
        }
    }
}