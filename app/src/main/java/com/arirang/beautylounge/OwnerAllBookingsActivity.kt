package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityOwnerAllBookingsBinding
import com.google.firebase.firestore.FirebaseFirestore

class OwnerAllBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerAllBookingsBinding
    private lateinit var db: FirebaseFirestore

    private val allBookings = mutableListOf<Booking>()
    private val filteredBookings = mutableListOf<Booking>()
    private lateinit var bookingAdapter: BookingAdapter
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerAllBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "All Bookings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupFilterChips()
        loadAllBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(filteredBookings, showCustomer = true)
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = bookingAdapter
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { applyFilter("All") }
        binding.chipConfirmed.setOnClickListener { applyFilter("Confirmed") }
        binding.chipCompleted.setOnClickListener { applyFilter("Completed") }
        binding.chipCancelled.setOnClickListener { applyFilter("Cancelled") }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        filteredBookings.clear()
        filteredBookings.addAll(
            if (filter == "All") allBookings
            else allBookings.filter { it.status == filter }
        )
        if (filteredBookings.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvBookings.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvBookings.visibility = View.VISIBLE
        }
        bookingAdapter.notifyDataSetChanged()
    }

    private fun loadAllBookings() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvBookings.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        db.collection("bookings").get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                allBookings.clear()

                for (doc in documents) {
                    val booking = Booking(
                        bookingId = doc.getString("bookingId") ?: doc.id,
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
                    allBookings.add(booking)
                }

                allBookings.sortByDescending { it.createdAt }
                applyFilter(currentFilter)
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
