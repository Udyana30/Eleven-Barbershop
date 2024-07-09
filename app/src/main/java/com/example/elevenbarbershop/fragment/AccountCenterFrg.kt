package com.example.elevenbarbershop.fragment

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.UserModel
import com.example.elevenbarbershop.view_model.MainVM
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hbb20.CountryCodePicker

@Suppress("DEPRECATION")
class AccountCenterFrg : Fragment() {

    private lateinit var mainVM: MainVM
    private lateinit var currentUser: UserModel
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var myEmail: EditText
    private lateinit var myUsername: EditText
    private lateinit var myPhone: EditText
    private lateinit var myPassword: EditText
    private lateinit var showPassword: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnVerifyEmail: Button
    private lateinit var backProfile: ImageView
    private var isPasswordVisible = false
    private var newEmail: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_center, container, false)

        backProfile = view.findViewById(R.id.logo)
        myEmail = view.findViewById(R.id.my_email)
        myUsername = view.findViewById(R.id.my_username)
        myPhone = view.findViewById(R.id.my_phone)
        myPassword = view.findViewById(R.id.my_password)
        showPassword = view.findViewById(R.id.show_hide_password)
        btnSave = view.findViewById(R.id.btn_save)
        btnVerifyEmail = view.findViewById(R.id.btn_verify)
        val ccp = view.findViewById<CountryCodePicker>(R.id.ccp)

        mainVM = ViewModelProvider(this)[MainVM::class.java]
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")

        mainVM.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = it
                myEmail.setText(it.email)
                myUsername.setText(it.username)
                val phone = it.phone
                ccp.setCountryForPhoneCode(getCountryCodeFromPhone(phone))
                val phoneWithoutCountryCode = phone.replaceFirst("+${getCountryCodeFromPhone(phone)}", "")
                myPhone.setText(phoneWithoutCountryCode)
                ccp.registerCarrierNumberEditText(myPhone)
            }
        }

        backProfile.setOnClickListener{
            val back = ProfileFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container,back)
                .addToBackStack(null)
                .commit()
        }

        showPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                checkEmailVerification()
            }
        }

        btnVerifyEmail.setOnClickListener {
            val email = myEmail.text.toString()
            val password = myPassword.text.toString()
            if (validateEmail(email) && validatePassword(password)) {
                newEmail = email
                reauthenticateAndSendVerification(email, password)
            }
        }

        return view
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            myPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            showPassword.setImageResource(R.drawable.removepw)
        } else {
            myPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showPassword.setImageResource(R.drawable.showpw)
        }
        myPassword.setSelection(myPassword.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun validateInput(): Boolean {
        var isValid = true
        if (myUsername.text.isBlank()) {
            myUsername.error = getString(R.string.empty_messages)
            isValid = false
        }
        if (myPhone.text.isBlank()) {
            myPhone.error = getString(R.string.empty_messages)
            isValid = false
        }
        return isValid
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isBlank()) {
            Toast.makeText(requireContext(), "Email cannot be empty", Toast.LENGTH_SHORT).show()
            false
        } else true
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.isBlank()) {
            Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    private fun reauthenticateAndSendVerification(email: String, password: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user?.email!!, password)

        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Reauth", "Reauthentication successful")
                sendVerificationLink(email)
            } else {
                Log.e("Reauth", "Reauthentication failed: ${task.exception?.message}")
                Toast.makeText(requireContext(), "Reauthentication failed. Please check your password.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendVerificationLink(email: String) {
        val actionCodeSettings = actionCodeSettings {
            url = "https://autheleven.page.link"
            handleCodeInApp = true
            iosBundleId = "com.example.ios"
            setAndroidPackageName(
                "com.example.android",
                true, // installIfNotAvailable
                "12" // minimumVersion
            )
        }

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("EmailVerification", "Verification email sent successfully")
                    Toast.makeText(requireContext(), "Verification email sent. Please check your email.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("EmailVerification", "Failed to send verification email: ${task.exception?.message}")
                    Toast.makeText(requireContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkEmailVerification() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.reload()?.addOnCompleteListener {
            if (user.isEmailVerified) {
                Log.d("EmailVerificationCheck", "Email is verified")
                newEmail?.let { email ->
                    updateUserInDatabase(email)
                }
            } else {
                Log.d("EmailVerificationCheck", "Email is not verified")
                Toast.makeText(requireContext(), "Please verify your email before updating your profile.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserInDatabase(newEmail: String) {
        val username = myUsername.text.toString()
        val phone = myPhone.text.toString()
        val ccp = view?.findViewById<CountryCodePicker>(R.id.ccp)
        val fullPhoneNumber = "+${ccp?.selectedCountryCode}${phone}"

        val updatedUser = currentUser.copy(email = newEmail, username = username, phone = fullPhoneNumber)

        mainVM.updateUserData(updatedUser)
        database.child(currentUser.userId).setValue(updatedUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.update_messages), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getCountryCodeFromPhone(phone: String): Int {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val number = phoneUtil.parse(phone, null)
            number.countryCode
        } catch (e: NumberParseException) {
            e.printStackTrace()
            0
        }
    }

}