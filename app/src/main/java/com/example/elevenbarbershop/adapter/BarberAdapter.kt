package com.example.elevenbarbershop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.activity.BarberDetailsAct
import com.example.elevenbarbershop.model.BarberModel
import java.util.Locale

class BarberAdapter(
    private var barberList: List<BarberModel>,
    private val context: Context
) : RecyclerView.Adapter<BarberAdapter.BarberViewHolder>() {

    private var barberFilter: List<BarberModel> = barberList
    private var displayedItems: List<BarberModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_barber, parent, false)
        return BarberViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
        holder.bind(barberFilter[position], context)
    }

    override fun getItemCount() = barberFilter.size

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val lowercaseQuery = query.lowercase(Locale.getDefault())
        barberFilter = barberList.filter { barber ->
            barber.name.lowercase(Locale.getDefault()).contains(lowercaseQuery) &&
                    !displayedItems.contains(barber)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDisplayedItems(items: List<BarberModel>) {
        displayedItems = items
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setOriginalBarber(newList: List<BarberModel>) {
        barberList = newList
        filter("") // Refresh the filtered list
    }

    class BarberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val barberTextView: TextView = itemView.findViewById(R.id.gallery)
        private val barberImageView: ImageView = itemView.findViewById(R.id.barber_image)

        fun bind(barber: BarberModel, context: Context) {
            barberTextView.text = barber.name
            Glide.with(itemView.context).load(barber.bPict).into(barberImageView)
            itemView.setOnClickListener {
                val intent = Intent(context, BarberDetailsAct::class.java)
                intent.putExtra("barber_item", barber)
                context.startActivity(intent)
            }
        }
    }
}