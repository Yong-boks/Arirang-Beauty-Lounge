package com.arirang.beautylounge

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.arirang.beautylounge.databinding.ActivityStaffCustomersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StaffCustomersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStaffCustomersBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val customerList = mutableListOf<CustomerSummary>()
    private lateinit var adapter: StaffCustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaffCustomersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "My Customers"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        adapter = StaffCustomerAdapter(customerList)
        binding.rvCustomers.layoutManager = LinearLayoutManager(this)
        binding.rvCustomers.adapter = adapter

        loadCustomers()
    }

    private fun loadCustomers() {
        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.rvCustomers.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE

        db.collection("bookings")
            .whereEqualTo("staffId", uid)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE

                // Sort by createdAt descending so we pick the most recent booking per customer first
                val sortedDocs = documents.sortedByDescending { it.getLong("createdAt") ?: 0L }

                // Group bookings by customerId to get unique customers
                val customerMap = mutableMapOf<String, CustomerSummary>()
                for (doc in sortedDocs) {
                    val customerId = doc.getString("customerId") ?: continue
                    val customerName = doc.getString("customerName") ?: "Unknown"
                    val serviceName = doc.getString("serviceName") ?: ""
                    val date = doc.getString("date") ?: ""
                    val existing = customerMap[customerId]
                    if (existing == null) {
                        // First (most recent) booking for this customer
                        customerMap[customerId] = CustomerSummary(
                            customerId = customerId,
                            customerName = customerName,
                            bookingCount = 1,
                            lastService = serviceName,
                            lastDate = date
                        )
                    } else {
                        // Subsequent bookings - just increment count, keep most recent service/date
                        customerMap[customerId] = existing.copy(bookingCount = existing.bookingCount + 1)
                    }
                }

                customerList.clear()
                customerList.addAll(customerMap.values.sortedByDescending { it.bookingCount })

                val count = customerList.size
                binding.tvCustomerSummary.text = "$count unique customer(s)"

                if (customerList.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rvCustomers.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rvCustomers.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.tvCustomerSummary.text = "Could not load customers"
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
