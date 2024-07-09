package com.example.elevenbarbershop.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.elevenbarbershop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginAct : AppCompatActivity() {

    private lateinit var emailUsernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var toRegisterTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var showHidePasswordImageView: ImageView
    private lateinit var emailErrorTextView: TextView
    private lateinit var passwordErrorTextView: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailUsernameEditText = findViewById(R.id.email_username)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.btn_login)
        toRegisterTextView = findViewById(R.id.to_register)
        forgotPasswordTextView = findViewById(R.id.forgot_password)
        showHidePasswordImageView = findViewById(R.id.show_hide_password)
        emailErrorTextView = findViewById(R.id.email_error)
        passwordErrorTextView = findViewById(R.id.password_error)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("Users")

        loginButton.setOnClickListener {
            loginUser()
        }

        toRegisterTextView.setOnClickListener {
            startActivity(Intent(this@LoginAct, RegisterAct::class.java))
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

    private fun loginUser() {
        val emailUsername = emailUsernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        var isValid = true

        if (TextUtils.isEmpty(emailUsername)) {
            emailErrorTextView.setText(R.string.error_email_required)
            emailErrorTextView.visibility = TextView.VISIBLE
            isValid = false
        } else {
            emailErrorTextView.visibility = TextView.GONE
        }

        if (TextUtils.isEmpty(password)) {
            passwordErrorTextView.setText(R.string.error_password_required)
            passwordErrorTextView.visibility = TextView.VISIBLE
            isValid = false
        } else {
            passwordErrorTextView.visibility = TextView.GONE
        }

        if (!isValid) return

        if (emailUsername.contains("@")) {
            // It's an email, proceed to login
            signInWithEmail(emailUsername, password)
        } else {
            // It's a username, fetch the email associated with this username
            fetchEmailByUsername(emailUsername) { email ->
                if (email != null) {
                    signInWithEmail(email, password)
                } else {
                    showCustomToast(R.layout.failure_toast)
                }
            }
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    showCustomToast(R.layout.success_toast)
                    // Navigate to HomeAct or any other desired activity
                    val intent = Intent(this@LoginAct, MainAct::class.java)
                    startActivity(intent)
                    finish() // Close the current activity
                } else {
                    // If sign in fails, display a message to the user.
                    showCustomToast(R.layout.failure_toast)
                }
            }
    }

    private fun fetchEmailByUsername(username: String, callback: (String?) -> Unit) {
        database.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val email = userSnapshot.child("email").getValue(String::class.java)
                        callback(email)
                        return
                    }
                }
                callback(null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        })
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
