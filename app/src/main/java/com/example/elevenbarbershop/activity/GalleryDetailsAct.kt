package com.example.elevenbarbershop.activity

import ItemDecoration
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.adapter.GalleryAdapter
import com.example.elevenbarbershop.databinding.ActivityGalleryDetailsBinding
import com.example.elevenbarbershop.model.GalleryModel
import com.example.elevenbarbershop.view_model.MainVM

class GalleryDetailsAct : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryDetailsBinding
    private lateinit var backHome: ImageView
    private lateinit var galleryAdapter: GalleryAdapter

    private val mainVM: MainVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        backHome = findViewById(R.id.logo)

        backHome.setOnClickListener {
            startActivity(Intent(this@GalleryDetailsAct, MainAct::class.java))
        }

        // Mendapatkan data galeri dari intent
        val galleryItem = intent.getParcelableExtra<GalleryModel>("gallery_item")
        galleryItem?.let {
            binding.insightTitle.text = it.name
            binding.insightStyle.text = it.type
            binding.insightSubtitle.text = it.des
            binding.faceDescription.text = it.face
            Glide.with(this).load(it.gUrl).into(binding.gallery)
        }

        // Inisialisasi dan setup RecyclerView serta adapter
        galleryAdapter = GalleryAdapter(emptyList(), this)
        binding.recyclerGallery.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = galleryAdapter
            addItemDecoration(ItemDecoration(2, 40, false))
        }

        // Observasi galleryList dari MainVM dan perbarui adapter saat data berubah
        mainVM.galleryList.observe(this) { galleryItems ->
            Log.d("GalleryDetailsAct", "Gallery Items: $galleryItems")
            galleryItems?.let {
                galleryAdapter.setOriginalGalleryList(it)
                galleryAdapter.setDisplayedItems(listOfNotNull(galleryItem))
                binding.progressBar4.visibility = View.GONE
            }
        }

        mainVM.fetchGallery()
    }
}
