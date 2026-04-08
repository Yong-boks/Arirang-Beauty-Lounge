package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityMyDutiesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MyDutiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyDutiesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val scheduleList = mutableListOf<Booking>()
    private lateinit var adapter: StaffScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDutiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Today's Schedule"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        binding.tvTodayDate.text = sdf.format(Calendar.getInstance().time)

        adapter = StaffScheduleAdapter(scheduleList)
        binding.rvSchedule.layoutManager = LinearLayoutManager(this)
        binding.rvSchedule.adapter = adapter

        loadTodaySchedule()
    }

    private fun loadTodaySchedule() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvSchedule.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        // Today's date formatted the same way bookings are stored
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val todayStr = sdf.format(Calendar.getInstance().time)

        db.collection("bookings")
            .whereEqualTo("staffId", uid)
            .whereEqualTo("date", todayStr)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                scheduleList.clear()

                for (doc in documents) {
                    val booking = Booking(
                        bookingId = doc.getString("bookingId") ?: "",
                        customerId = doc.getString("customerId") ?: "",
                        customerName = doc.getString("customerName") ?: "",
                        serviceId = doc.getString("serviceId") ?: "",
                        serviceName = doc.getString("serviceName") ?: "",
                        serviceCategory = doc.getString("serviceCategory") ?: "",
                        staffId = doc.getString("staffId") ?: "",
                        staffName = doc.getString("staffName") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        price = (doc.getLong("price") ?: 0L).toInt(),
                        durationMin = (doc.getLong("durationMin") ?: 0L).toInt(),
                        durationMax = (doc.getLong("durationMax") ?: 0L).toInt(),
                        status = doc.getString("status") ?: "Confirmed",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                    scheduleList.add(booking)
                }

                // Parse times for correct chronological ordering ("08:00 AM", "09:30 AM", etc.)
                val timeSdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                scheduleList.sortBy { booking ->
                    try { timeSdf.parse(booking.time)?.time ?: 0L } catch (e: Exception) { 0L }
                }

                if (scheduleList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvSchedule.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvSchedule.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
