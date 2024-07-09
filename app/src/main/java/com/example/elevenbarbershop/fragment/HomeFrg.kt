package com.example.elevenbarbershop.fragment

import ItemDecoration
import SliderAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.adapter.GalleryAdapter
import com.example.elevenbarbershop.adapter.ServicesAdapter
import com.example.elevenbarbershop.databinding.FragmentHomeFrgBinding
import com.example.elevenbarbershop.etc.FilterDialogFragment
import com.example.elevenbarbershop.view_model.MainVM
import com.example.elevenbarbershop.view_model.TimeVM

class HomeFrg : Fragment() {

    private lateinit var binding: FragmentHomeFrgBinding
    private val mainViewModel: MainVM by viewModels()
    private val timeViewModel: TimeVM by viewModels()
    private var galleryAdapter: GalleryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFrgBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.name.text = user.username
        }

        val viewPager = binding.banner
        val dotsIndicator = binding.dot

        timeViewModel.currentTime.observe(viewLifecycleOwner) { currentTime ->
            binding.realtimeDate.text = currentTime
        }

        mainViewModel.bannerList.observe(viewLifecycleOwner) { banners ->
            val adapter = SliderAdapter(banners)
            viewPager.adapter = adapter
            dotsIndicator.setViewPager2(viewPager)

            if (banners.isNotEmpty()) {
                binding.progressBar.visibility = View.GONE
                dotsIndicator.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.VISIBLE
                dotsIndicator.visibility = View.GONE
            }
        }

        mainViewModel.servicesList.observe(viewLifecycleOwner) { services ->
            val servicesAdapter = ServicesAdapter(services)
            binding.recyclerServices.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = servicesAdapter
            }

            if (services.isNotEmpty()) {
                binding.progressBar2.visibility = View.GONE
            } else {
                binding.progressBar2.visibility = View.VISIBLE
            }
        }

        // Setting up the gallery RecyclerView
        mainViewModel.galleryList.observe(viewLifecycleOwner) { galleries ->
            galleryAdapter = GalleryAdapter(galleries, requireContext())
            binding.recyclerGallery.apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = galleryAdapter
                addItemDecoration(ItemDecoration(2, 40, false))
            }

            if (galleries.isNotEmpty()) {
                binding.progressBar4.visibility = View.GONE
            } else {
                binding.progressBar4.visibility = View.VISIBLE
            }

            setupSearch()
            setupFilter()
        }
    }

    private fun setupSearch() {
        val searchBar = binding.searchBar
        searchBar.setOnEditorActionListener { _, _, _ ->
            val query = searchBar.text.toString().trim()
            if (query.isEmpty()) {
                galleryAdapter?.filter("")
            } else {
                galleryAdapter?.filter(query)

                if ((galleryAdapter?.itemCount ?: 0) > 0) {
                    Toast.makeText(requireContext(), "Item found", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show()
                    galleryAdapter?.filter("")  // Load all items
                }
            }
            true
        }
    }

    private fun setupFilter() {
        val filterImageView = binding.filter
        filterImageView.setOnClickListener {
            val filterDialog = FilterDialogFragment { type, face ->
                galleryAdapter?.setTypeFilter(if (type == "All") "" else type)
                galleryAdapter?.setFaceFilter(if (face == "All") "" else face)
            }
            filterDialog.show(parentFragmentManager, "FilterDialog")
        }
    }

}