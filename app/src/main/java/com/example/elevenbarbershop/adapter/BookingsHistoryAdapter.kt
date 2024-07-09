package com.example.elevenbarbershop.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.databinding.ItemBookHistoryBinding
import com.example.elevenbarbershop.model.BarberModel
import com.example.elevenbarbershop.model.BookingsModel
import com.example.elevenbarbershop.view_model.BookVM
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookingsHistoryAdapter(
    private var bookings: MutableList<BookingsModel>,
    private val bookViewModel: BookVM
) : RecyclerView.Adapter<BookingsHistoryAdapter.BookingsHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingsHistoryViewHolder {
        val binding = ItemBookHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingsHistoryViewHolder(binding, bookViewModel)
    }

    override fun onBindViewHolder(holder: BookingsHistoryViewHolder, position: Int) {
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

    class BookingsHistoryViewHolder(
        private val binding: ItemBookHistoryBinding,
        private val bookViewModel: BookVM
    ) : RecyclerView.ViewHolder(binding.root) {

        private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

        fun bind(booking: BookingsModel) {
            val servicesTitles = booking.services.joinToString(", ") { it.title }
            binding.titleBook.text = servicesTitles
            binding.dateActive.text = "${booking.date} // ${booking.time}"
            binding.locationActive.text = booking.location
            binding.barber.text = "with ${booking.barber}"
            loadBarberImage(booking.barber)
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
                                        .placeholder(R.drawable.pictbarber_2)
                                        .into(binding.barberImage)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }
}