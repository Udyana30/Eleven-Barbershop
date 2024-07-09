package com.example.elevenbarbershop.etc

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.elevenbarbershop.model.BookingsModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AutoDeleteWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Bookings")
        val now = System.currentTimeMillis()
        val fifteenMinutesAgo = now - (15 * 60 * 1000)

        databaseReference.get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { childSnapshot ->
                val booking = childSnapshot.getValue(BookingsModel::class.java)
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                val bookingTimeStr = "${booking?.date} ${booking?.time}"
                val bookingTime = sdf.parse(bookingTimeStr)?.time ?: 0

                if (booking?.status == "active" && bookingTime <= fifteenMinutesAgo) {
                    childSnapshot.ref.removeValue()
                }
            }
        }
        return Result.success()
    }
}