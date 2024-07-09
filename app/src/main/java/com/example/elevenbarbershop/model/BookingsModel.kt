    package com.example.elevenbarbershop.model

    data class BookingsModel(
        var bookingId: String = "",
        val userId: String = "",
        val date: String = "",
        val time: String = "",
        val location: String = "",
        val barber: String = "",
        var status: String = "active",
        var services: List<BookServicesModel> = listOf()
    )