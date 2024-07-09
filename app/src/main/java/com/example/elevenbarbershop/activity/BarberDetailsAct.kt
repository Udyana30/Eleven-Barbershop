package com.example.elevenbarbershop.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.adapter.BarberAdapter
import com.example.elevenbarbershop.databinding.ActivityBarberDetailsBinding
import com.example.elevenbarbershop.model.BarberModel
import com.example.elevenbarbershop.view_model.MainVM

class BarberDetailsAct : AppCompatActivity() {

    private lateinit var binding: ActivityBarberDetailsBinding
    private lateinit var backHome: ImageView
    private lateinit var barberAdapter: BarberAdapter

    private val mainVM: MainVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarberDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        backHome = findViewById(R.id.logo)

        backHome.setOnClickListener {
            val intent = Intent(this@BarberDetailsAct, MainAct::class.java)
            intent.putExtra("fragment", "about")
            startActivity(intent)
        }

        // Mendapatkan data barber dari intent
        val barberItem = intent.getParcelableExtra<BarberModel>("barber_item")
        barberItem?.let {
            binding.barberName.text = it.name
            binding.barberDescription.text = it.des
            binding.locationDescription.text = it.loc
            binding.skillDescription.text = it.skill
            binding.languageDescription.text = it.lg
            Glide.with(this).load(it.bPict).into(binding.barber)
        }

        // Inisialisasi dan setup RecyclerView serta adapter
        barberAdapter = BarberAdapter(emptyList(), this)
        binding.recyclerBarber.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = barberAdapter
        }

        // Observasi barberList dari MainVM dan perbarui adapter saat data berubah
        mainVM.barberList.observe(this) { barberItems ->
            Log.d("BarberDetailsAct", "Barber Items: $barberItems")
            barberItems?.let {
                barberAdapter.setOriginalBarber(it)
                barberAdapter.setDisplayedItems(listOfNotNull(barberItem))
                binding.progressBar7.visibility = View.GONE
            }
        }

        mainVM.fetchBarber()
    }
}