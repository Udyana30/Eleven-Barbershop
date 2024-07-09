package com.example.elevenbarbershop.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.elevenbarbershop.R
import com.example.elevenbarbershop.adapter.BookingsHistoryAdapter
import com.example.elevenbarbershop.databinding.FragmentBookHistoryBinding
import com.example.elevenbarbershop.view_model.BookVM

class BookHistoryFrg : Fragment() {

    private var _binding: FragmentBookHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var bookViewModel: BookVM
    private lateinit var bookingsHistoryAdapter: BookingsHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookViewModel = ViewModelProvider(this)[BookVM::class.java]

        bookingsHistoryAdapter = BookingsHistoryAdapter(mutableListOf(), bookViewModel)
        binding.historyBook.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookingsHistoryAdapter
        }

        binding.btnBook.setOnClickListener {
            val bookFragment = BookFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, bookFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnActive.setOnClickListener {
            val bookActive = BookActiveFrg()
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, bookActive)
                .addToBackStack(null)
                .commit()
        }
        setupViewModelObservers()

        loadHistoryBookings()
    }

    private fun setupViewModelObservers() {
        bookViewModel.bookings.observe(viewLifecycleOwner) { bookings ->
            val historyBookings = bookings.filter { it.status == "history" }
            if (historyBookings.isNotEmpty()) {
                bookingsHistoryAdapter.updateBookings(historyBookings)
                binding.emptyMessage.visibility = View.GONE
                binding.historyBook.visibility = View.VISIBLE
            } else {
                binding.emptyMessage.visibility = View.VISIBLE
                binding.historyBook.visibility = View.GONE
            }
        }
    }

    private fun loadHistoryBookings() {
        bookViewModel.loadHistoryBookings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}