package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.arirang.beautylounge.databinding.ActivityOwnerDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class OwnerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOwnerDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOwnerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadOwnerData()
        loadStats()
        setupClickListeners()
    }

    private fun loadOwnerData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Owner"
                    val salonCode = document.getString("salonCode") ?: "N/A"
                    binding.tvWelcome.text = "Welcome, $name!"
                    binding.tvSalonCode.text = "Salon Code: $salonCode"
                }
            }
    }

    private fun loadStats() {
        db.collection("users").whereEqualTo("role", "staff").get()
            .addOnSuccessListener { documents ->
                binding.tvStaffCount.text = "${documents.size()} Staff Members"
            }

        db.collection("users").whereEqualTo("role", "customer").get()
            .addOnSuccessListener { documents ->
                binding.tvCustomerCount.text = "${documents.size()} Customers"
            }

        db.collection("bookings").get()
            .addOnSuccessListener { documents ->
                binding.tvBookingCount.text = "${documents.size()} Total Bookings"
            }
    }

    private fun setupClickListeners() {
        binding.btnManageStaff.setOnClickListener {
            android.widget.Toast.makeText(this, "Staff Management - Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnViewAllBookings.setOnClickListener {
            startActivity(Intent(this, OwnerAllBookingsActivity::class.java))
        }

        binding.btnInventory.setOnClickListener {
            android.widget.Toast.makeText(this, "Inventory Management - Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnReports.setOnClickListener {
            android.widget.Toast.makeText(this, "Reports & Analytics - Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnManageSchedules.setOnClickListener {
            android.widget.Toast.makeText(this, "Schedule Management - Coming Soon!", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finishAffinity()
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Prevent going back from dashboard
    }
}
