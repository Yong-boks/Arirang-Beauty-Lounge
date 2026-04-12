package com.arirang.beautylounge

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityMyBookingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        bookingAdapter = BookingAdapter(
            filteredBookings,
            onCancelClick = { booking -> confirmCancelBooking(booking) },
            onRescheduleClick = { booking -> showRescheduleDialog(booking) }
        )
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

    private fun showRescheduleDialog(booking: Booking) {
        var newDate = ""
        var newTime = ""
        var selectedCal = Calendar.getInstance()

        // Step 1: Date picker
        val today = Calendar.getInstance()
        val maxCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 14) }
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedCal = Calendar.getInstance().apply { set(year, month, day) }
                val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                newDate = sdf.format(selectedCal.time)

                // Step 2: Time picker spinner inside AlertDialog
                val isSunday = selectedCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                val startHour = if (isSunday) 9 else 8
                val endHour = if (isSunday) 15 else 18

                val slots = mutableListOf<String>()
                var h = startHour
                var m = 0
                while (h < endHour) {
                    val amPm = if (h < 12) "AM" else "PM"
                    val h12 = when {
                        h == 0 -> 12
                        h > 12 -> h - 12
                        else -> h
                    }
                    slots.add(String.format("%02d:%02d %s", h12, m, amPm))
                    m += 30
                    if (m >= 60) { m = 0; h++ }
                }

                val spinner = Spinner(this)
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, slots)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                if (slots.isNotEmpty()) newTime = slots[0]
                spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                        newTime = slots[pos]
                    }
                    override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
                })

                AlertDialog.Builder(this)
                    .setTitle("Select New Time")
                    .setMessage("Date: $newDate")
                    .setView(spinner)
                    .setPositiveButton("Confirm") { _, _ ->
                        if (newDate.isNotEmpty() && newTime.isNotEmpty()) {
                            checkRescheduleConflictAndSave(booking, newDate, newTime)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = today.timeInMillis
            datePicker.maxDate = maxCal.timeInMillis
            show()
        }
    }

    private fun checkRescheduleConflictAndSave(booking: Booking, newDate: String, newTime: String) {
        val timeSdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
        val newStartMins = try {
            val cal = Calendar.getInstance()
            cal.time = timeSdf.parse(newTime)!!
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } catch (e: Exception) { -1 }
        val newEndMins = newStartMins + booking.durationMax

        db.collection("bookings")
            .whereEqualTo("staffId", booking.staffId)
            .whereEqualTo("date", newDate)
            .get()
            .addOnSuccessListener { documents ->
                var hasConflict = false
                for (doc in documents) {
                    val docId = doc.getString("bookingId") ?: doc.id
                    if (docId == booking.bookingId) continue  // skip current booking
                    val status = doc.getString("status") ?: "Confirmed"
                    if (status == "Cancelled") continue

                    val bookingTime = doc.getString("time") ?: continue
                    val bookingDurationMax = (doc.getLong("durationMax") ?: 30L).toInt()
                    val bookingStartMins = try {
                        val cal = Calendar.getInstance()
                        cal.time = timeSdf.parse(bookingTime)!!
                        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                    } catch (e: Exception) { continue }
                    val bookingEndMins = bookingStartMins + bookingDurationMax

                    if (newStartMins < bookingEndMins && bookingStartMins < newEndMins) {
                        hasConflict = true
                        break
                    }
                }

                if (hasConflict) {
                    Toast.makeText(
                        this,
                        "Staff unavailable at this time – please pick another slot",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    performReschedule(booking, newDate, newTime)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not verify availability. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun performReschedule(booking: Booking, newDate: String, newTime: String) {
        db.collection("bookings").document(booking.bookingId)
            .update(
                mapOf(
                    "date" to newDate,
                    "time" to newTime,
                    "updatedAt" to Date()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Booking rescheduled to $newDate at $newTime", Toast.LENGTH_LONG).show()
                val idx = allBookings.indexOfFirst { it.bookingId == booking.bookingId }
                if (idx >= 0) {
                    allBookings[idx] = allBookings[idx].copy(date = newDate, time = newTime)
                    applyFilter(currentFilter)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to reschedule: ${e.message}", Toast.LENGTH_SHORT).show()
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

