package com.arirang.beautylounge

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            Toast.makeText(this, "Staff Management - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewAllBookings.setOnClickListener {
            Toast.makeText(this, "All Bookings - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnInventory.setOnClickListener {
            Toast.makeText(this, "Inventory Management - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnReports.setOnClickListener {
            Toast.makeText(this, "Reports & Analytics - Coming Soon!", Toast.LENGTH_SHORT).show()
        }

        binding.btnManageSchedules.setOnClickListener {
            Toast.makeText(this, "Schedule Management - Coming Soon!", Toast.LENGTH_SHORT).show()
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
