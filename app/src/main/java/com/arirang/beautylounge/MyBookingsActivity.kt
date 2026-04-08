package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityMyBookingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val allBookings = mutableListOf<Booking>()
    private val filteredBookings = mutableListOf<Booking>()
    private lateinit var bookingAdapter: BookingAdapter
    private var currentFilter = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "My Bookings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupFilterChips()
        loadBookings()

        binding.btnNewBooking.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(filteredBookings) { booking ->
            confirmCancelBooking(booking)
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = bookingAdapter
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { applyFilter("All") }
        binding.chipUpcoming.setOnClickListener { applyFilter("Confirmed") }
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

    private fun loadBookings() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvBookings.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        db.collection("bookings")
            .whereEqualTo("customerId", uid)
            .get()
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

    private fun confirmCancelBooking(booking: Booking) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel your ${booking.serviceName} appointment on ${booking.date}?")
            .setPositiveButton("Yes, Cancel") { _, _ -> cancelBooking(booking) }
            .setNegativeButton("Keep Booking", null)
            .show()
    }

    private fun cancelBooking(booking: Booking) {
        if (booking.bookingId.isEmpty()) {
            Toast.makeText(this, "Unable to cancel: booking ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("bookings").document(booking.bookingId)
            .update(
                mapOf(
                    "status" to "Cancelled",
                    "updatedAt" to Date()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show()
                // Update local list
                val idx = allBookings.indexOfFirst { it.bookingId == booking.bookingId }
                if (idx >= 0) {
                    allBookings[idx] = allBookings[idx].copy(status = "Cancelled")
                    applyFilter(currentFilter)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to cancel booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        loadBookings()
    }
}

