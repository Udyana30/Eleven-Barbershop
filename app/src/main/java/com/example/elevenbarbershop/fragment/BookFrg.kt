package com.example.elevenbarbershop.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CalendarView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.activity.PaymentAct
import com.example.elevenbarbershop.adapter.BookBarberAdapter
import com.example.elevenbarbershop.adapter.BookServicesAdapter
import com.example.elevenbarbershop.databinding.FragmentBookFrgBinding
import com.example.elevenbarbershop.model.BookServicesModel
import com.example.elevenbarbershop.view_model.BookVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class BookFrg : Fragment() {

    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var autoCompleteTextViewTime: AutoCompleteTextView
    private lateinit var recyclerViewServices: RecyclerView
    private lateinit var progressBarServices: ProgressBar
    private lateinit var recyclerViewBarbers: RecyclerView
    private lateinit var emptyBarbers: TextView
    private lateinit var calendarView: CalendarView
    private lateinit var bookViewModel: BookVM
    private lateinit var binding: FragmentBookFrgBinding
    private lateinit var databaseReference: DatabaseReference

    private var selectedLocation: String? = null
    private var selectedBarber: String? = null
    private var selectedDate: Long? = null
    private var selectedTime: String? = null
    private val selectedServices = mutableListOf<BookServicesModel>()

    private val times = listOf("10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00")
    private var availableTimes = times.toMutableList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookFrgBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        try {
            autoCompleteTextView = view.findViewById(R.id.autoCompleteTextView)
            autoCompleteTextViewTime = view.findViewById(R.id.autoCompleteTextView1)
            recyclerViewServices = view.findViewById(R.id.recyclerServices)
            progressBarServices = view.findViewById(R.id.progressBar_services)
            recyclerViewBarbers = view.findViewById(R.id.recyclerBarbers)
            emptyBarbers = view.findViewById(R.id.empty_barber)
            calendarView = view.findViewById(R.id.calendarView)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to initialize views: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Set the minimum date for the calendar to be tomorrow
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendarView.minDate = calendar.timeInMillis

        recyclerViewServices.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewBarbers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Initialize ViewModel
        try {
            bookViewModel = ViewModelProvider(this)[BookVM::class.java]
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to initialize ViewModel: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        bookViewModel.services.observe(viewLifecycleOwner) { services ->
            recyclerViewServices.adapter = BookServicesAdapter(services, { selectedServiceModel ->
                if (selectedServices.contains(selectedServiceModel)) {
                    selectedServices.remove(selectedServiceModel)
                } else {
                    selectedServices.add(selectedServiceModel)
                }
            }, selectedServices)
            progressBarServices.visibility = View.GONE
        }

        bookViewModel.locations.observe(viewLifecycleOwner) { locations ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                locations
            )
            autoCompleteTextView.setAdapter(adapter)
        }

        // Set up CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            selectedDate = selectedCalendar.timeInMillis
            loadAvailableTimes()
        }

        // Set up time input
        autoCompleteTextViewTime.setOnItemClickListener { _, _, position, _ ->
            selectedTime = availableTimes[position]
        }

        // Set up location selection
        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            selectedLocation = autoCompleteTextView.adapter.getItem(position) as String
            loadBarbersForLocation(selectedLocation!!)
        }

        // Set up booking button
        binding.bookNow.setOnClickListener {
            if (selectedLocation == null || selectedBarber == null || selectedDate == null || selectedTime == null) {
                Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show()
            } else {
                showUserAgreementDialog()
            }
        }

        binding.btnActive.setOnClickListener {
            val activeFragment = BookActiveFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, activeFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnHistory.setOnClickListener {
            val historyFragment = BookHistoryFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, historyFragment)
                .addToBackStack(null)
                .commit()
        }
        setupViewModelObservers()
    }

    private fun setupViewModelObservers() {
        bookViewModel.services.observe(viewLifecycleOwner) { services ->
            binding.recyclerServices.adapter = BookServicesAdapter(services, { selectedServiceModel ->
                if (selectedServices.contains(selectedServiceModel)) {
                    selectedServices.remove(selectedServiceModel)
                } else {
                    selectedServices.add(selectedServiceModel)
                }
            }, selectedServices)
            binding.progressBarServices.visibility = View.GONE
        }

        bookViewModel.locations.observe(viewLifecycleOwner) { locations ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                locations
            )
            binding.autoCompleteTextView.setAdapter(adapter)
        }
    }

    private fun loadBarbersForLocation(location: String) {
        recyclerViewBarbers.visibility = View.GONE
        emptyBarbers.visibility = View.VISIBLE

        bookViewModel.loadBarbersByLocation(location)
        bookViewModel.barbers.observe(viewLifecycleOwner) { barbers ->
            val barberAdapter = BookBarberAdapter(barbers) { barber ->
                selectedBarber = barber.name
            }
            recyclerViewBarbers.adapter = barberAdapter

            if (barbers.isNotEmpty()) {
                recyclerViewBarbers.visibility = View.VISIBLE
                emptyBarbers.visibility = View.GONE
            } else {
                recyclerViewBarbers.visibility = View.GONE
                emptyBarbers.visibility = View.VISIBLE
            }
        }
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
                    autoCompleteTextViewTime.setAdapter(timeAdapter)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load available times", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun bookAppointment() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(selectedDate!!))
        val booking = mapOf(
            "userId" to userId,
            "location" to selectedLocation,
            "barber" to selectedBarber,
            "date" to formattedDate,
            "time" to selectedTime,
            "status" to "active",
            "date_barber" to "${formattedDate}_$selectedBarber",
            "services" to selectedServices.map { mapOf(
                "title" to it.title,
                "price" to it.price,
                "description" to it.dec
            )}
        )

        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings")
        val bookingId = databaseReference.push().key

        if (bookingId != null) {
            databaseReference.child(bookingId).setValue(booking).addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(requireContext(), "Booking successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), PaymentAct::class.java).apply {
                        putExtra("booking_id", bookingId)
                    }
                    startActivity(intent)
                    loadAvailableTimes() // Refresh available times after booking
                } else {
                    Toast.makeText(requireContext(), "Booking failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showUserAgreementDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("User Agreement")
        builder.setMessage("Arrive within 15 minutes of your booking time, or your appointment will be automatically canceled.")
        builder.setPositiveButton("Agree") { dialog, _ ->
            bookAppointment()
            dialog.dismiss()
        }
        builder.setNegativeButton("Disagree") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}