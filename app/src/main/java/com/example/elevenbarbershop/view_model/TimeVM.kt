package com.example.elevenbarbershop.view_model

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.elevenbarbershop.model.BarberModel
import java.text.SimpleDateFormat
import java.util.*

class TimeVM : ViewModel() {

    private val _currentTime = MutableLiveData<String>()
    val currentTime: LiveData<String> get() = _currentTime

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    init {
        updateTime()
    }

    private fun updateTime() {
        runnable = object : Runnable {
            override fun run() {
                val currentDateTime = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(Date())
                _currentTime.postValue(currentDateTime)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(runnable)
    }
}