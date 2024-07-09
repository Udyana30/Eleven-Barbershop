package com.example.elevenbarbershop.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.elevenbarbershop.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hbb20.CountryCodePicker
import java.text.SimpleDateFormat
import java.util.*

class PersonalAct : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var birthEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var phoneEditText: EditText
    private lateinit var ccp: CountryCodePicker
    private lateinit var saveButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val calendar = Calendar.getInstance()
    private lateinit var genderAdapter: ArrayAdapter<CharSequence>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        nameEditText = findViewById(R.id.name)
        birthEditText = findViewById(R.id.birth)
        addressEditText = findViewById(R.id.address)
        genderSpinner = findViewById(R.id.gender)
        phoneEditText = findViewById(R.id.phone)
        ccp = findViewById(R.id.ccp)
        saveButton = findViewById(R.id.btn_save)

        ccp.registerCarrierNumberEditText(phoneEditText)

        saveButton.setOnClickListener {
            saveUserData()
        }

        setupGenderSpinner()
        setupDatePicker()
    }

    private fun setupGenderSpinner() {
        genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_array,
            R.layout.item_spinner
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter

        // Set the hint
        genderSpinner.setSelection(0)

        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    (view as TextView).setTextColor(ContextCompat.getColor(this@PersonalAct, R.color.gray))
                } else {
                    (view as TextView).setTextColor(ContextCompat.getColor(this@PersonalAct, R.color.black))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }

        birthEditText.setOnClickListener {
            DatePickerDialog(
                this,
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
        birthEditText.setText(sdf.format(calendar.time))
    }

    private fun saveUserData() {
        val name = nameEditText.text.toString()
        val birth = birthEditText.text.toString()
        val address = addressEditText.text.toString()
        val gender = if (genderSpinner.selectedItemPosition != 0) {
            genderSpinner.selectedItem.toString()
        } else {
            ""
        }
        val phone = if (phoneEditText.text.isNotEmpty() && this::ccp.isInitialized) {
            try {
                ccp.fullNumberWithPlus
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            ""
        }

        if (name.isEmpty() || birth.isEmpty() || address.isEmpty() || gender.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userUpdates = mapOf(
                "name" to name,
                "birth" to birth,
                "address" to address,
                "gender" to gender,
                "phone" to phone
            )
            database.child(userId).updateChildren(userUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "User data saved successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@PersonalAct, ProfileAct::class.java)
                    startActivity(intent)
                    finish() // Close the current activity
                } else {
                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}