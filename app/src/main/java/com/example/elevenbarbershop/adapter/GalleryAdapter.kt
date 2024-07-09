package com.example.elevenbarbershop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.activity.GalleryDetailsAct
import com.example.elevenbarbershop.model.GalleryModel
import java.util.Locale

class GalleryAdapter(
    private var originalGalleryList: List<GalleryModel>,
    private val context: Context
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private var filteredGalleryList: List<GalleryModel> = originalGalleryList
    private var displayedItems: List<GalleryModel> = emptyList()

    private var typeFilter: String = ""
    private var faceFilter: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.viewholder_gallery, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(filteredGalleryList[position], context)
    }

    override fun getItemCount() = filteredGalleryList.size

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        val lowercaseQuery = query.lowercase(Locale.getDefault())
        filteredGalleryList = originalGalleryList.filter { gallery ->
            val matchesQuery = gallery.name.lowercase(Locale.getDefault()).contains(lowercaseQuery)
            val matchesType = typeFilter.isEmpty() || gallery.type.lowercase(Locale.getDefault()).contains(typeFilter.lowercase(Locale.getDefault()))
            val matchesFace = faceFilter.isEmpty() || gallery.face.lowercase(Locale.getDefault()).contains(faceFilter.lowercase(Locale.getDefault()))

            matchesQuery && matchesType && matchesFace && !displayedItems.contains(gallery)
        }
        Log.d("GalleryAdapter", "Filtered List Size: ${filteredGalleryList.size}")
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDisplayedItems(items: List<GalleryModel>) {
        displayedItems = items
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setOriginalGalleryList(newList: List<GalleryModel>) {
        originalGalleryList = newList
        filter("") // Refresh the filtered list
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTypeFilter(type: String) {
        typeFilter = type
        filter("")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFaceFilter(face: String) {
        faceFilter = face
        filter("")
    }


    inner class GalleryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val galleryImageView: ImageView = itemView.findViewById(R.id.vm_gallery)
        private val galleryName: TextView = itemView.findViewById(R.id.gallery)

        fun bind(gallery: GalleryModel, context: Context) {
            galleryName.text = gallery.name
            Glide.with(context).load(gallery.gUrl).into(galleryImageView)
            itemView.setOnClickListener {
                val intent = Intent(context, GalleryDetailsAct::class.java)
                intent.putExtra("gallery_item", gallery)
                context.startActivity(intent)
            }
        }
    }
}
