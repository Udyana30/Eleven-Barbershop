package com.example.elevenbarbershop.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.adapter.BookServicesAdapter
import com.example.elevenbarbershop.databinding.FragmentAppointmentBinding
import com.example.elevenbarbershop.model.BarberModel
import com.example.elevenbarbershop.model.BookServicesModel
import com.example.elevenbarbershop.model.BookingsModel
import com.example.elevenbarbershop.view_model.BookVM
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppointmentFrg : Fragment() {

    private lateinit var binding: FragmentAppointmentBinding
    private val bookVM: BookVM by viewModels()
    private lateinit var databaseReference: DatabaseReference
    private val selectedServices = mutableListOf<BookServicesModel>()

    private var selectedLocation: String? = null
    private var selectedBarber: String? = null
    private var selectedDate: Long? = null
    private var selectedTime: String? = null

    private val times = listOf("10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00")
    private var availableTimes = times.toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookingId = arguments?.getString("bookingId") ?: return
        bookVM.loadActiveBookings()

        binding.logo.setOnClickListener{
            val back = BookActiveFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, back)
                .addToBackStack(null)
                .commit()
        }

        bookVM.bookings.observe(viewLifecycleOwner) { bookings ->
            val booking = bookings.find { it.bookingId == bookingId }
            booking?.let { updateUI(it) }
        }

        binding.updateNow.setOnClickListener {
            showConfirmationDialog(bookingId)
        }
        setupViewModelObservers()
        setupUI()
    }

    private fun setupUI() {
        binding.recyclerServices.layoutManager = LinearLayoutManager(requireContext())

        // Set the minimum date for the calendar to be tomorrow
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.timeInMillis

        binding.calendarView.minDate = tomorrow

        // Set Selected Date Before
        if (selectedDate != null) {
            binding.calendarView.date = selectedDate!!
        } else {
            binding.calendarView.date = tomorrow
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar.timeInMillis
            loadAvailableTimes()
        }

        binding.autoBarber.setOnItemClickListener { _, _, position, _ ->
            selectedBarber = binding.autoBarber.adapter.getItem(position) as String
            selectedBarber?.let {
                loadBarberImage(it)
                loadAvailableTimes()
            }
        }
    }

    private fun setupViewModelObservers() {
        bookVM.services.observe(viewLifecycleOwner) { services ->
            binding.recyclerServices.adapter = BookServicesAdapter(services, onServiceSelected = { service ->
                selectedServices.clear()
                selectedServices.add(service)
            }, selectedServices)
            binding.progressBarServices.visibility = View.GONE
        }

        bookVM.bookings.observe(viewLifecycleOwner) { bookings ->
            val bookingId = arguments?.getString("bookingId")
            val booking = bookings.find { it.bookingId == bookingId }
            booking?.let {
                selectedLocation = it.location
                loadBarbersFromDatabase(it.location)
                selectedServices.clear()
                selectedServices.addAll(it.services)
                Log.d("AppointmentFrg", "Selected Services: ${selectedServices.map { service -> service.title }}")
                binding.recyclerServices.adapter?.notifyDataSetChanged()

            }
        }
    }

    private fun loadBarbersFromDatabase(location: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Barber")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val barbers = mutableListOf<String>()
                for (data in snapshot.children) {
                    val barber = data.getValue(BarberModel::class.java)
                    if (barber?.loc == location) {
                        barber.name.let { barbers.add(it) }
                    }
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    barbers
                )
                binding.autoBarber.setAdapter(adapter)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load barbers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadBarberImage(barberName: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Barber")

        databaseReference.orderByChild("name").equalTo(barberName).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children) {
                    val barber = data.getValue(BarberModel::class.java)
                    barber?.bPict?.let { imageUrl ->
                        Glide.with(this@AppointmentFrg)
                            .load(imageUrl)
                            .placeholder(R.drawable.pictbarber_2)
                            .into(binding.barberImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load barber image", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(booking: BookingsModel) {
        binding.autoBarber.setText(booking.barber)
        selectedBarber = booking.barber
        selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(booking.date)?.time ?: 0L

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.timeInMillis
        selectedDate = maxOf(selectedDate!!, tomorrow)
        binding.calendarView.date = selectedDate!!

        binding.autoCompleteTextView1.setText(booking.time)
        selectedTime = booking.time

        selectedServices.clear()
        selectedServices.addAll(booking.services)
        binding.recyclerServices.adapter?.notifyDataSetChanged()
        loadBarberImage(booking.barber)
    }


    private fun loadAvailableTimes() {
        if (selectedDate == null || selectedBarber == null) return

        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate!!))
        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings")

        // Query to get all bookings for the selected date and barber
        databaseReference.orderByChild("date_barber")
            .equalTo("${formattedDate}_$selectedBarber")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    availableTimes = times.toMutableList()
                    for (data in snapshot.children) {
                        val bookedTime = data.child("time").getValue(String::class.java)
                        bookedTime?.let { availableTimes.remove(it) }
                    }
                    val timeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availableTimes)
                    binding.autoCompleteTextView1.setAdapter(timeAdapter)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load available times", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun showConfirmationDialog(bookingId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Changes")
            .setMessage("Are you sure you want to save the changes to this booking?")
            .setPositiveButton("Yes") { _, _ ->
                saveBookingChanges(bookingId)
                navigateToBookActiveFragment()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun saveBookingChanges(bookingId: String) {
        val updatedBooking = hashMapOf<String, Any>(
            "barber" to binding.autoBarber.text.toString(),
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(binding.calendarView.date),
            "time" to binding.autoCompleteTextView1.text.toString(),
            "services" to selectedServices.map { mapOf(
                "title" to it.title,
                "price" to it.price,
                "dec" to it.dec
            )}
        )
        bookVM.updateBookingData(bookingId, updatedBooking)
    }

    private fun navigateToBookActiveFragment() {
        val back = BookActiveFrg()
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, back)
            .addToBackStack(null)
            .commit()
    }
}