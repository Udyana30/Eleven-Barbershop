package com.example.elevenbarbershop.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.elevenbarbershop.model.BarberModel
import com.example.elevenbarbershop.model.BookServicesModel
import com.example.elevenbarbershop.model.BookingsModel
import com.example.elevenbarbershop.etc.AutoDeleteWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.TimeUnit

class BookVM : ViewModel() {

    private val _services = MutableLiveData<List<BookServicesModel>>()
    val services: LiveData<List<BookServicesModel>> get() = _services

    private val _locations = MutableLiveData<List<String>>()
    val locations: LiveData<List<String>> get() = _locations

    private val _barbers = MutableLiveData<List<BarberModel>>()
    val barbers: LiveData<List<BarberModel>> get() = _barbers

    private val _bookings = MutableLiveData<List<BookingsModel>>()
    val bookings: LiveData<List<BookingsModel>> get() = _bookings

    private val locationsDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Barbershop_Locations")
    private val bookingsDatabaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Bookings")
    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    init {
        loadServices()
        loadLocationsFromFirebase()
        scheduleAutoDelete()
    }

    private fun loadServices() {
        val servicesRef = FirebaseDatabase.getInstance().getReference("Book_Services")
        servicesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val serviceList = mutableListOf<BookServicesModel>()
                for (serviceSnapshot in snapshot.children) {
                    val service = serviceSnapshot.getValue(BookServicesModel::class.java)
                    service?.let { serviceList.add(it) }
                }
                _services.value = serviceList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadLocationsFromFirebase() {
        locationsDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val locationsList = mutableListOf<String>()
                for (snapshot in dataSnapshot.children) {
                    val location = snapshot.getValue(String::class.java)
                    if (location != null) {
                        locationsList.add(location)
                    }
                }
                _locations.value = locationsList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    fun loadBarbersByLocation(location: String) {
        databaseReference.child("Barber").orderByChild("loc").equalTo(location)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val barbers = mutableListOf<BarberModel>()
                    for (data in snapshot.children) {
                        val barber = data.getValue(BarberModel::class.java)
                        barber?.let { barbers.add(it) }
                    }
                    _barbers.value = barbers
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun loadActiveBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _bookings.value = emptyList()
            return
        }

        bookingsDatabaseReference.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val bookingsList = mutableListOf<BookingsModel>()
                    for (snapshot in dataSnapshot.children) {
                        val booking = snapshot.getValue(BookingsModel::class.java)
                        if (booking != null) {
                            booking.bookingId = snapshot.key ?: "" // Set bookingId here
                            bookingsList.add(booking)
                        }
                    }
                    _bookings.value = bookingsList
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    fun deleteBooking(bookingId: String) {
        bookingsDatabaseReference.child(bookingId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadActiveBookings()
            } else {
                // Handle error
            }
        }
    }

    fun updateBookingData(bookingId: String, updatedBooking: Map<String, Any>) {
        bookingsDatabaseReference.child(bookingId).updateChildren(updatedBooking).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                loadActiveBookings()
            } else {
                // Handle error
            }
        }
    }
    fun loadHistoryBookings() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            _bookings.value = emptyList()
            return
        }

        bookingsDatabaseReference.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val historyBookingsList = mutableListOf<BookingsModel>()
                    for (snapshot in dataSnapshot.children) {
                        val booking = snapshot.getValue(BookingsModel::class.java)
                        if (booking != null && booking.status == "history") {
                            booking.bookingId = snapshot.key ?: "" // Set bookingId here
                            historyBookingsList.add(booking)
                        }
                    }
                    _bookings.value = historyBookingsList
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun scheduleAutoDelete() {
        val workRequest = PeriodicWorkRequestBuilder<AutoDeleteWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance().enqueue(workRequest)
    }
}