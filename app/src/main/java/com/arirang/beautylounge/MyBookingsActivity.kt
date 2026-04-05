package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityMyBookingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val bookingsList = mutableListOf<Booking>()
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "My Bookings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadBookings()

        binding.btnNewBooking.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(bookingsList)
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = bookingAdapter
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
                bookingsList.clear()

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
                    bookingsList.add(booking)
                }

                bookingsList.sortByDescending { it.createdAt }

                if (bookingsList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvBookings.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvBookings.visibility = View.VISIBLE
                    bookingAdapter.notifyDataSetChanged()
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
