package com.example.elevenbarbershop.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.BookingsModel
import com.example.elevenbarbershop.view_model.BookVM
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PaymentAct : AppCompatActivity() {

    private lateinit var bookVM: BookVM
    private lateinit var location: TextView
    private lateinit var date: TextView
    private lateinit var services: TextView
    private lateinit var services_info: TextView
    private lateinit var price: TextView
    private lateinit var backPayment: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        bookVM = ViewModelProvider(this)[BookVM::class.java]

        location = findViewById(R.id.location)
        date = findViewById(R.id.date)
        services_info = findViewById(R.id.services)
        services = findViewById(R.id.price_service)
        price = findViewById(R.id.price)
        backPayment = findViewById(R.id.btn_back)

        backPayment.setOnClickListener {
            val intent = Intent(this@PaymentAct, MainAct::class.java)
            intent.putExtra("fragment", "active_bookings")
            startActivity(intent)
        }

        val bookingId = intent.getStringExtra("booking_id")

        if (bookingId != null){
            loadBookingData(bookingId)
        }else{
            Toast.makeText(this,"Booking ID not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadBookingData(bookingId: String){
        val bookRef = FirebaseDatabase.getInstance().getReference("Bookings").child(bookingId)

        bookRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot){
                val booking = snapshot.getValue(BookingsModel::class.java)
                if(booking != null){
                    displayBookingData(booking)
                }else{
                    Toast.makeText(this@PaymentAct, "Booking not found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError){
                Toast.makeText(this@PaymentAct, "Failed to load booking data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun displayBookingData(booking: BookingsModel){
        location.text = booking.location
        date.text = booking.date
        val servicesTitles = booking.services.joinToString(", ") { it.title }
        services_info.text = servicesTitles

        val servicesText = booking.services.joinToString(separator = "\n") {it.title}
        services.text = servicesText

        val totalPrice = booking.services.sumOf { it.price.removePrefix("$").toInt() }
        price.text = "$$totalPrice"
    }
}