package com.example.elevenbarbershop.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.adapter.BookingsAdapter
import com.example.elevenbarbershop.databinding.FragmentBookActiveBinding
import com.example.elevenbarbershop.view_model.BookVM

class BookActiveFrg : Fragment() {

    private var _binding: FragmentBookActiveBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookViewModel: BookVM
    private lateinit var bookingsAdapter: BookingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookActiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookViewModel = ViewModelProvider(this)[BookVM::class.java]

        bookingsAdapter = BookingsAdapter(mutableListOf(), bookViewModel)
        binding.activeBook.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookingsAdapter
        }

        setupViewModelObservers()

        binding.btnBook.setOnClickListener {
            val bookFragment = BookFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, bookFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnHistory.setOnClickListener {
            val historyFragment = BookHistoryFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, historyFragment)
                .addToBackStack(null)
                .commit()
        }

        loadActiveBookings()
    }

    private fun setupViewModelObservers() {
        bookViewModel.bookings.observe(viewLifecycleOwner) { bookings ->
            val activeBookings = bookings.filter { it.status == "active" }
            if (activeBookings.isNotEmpty()) {
                bookingsAdapter.updateBookings(activeBookings)
                binding.emptyMessage.visibility = View.GONE
                binding.activeBook.visibility = View.VISIBLE
            } else {
                binding.emptyMessage.visibility = View.VISIBLE
                binding.activeBook.visibility = View.GONE
            }
        }
    }

    private fun loadActiveBookings() {
        bookViewModel.loadActiveBookings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}