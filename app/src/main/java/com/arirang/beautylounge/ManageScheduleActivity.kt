package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityManageScheduleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManageScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageScheduleBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val scheduleList = mutableListOf<Booking>()
    private lateinit var adapter: StaffScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Manage Schedule"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        adapter = StaffScheduleAdapter(scheduleList) { booking -> markAsCompleted(booking) }
        binding.rvSchedule.layoutManager = LinearLayoutManager(this)
        binding.rvSchedule.adapter = adapter

        loadUpcomingSchedule()
    }

    private fun loadUpcomingSchedule() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvSchedule.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val employeeId = userDoc.getString("employeeId")
                if (employeeId == null) {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.tvScheduleSummary.text = "Employee profile incomplete"
                    return@addOnSuccessListener
                }
                loadUpcomingScheduleForEmployee(employeeId)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvScheduleSummary.text = "Could not load schedule"
            }
    }

    private fun loadUpcomingScheduleForEmployee(employeeId: String) {
        // Generate date strings for next 14 days
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val dateStrings = mutableSetOf<String>()
        for (i in 0..13) {
            dateStrings.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        db.collection("bookings")
            .whereEqualTo("staffId", employeeId)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                scheduleList.clear()

                for (doc in documents) {
                    val date = doc.getString("date") ?: ""
                    if (date !in dateStrings) continue
                    val status = doc.getString("status") ?: "Confirmed"
                    if (status == "Cancelled") continue

                    val booking = Booking(
                        bookingId = doc.getString("bookingId") ?: "",
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
                        status = status,
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                    scheduleList.add(booking)
                }

                scheduleList.sortWith(Comparator { a, b ->
                    try {
                        val dateA = sdf.parse(a.date)?.time ?: 0L
                        val dateB = sdf.parse(b.date)?.time ?: 0L
                        if (dateA != dateB) return@Comparator dateA.compareTo(dateB)
                    } catch (e: Exception) { /* fall through to time comparison */ }
                    try {
                        val timeSdf = SimpleDateFormat("h:mm a", Locale.ENGLISH)
                        val timeA = timeSdf.parse(a.time)?.time ?: 0L
                        val timeB = timeSdf.parse(b.time)?.time ?: 0L
                        timeA.compareTo(timeB)
                    } catch (e: Exception) { 0 }
                })

                val count = scheduleList.size
                binding.tvScheduleSummary.text = if (count == 0)
                    "No bookings in the next 14 days"
                else
                    "$count booking(s) in the next 14 days"

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
                binding.tvScheduleSummary.text = "Could not load schedule"
            }
    }

    private fun markAsCompleted(booking: Booking) {
        if (booking.bookingId.isEmpty()) return
        db.collection("bookings").document(booking.bookingId)
            .update("status", "Completed")
            .addOnSuccessListener {
                Toast.makeText(this, "Booking marked as completed", Toast.LENGTH_SHORT).show()
                val idx = scheduleList.indexOfFirst { it.bookingId == booking.bookingId }
                if (idx >= 0) {
                    scheduleList[idx] = scheduleList[idx].copy(status = "Completed")
                    adapter.notifyItemChanged(idx)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
