package com.example.elevenbarbershop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.model.ServicesModel

class ServicesAdapter(private val servicesList: List<ServicesModel>) :
    RecyclerView.Adapter<ServicesAdapter.ServicesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_services, parent, false)
        return ServicesViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServicesViewHolder, position: Int) {
        holder.bind(servicesList[position])
    }

    override fun getItemCount() = servicesList.size

    class ServicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val servicesTextView: TextView = itemView.findViewById(R.id.services)
        private val servicesImageView: ImageView = itemView.findViewById(R.id.services_image)

        fun bind(service: ServicesModel) {
            servicesTextView.text = service.title
            Glide.with(itemView.context).load(service.picUrl).into(servicesImageView)
        }
    }
}