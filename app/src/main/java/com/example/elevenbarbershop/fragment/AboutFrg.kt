package com.example.elevenbarbershop.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elevenbarbershop.adapter.BarberAdapter
import com.example.elevenbarbershop.databinding.FragmentAboutFrgBinding
import com.example.elevenbarbershop.view_model.MainVM
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class AboutFrg : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentAboutFrgBinding
    private val viewModel: MainVM by activityViewModels()
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutFrgBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Map
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        // Observe barber list
        viewModel.barberList.observe(viewLifecycleOwner) { barbers ->
            val barberAdapter = BarberAdapter(barbers, requireContext())
            binding.recyclerBarbers.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = barberAdapter
            }

            if (barbers.isNotEmpty()) {
                binding.progressBar3.visibility = View.GONE
            } else {
                binding.progressBar3.visibility = View.VISIBLE
            }

            // Observe location list and add locations to map
            viewModel.locationList.observe(viewLifecycleOwner) { locations ->
                if (::googleMap.isInitialized) {
                    googleMap.clear() // Clear existing markers to avoid duplicates
                    locations.forEach { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        googleMap.addMarker(MarkerOptions().position(latLng).title(location.title))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 6f)) // Initial zoom level
                    }

                    // Optional: Move camera to the first location initially
                    if (locations.isNotEmpty()) {
                        val firstLocation = LatLng(locations[0].latitude, locations[0].longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 4f))
                    }
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Load locations to ensure the map is updated
        viewModel.locationList.value?.let { locations ->
            googleMap.clear()
            locations.forEach { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(MarkerOptions().position(latLng).title(location.title))
            }

            if (locations.isNotEmpty()) {
                val firstLocation = LatLng(locations[0].latitude, locations[0].longitude)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 4f))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

  
    }
}
