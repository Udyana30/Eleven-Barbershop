package com.example.elevenbarbershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.BookServicesModel

class BookServicesAdapter(
    private val services: List<BookServicesModel>,
    private val onServiceSelected: (BookServicesModel) -> Unit,
    private val selectedServices: List<BookServicesModel>
) : RecyclerView.Adapter<BookServicesAdapter.ServiceViewHolder>() {

    private var selectedItem: BookServicesModel? = null

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title_service)
        val price: TextView = itemView.findViewById(R.id.price_service)
        val description: TextView = itemView.findViewById(R.id.description_service)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)

        init {
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val service = services[position]
                        selectService(service)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_services, parent, false)
        return ServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val currentService = services[position]
        holder.title.text = currentService.title
        holder.price.text = currentService.price
        holder.description.text = currentService.dec

        holder.radioButton.setOnCheckedChangeListener(null)

        // Check if the current service is in the list of selected services
        val isSelected = selectedServices.any { it.title == currentService.title }
        holder.radioButton.isChecked = isSelected

        holder.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectService(currentService)
            }
        }
    }


    }
}
