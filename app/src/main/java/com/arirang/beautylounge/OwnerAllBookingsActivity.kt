package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityOwnerAllBookingsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

        supportActionBar?.title = "General Schedule"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()

        setupRecyclerView()
        setupFilterChips()
        loadGeneralSchedule()
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

    private fun loadGeneralSchedule() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvBookings.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        // Build set of valid date strings for the next 14 days
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val validDates = mutableSetOf<String>()
        for (i in 0..13) {
            validDates.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        db.collection("bookings").get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                allBookings.clear()

                for (doc in documents) {
                    val date = doc.getString("date") ?: ""
                    if (date !in validDates) continue  // only show next 14 days

                    val booking = Booking(
                        bookingId = doc.getString("bookingId") ?: doc.id,
                        customerId = doc.getString("customerId") ?: "",
                        customerName = doc.getString("customerName") ?: "",
                        serviceId = doc.getString("serviceId") ?: "",
                        serviceName = doc.getString("serviceName") ?: "",
                        serviceCategory = doc.getString("serviceCategory") ?: "",
                        staffId = doc.getString("staffId") ?: "",
                        staffName = doc.getString("staffName") ?: "",
                        date = date,
                        time = doc.getString("time") ?: "",
                        price = (doc.getLong("price") ?: 0L).toInt(),
                        durationMin = (doc.getLong("durationMin") ?: 0L).toInt(),
                        durationMax = (doc.getLong("durationMax") ?: 0L).toInt(),
                        status = doc.getString("status") ?: "Confirmed",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                    allBookings.add(booking)
                }

                // Sort by date then time
                val dateSdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                val timeSdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                allBookings.sortWith(Comparator { a, b ->
                    try {
                        val dateA = dateSdf.parse(a.date)?.time ?: 0L
                        val dateB = dateSdf.parse(b.date)?.time ?: 0L
                        if (dateA != dateB) return@Comparator dateA.compareTo(dateB)
                    } catch (e: Exception) { /* fall through */ }
                    try {
                        val timeA = timeSdf.parse(a.time)?.time ?: 0L
                        val timeB = timeSdf.parse(b.time)?.time ?: 0L
                        timeA.compareTo(timeB)
                    } catch (e: Exception) { 0 }
                })

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
