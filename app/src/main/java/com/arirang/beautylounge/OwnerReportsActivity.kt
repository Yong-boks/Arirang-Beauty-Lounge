package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityOwnerReportsBinding
import com.google.firebase.firestore.FirebaseFirestore

class OwnerReportsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerReportsBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Reports & Analytics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        db = FirebaseFirestore.getInstance()
        loadReports()
    }

    private fun loadReports() {
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.GONE

        db.collection("bookings").get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                binding.layoutContent.visibility = View.VISIBLE

                var totalRevenue = 0L
                var completedRevenue = 0L
                var confirmedCount = 0
                var completedCount = 0
                var cancelledCount = 0

                val categoryRevenue = mutableMapOf<String, Long>()
                val serviceCount = mutableMapOf<String, Int>()
                val staffCount = mutableMapOf<String, Int>()

                for (doc in documents) {
                    val status = doc.getString("status") ?: "Confirmed"
                    val price = doc.getLong("price") ?: 0L
                    val category = doc.getString("serviceCategory") ?: "Other"
                    val serviceName = doc.getString("serviceName") ?: "Unknown"
                    val staffName = doc.getString("staffName") ?: "Unknown"

                    when (status) {
                        "Confirmed" -> confirmedCount++
                        "Completed" -> {
                            completedCount++
                            completedRevenue += price
                        }
                        "Cancelled" -> cancelledCount++
                    }

                    if (status != "Cancelled") {
                        totalRevenue += price
                        categoryRevenue[category] = (categoryRevenue[category] ?: 0L) + price
                        serviceCount[serviceName] = (serviceCount[serviceName] ?: 0) + 1
                        staffCount[staffName] = (staffCount[staffName] ?: 0) + 1
                    }
                }

                // Revenue summary
                binding.tvTotalRevenue.text = "KES $totalRevenue"
                binding.tvCompletedRevenue.text = "KES $completedRevenue"

                // Status breakdown
                binding.tvConfirmedCount.text = confirmedCount.toString()
                binding.tvCompletedCount.text = completedCount.toString()
                binding.tvCancelledCount.text = cancelledCount.toString()

                // Revenue by category
                populateRankedList(
                    binding.layoutCategoryStats,
                    categoryRevenue.entries
                        .sortedByDescending { it.value }
                        .map { "${it.key}" to "KES ${it.value}" }
                )

                // Top 5 services
                populateRankedList(
                    binding.layoutTopServices,
                    serviceCount.entries
                        .sortedByDescending { it.value }
                        .take(5)
                        .mapIndexed { idx, entry -> "#${idx + 1}  ${entry.key}" to "${entry.value} bookings" }
                )

                // Top 5 staff
                populateRankedList(
                    binding.layoutTopStaff,
                    staffCount.entries
                        .sortedByDescending { it.value }
                        .take(5)
                        .mapIndexed { idx, entry -> "#${idx + 1}  ${entry.key}" to "${entry.value} bookings" }
                )
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
            }
    }

    /** Inflates simple two-column rows into the given parent [LinearLayout]. */
    private fun populateRankedList(parent: LinearLayout, items: List<Pair<String, String>>) {
        parent.removeAllViews()
        if (items.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No data available"
                textSize = 13f
                setTextColor(0xFF9E9E9E.toInt())
            }
            parent.addView(empty)
            return
        }
        for ((label, value) in items) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val pad = (8 * resources.displayMetrics.density).toInt()
                setPadding(0, pad / 2, 0, pad / 2)
            }
            val tvLabel = TextView(this).apply {
                text = label
                textSize = 13f
                setTextColor(0xFF212121.toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val tvValue = TextView(this).apply {
                text = value
                textSize = 13f
                setTextColor(0xFF1565C0.toInt())
                textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            }
            row.addView(tvLabel)
            row.addView(tvValue)
            parent.addView(row)

            // Thin divider
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).also { it.setMargins(0, 2, 0, 2) }
                setBackgroundColor(0xFFF5F5F5.toInt())
            }
            parent.addView(divider)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
