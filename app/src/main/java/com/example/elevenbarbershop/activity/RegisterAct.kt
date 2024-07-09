package com.example.elevenbarbershop.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterAct : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var showHidePasswordImageView: ImageView
    private lateinit var registerButton: Button
    private lateinit var toLoginTextView: TextView
    private lateinit var emailErrorTextView: TextView
    private lateinit var usernameErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView

    private var isPasswordVisible = false

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailEditText = findViewById(R.id.email)
        usernameEditText = findViewById(R.id.username)
        passwordEditText = findViewById(R.id.password)
        toLoginTextView = findViewById(R.id.to_login)
        registerButton = findViewById(R.id.btn_register)
        emailErrorTextView = findViewById(R.id.email_error)
        usernameErrorTextView = findViewById(R.id.username_error)
        passwordErrorTextView = findViewById(R.id.password_error)
        showHidePasswordImageView = findViewById(R.id.show_hide_password)

        mAuth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener {
            registerUser()
        }

        toLoginTextView.setOnClickListener{
            startActivity(Intent(this@RegisterAct, LoginAct::class.java))
        }

        showHidePasswordImageView.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Change EditText to password mode
            passwordEditText.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            showHidePasswordImageView.setImageResource(R.drawable.removepw)
        } else {
            // Change EditText to normal text mode
            passwordEditText.inputType =
                android.text.InputType.TYPE_CLASS_TEXT or
                        android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            showHidePasswordImageView.setImageResource(R.drawable.showpw)
        }

        // Move cursor to the end of the text
        passwordEditText.setSelection(passwordEditText.text.length)

        // Toggle flag
        isPasswordVisible = !isPasswordVisible
    }

    private fun registerUser() {
        val email = emailEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Reset error messages
        emailErrorTextView.visibility = TextView.GONE
        usernameErrorTextView.visibility = TextView.GONE
        passwordErrorTextView.visibility = TextView.GONE

        // Validasi format email menggunakan regex
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!email.matches(emailPattern.toRegex())) {
            emailErrorTextView.setText(R.string.error_format_email)
            emailErrorTextView.visibility = TextView.VISIBLE
            return
        }

        // Validate username format and length
        val usernamePattern = "^[a-z]{1,8}$"
        if (!username.matches(usernamePattern.toRegex())) {
            usernameErrorTextView.setText(R.string.error_format_username)
            usernameErrorTextView.visibility = TextView.VISIBLE
            return
        }

        // Validasi keberadaan karakter uppercase dan angka dalam password
        val uppercasePattern = "(.*[A-Z].*)"
        val digitPattern = "(.*[0-9].*)"
        if (!password.matches(uppercasePattern.toRegex()) || !password.matches(digitPattern.toRegex())) {
            passwordErrorTextView.setText(R.string.error_password_patern)
            passwordErrorTextView.visibility = TextView.VISIBLE
            return
        }

        if (TextUtils.isEmpty(email)) {
            emailErrorTextView.setText(R.string.error_only_email_required)
            emailErrorTextView.visibility = TextView.VISIBLE
            return
        }

        if (TextUtils.isEmpty(password)) {
            passwordErrorTextView.setText(R.string.password)
            passwordErrorTextView.visibility = TextView.VISIBLE
            return
        }

        if (password.length < 6) {
            passwordErrorTextView.setText(R.string.error_password_length)
            passwordErrorTextView.visibility = TextView.VISIBLE
            return
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = mAuth.currentUser?.uid
                    if (userId != null) {
                        val user = UserModel(email, username, userId = userId)
                        saveUserToDataBase(userId, user)
                        showCustomToast(R.layout.success_toast)
                        // Navigate to HomeAct or any other desired activity
                        val intent = Intent(this@RegisterAct, PersonalAct::class.java)
                        startActivity(intent)
                        finish() // Close the current activity
                    } else {
                        showCustomToast(R.layout.failure_toast)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    showCustomToast(R.layout.failure_toast)
                }
            }
    }

    private fun saveUserToDataBase(userId: String, user: UserModel) {
        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(userId).setValue(user).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showCustomToast(R.layout.success_toast)
            } else {
                showCustomToast(R.layout.failure_toast)
            }
        }
    }

    private fun showCustomToast(layoutId: Int) {
        val inflater = layoutInflater
        val layout = inflater.inflate(layoutId, findViewById(R.id.custom_toast))

        val toast = Toast(applicationContext).apply {
            duration = Toast.LENGTH_LONG
            view = layout
            setGravity(Gravity.CENTER, 0, 0)
        }
        toast.show()
    }
}