package com.example.elevenbarbershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.BarberModel

class BookBarberAdapter(
    private val barbers: List<BarberModel>,
    private val onBarberSelected: (BarberModel) -> Unit
) : RecyclerView.Adapter<BookBarberAdapter.BarberViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    class BarberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.barber)
        val image: ImageView = itemView.findViewById(R.id.barber_image)
        val container: FrameLayout = itemView.findViewById(R.id.item_barber)
        val innerContainer: LinearLayout = itemView.findViewById(R.id.barber_linier)  // Add this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_book_barber, parent, false)
        return BarberViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
        val barber = barbers[position]
        holder.name.text = barber.name
        Glide.with(holder.image.context).load(barber.bPict).into(holder.image) // Load image using Glide

        holder.innerContainer.isSelected = (selectedPosition == position)  // Set the selected state on innerContainer

        // Apply animation if selected
        if (holder.innerContainer.isSelected) {
            holder.container.animate().scaleX(1.1f).scaleY(1.1f).duration = 300
        } else {
            holder.container.animate().scaleX(1.0f).scaleY(1.0f).duration = 300
        }

        holder.itemView.setOnClickListener {
            val previousSelectedPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelectedPosition)
            }
            notifyItemChanged(selectedPosition)

            onBarberSelected(barber)
        }
    }

    override fun getItemCount() = barbers.size
}