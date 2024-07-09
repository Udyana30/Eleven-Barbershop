package com.example.elevenbarbershop.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.databinding.ItemBookActiveBinding
import com.example.elevenbarbershop.fragment.AppointmentFrg
import com.example.elevenbarbershop.model.BarberModel
import com.example.elevenbarbershop.model.BookingsModel
import com.example.elevenbarbershop.view_model.BookVM
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class BookingsAdapter(
    private var bookings: MutableList<BookingsModel>,
    private val bookViewModel: BookVM
) : RecyclerView.Adapter<BookingsAdapter.BookingsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsViewHolder {
        val binding = ItemBookActiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingsViewHolder(binding, bookViewModel, this)
    }

    override fun onBindViewHolder(holder: BookingsViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookings.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateBookings(newBookings: List<BookingsModel>) {
        bookings.clear()
        bookings.addAll(newBookings)
        notifyDataSetChanged()
    }

    class BookingsViewHolder(
        private val binding: ItemBookActiveBinding,
        private val bookViewModel: BookVM,
        private val adapter: BookingsAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
        private var autoDeleteRunnable: Runnable? = null

        // Handler untuk pengaturan Runnable
        private val handler = Handler(Looper.getMainLooper())

        @SuppressLint("SetTextI18n")
        fun bind(booking: BookingsModel) {
            val servicesTitles = booking.services.joinToString(", ") { it.title }
            binding.titleBook.text = servicesTitles
            binding.dateActive.text = "${booking.date} // ${booking.time}"
            binding.locationActive.text = booking.location
            binding.barber.text = "with ${booking.barber}"
            loadBarberImage(booking.barber)

            binding.root.setOnClickListener {
                handleBookingAction(booking, isEdit = true)
            }

            binding.btnCancel.setOnClickListener {
                handleBookingAction(booking, isEdit = false)
            }
        }

        private fun loadBarberImage(barberName: String) {
            databaseReference.child("Barber").orderByChild("name").equalTo(barberName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (barberSnapshot in snapshot.children) {
                                val barber = barberSnapshot.getValue(BarberModel::class.java)
                                if (barber != null) {
                                    Glide.with(binding.root.context)
                                        .load(barber.bPict)
                                        .placeholder(R.drawable.pictbarber_2) // Gambar placeholder
                                        .into(binding.barberImage)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("BookingsViewHolder", "Error fetching barber image", error.toException())
                    }
                })
        }

        private fun handleBookingAction(booking: BookingsModel, isEdit: Boolean) {
            val currentTime = System.currentTimeMillis()

            // Konversi tanggal booking ke milidetik
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val bookingDateStr = booking.date
            val bookingDate = try {
                sdf.parse(bookingDateStr)?.time ?: 0
            } catch (e: Exception) {
                0L
            }
            Log.d("BookingsViewHolder", "Current time (epoch): $currentTime")
            Log.d("BookingsViewHolder", "Booking date (epoch): $bookingDate")

            // Hitung waktu batas untuk edit atau cancel (tengah malam sebelum hari booking)
            val calendar = Calendar.getInstance().apply {
                timeInMillis = bookingDate
                add(Calendar.DAY_OF_MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val cutoffTime = calendar.timeInMillis
            Log.d("BookingsViewHolder", "Cutoff time (epoch): $cutoffTime")

            if (currentTime < cutoffTime) {
                if (isEdit) {
                    val context = binding.root.context
                    if (context is androidx.fragment.app.FragmentActivity) {
                        val transaction = context.supportFragmentManager.beginTransaction()
                        val fragment = AppointmentFrg()
                        val args = Bundle().apply {
                            putString("bookingId", booking.bookingId)
                        }
                        fragment.arguments = args
                        transaction.replace(R.id.container, fragment)
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                } else {
                    AlertDialog.Builder(binding.root.context)
                        .setTitle("Cancel Booking")
                        .setMessage("Are you sure you want to cancel this booking?")
                        .setPositiveButton("Yes") { dialog, _ ->
                            bookViewModel.deleteBooking(booking.bookingId)
                            Log.d("BookingsViewHolder", "BookingID: ${booking.bookingId}")
                            dialog.dismiss()
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            } else {
                showBookingActionRestrictionDialog()
            }
        }

        private fun showBookingActionRestrictionDialog() {
            AlertDialog.Builder(binding.root.context)
                .setTitle("Edit or Cancel Booking")
                .setMessage("You can only edit or cancel bookings until midnight the day before the booking date.")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}